import ca.uol.aig.fts.drpipeline.DRPipelineDebug;
import ca.uol.aig.fts.display.PlotXY;
import javax.swing.*;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JPanel;
import javax.swing.event.*;
import javax.swing.JLabel;
import java.io.*;

public class DRTuneup
{
     DRPipelineDebug drp = null;
     PlotXY phasePlot = null, pcfPlot = null;
     PlotXY spectrumPlot = null, ifgmPlot = null;
     JFrame drFrame = null;
     JPanel controlArea = null;
     JPanel displayArea = null;
     JLabel noticeBoard = null;

     String dataFile = null, outFile_prefix = null;
     int pcfSize_h = 60, curr_pcfSize_h = pcfSize_h;
     int dsSize = 300, curr_dsSize = dsSize;
     int ssSize = 6000, curr_ssSize = ssSize;
     int fittingDegree = 2, curr_fittingDegree = fittingDegree;
     double weight_limit = 0.2, curr_weight_limit = weight_limit;

     boolean showIfgm_flag = true;
     boolean pauseDR = false;
     boolean stopDR_flag = true;

     int array_widthStart = 0, array_widthEnd = 0;
     int array_heightStart = 0, array_heightEnd = 0;
     int time_showPause = 500;
     int current_WidthIndex = array_widthStart;
     int current_HeightIndex = array_heightEnd;


     Thread drThread = null;

     public static void main(String[] args)
     {
          DRTuneup tp = new DRTuneup();
          tp.createDRFrame();
     }

     void showDRResult(int index_w, int index_h)
     {
          drp.dataReduction_Debug(index_w, index_h);

          double[] freq = new double[drp.phase_fitting_debug.length];
          double[][] phase_left = new double[2][];
          double[][] intensity_right = new double[1][];
          phase_left[0] = drp.phase_fitting_debug;
          phase_left[1] = drp.phase_orig_debug; 
          intensity_right[0] = new double[drp.intensity_square_orig_debug.length];
          for(int i=0; i<drp.phase_fitting_debug.length; i++)
          {
               freq[i] = i;
               intensity_right[0][i] = Math.sqrt(drp.intensity_square_orig_debug[i])/(2*drp.pc_dsSize);
          }
          phasePlot.plot(freq, phase_left, intensity_right);
          double[] x = new double[drp.pcf_debug.length];
          for(int i=0; i<drp.pcf_debug.length; i++)
          {
              x[i] = -drp.pcf_debug.length/2 + 1 + i; 
          }
          double[][] pcf_left = new double[1][];
          double[][] pcf_right = null;
          pcf_left[0] = drp.pcf_debug;
          pcfPlot.plot(x, pcf_left, pcf_right);

          double[] freq_whole = new double[drp.spectrum_debug.length]; 
          for(int i=0; i<drp.spectrum_debug.length; i++) freq_whole[i] = i;
          double[][] spectrum_left = new double[1][];
          double[][] spectrum_right = null;
          spectrum_left[0] = drp.spectrum_debug;
          spectrumPlot.plot(freq_whole, spectrum_left, spectrum_right);

          if(showIfgm_flag)
          {
              double[][] ifgm_left = new double[1][];
              double[][] ifgm_right = null;
              ifgm_left[0] = drp.ifgm_debug;
              ifgmPlot.plot(drp.mirror_pos_debug, ifgm_left, ifgm_right);
          }

          String info_str;
          info_str = "Pixel: (" + index_w + ", " + index_h + ") ==>"
                     + "[std error=" + String.format("%.2g", drp.phaseFittingStdErr_debug) + "]"
                     + ":[PCF:" + drp.pc_pcfSize +"(" + 2*curr_pcfSize_h + ")]"
                     + ":[DS:" + drp.pc_dsSize + "(" + curr_dsSize + ")]"
                     + ":[SS:" + drp.pc_ssSize + "(" + curr_ssSize + ")]"
                     + ":[Weight limit(%):" + curr_weight_limit*100 + "]"
                     + ":[Fitting:" + curr_fittingDegree + "]";

          noticeBoard.setText(info_str);
          current_WidthIndex = index_w;
          current_HeightIndex = index_h;

     }
     public void createDRFrame()
     {
          drFrame = new JFrame("FTS-2 Data Reduction");

          drFrame.setJMenuBar(createMenuBar());
          Container content = drFrame.getContentPane();
          content.setBackground(Color.WHITE);
//        content.setLayout(new GridBagLayout());
          content.setLayout(new BorderLayout());

          controlArea = new JPanel();
          displayArea = new JPanel();

          controlArea.setBackground(Color.ORANGE);

          noticeBoard = new JLabel("    Information Board    ");
          controlArea.add(noticeBoard);
          controlArea.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));

          displayArea.setBackground(Color.WHITE);

          displayArea.setLayout(new GridBagLayout());
          GridBagConstraints c_display = new GridBagConstraints();
          phasePlot = new PlotXY("Phase Fitting");
          pcfPlot = new PlotXY("Phase Correction Function");
          spectrumPlot = new PlotXY("Spectrum");
          ifgmPlot = new PlotXY("Interferogram");

          c_display.fill = GridBagConstraints.BOTH;
          c_display.gridx = 0; c_display.gridy = 0;
          displayArea.add(phasePlot, c_display);
          c_display.gridx = 1; c_display.gridy = 0;
          displayArea.add(pcfPlot, c_display);
          c_display.gridx = 0; c_display.gridy = 1; c_display.gridwidth = 2;
          displayArea.add(spectrumPlot, c_display);
          c_display.gridx = 0; c_display.gridy = 2; c_display.gridwidth = 2;
          displayArea.add(ifgmPlot, c_display);


          content.add(controlArea, BorderLayout.NORTH);
          content.add(displayArea, BorderLayout.CENTER);

          drFrame.pack();
          drFrame.setVisible(true);
//          drFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//          drFrame.setIconImage(createImageIcon("bug-buddy.png"));
          drFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("DRTuneup.png"));
          drFrame.addWindowListener(new CloserDRFrame());
          drFrame.addComponentListener(new ResizeDisplayArea());
//          displayArea.addComponentListener(new ResizeDisplayArea());
          displayArea.addMouseListener(new PauseDRFrame());
          displayArea.setVisible(false);
          controlArea.setVisible(false);
//          drFrame.addMouseListener(new PauseDRFrame());
    }
    protected static ImageIcon createImageIcon(String path)
    {
        java.net.URL imgURL = DRTuneup.class.getResource(path);
        if(imgURL != null)
            return new ImageIcon(imgURL);
        else return null;
    }


    JMenuBar createMenuBar()
    {
         JMenuBar menuBar;
         JMenu menu, submenu;
         JMenuItem menuItem;
         JRadioButtonMenuItem rbMenuItem;
         JCheckBoxMenuItem cbMenuItem;

         MenuEventDRFrame menuEvent = new MenuEventDRFrame();
         CheckBoxMenuItemEventDRFrame cbEvent = new CheckBoxMenuItemEventDRFrame();

         menuBar = new JMenuBar();
 
         menu = new JMenu("File");
         menu.setMnemonic(KeyEvent.VK_F);
         menuBar.add(menu);

         menuItem = new JMenuItem("Choose a data file ...", KeyEvent.VK_O);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);
         menu.addSeparator();
         
         menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menu = new JMenu("View");
         menu.setMnemonic(KeyEvent.VK_V);
         cbMenuItem = new JCheckBoxMenuItem("Interferogram", true);
         cbMenuItem.setMnemonic(KeyEvent.VK_I);
         cbMenuItem.addItemListener(cbEvent);
         menu.add(cbMenuItem);         
         menuBar.add(menu);

         menu = new JMenu("Tools");
         menu.setMnemonic(KeyEvent.VK_T);
         menuBar.add(menu);

         menuItem = new JMenuItem("Run", KeyEvent.VK_R);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menuItem = new JMenuItem("Stop", KeyEvent.VK_S);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menuItem = new JMenuItem("Pause", KeyEvent.VK_P);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menu.addSeparator();
         menuItem = new JMenuItem("Break Time", KeyEvent.VK_T);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menu.addSeparator();
         menuItem = new JMenuItem("Options...", KeyEvent.VK_O);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);


         menu = new JMenu("Help");
         menu.setMnemonic(KeyEvent.VK_H);

         menuItem = new JMenuItem("About", KeyEvent.VK_A);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menuBar.add(menu);

         return menuBar;
    }
    class sdfFileFilter extends javax.swing.filechooser.FileFilter
    {
         public boolean accept(File file)
         {
              String filename = file.getName();
              return filename.endsWith(".sdf") || file.isDirectory();
         }
         public String getDescription()
         {
              return "N-Dimension Format(*.sdf)";
         }
    }

    class MenuEventDRFrame implements ActionListener
    {
         public void actionPerformed(ActionEvent e)
         {
              JMenuItem source = (JMenuItem)e.getSource();
              String menuName = source.getText();
              if(menuName.equals("Choose a data file ..."))
              {
                    String currDir = null;
                    if(dataFile != null)
                    {
                        int index = dataFile.lastIndexOf('/');
                        currDir = dataFile.substring(0, index);
                    }

                    JFileChooser fc = new JFileChooser(currDir);
                    fc.addChoosableFileFilter(new sdfFileFilter());

                    int ret = fc.showOpenDialog(drFrame);
                    if(ret == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        String fileName = file.getAbsolutePath();
                        int index = fileName.lastIndexOf('.');
                        dataFile = fileName.substring(0, index);
                    }
              }
              else if(menuName.equals("Run"))
              {
                    if(dataFile == null) return;
                    if(stopDR_flag == false) return;
                    controlArea.setVisible(true);
                    displayArea.setVisible(true);
                    pauseDR = false;
                    stopDR_flag  = false;
                    drThread = new Thread(new DRThread());
                    drThread.start();
              }
              else if(menuName.equals("Stop"))
              {
                    if(stopDR_flag == true) return;
                    if(drThread != null) stopDR_flag = true;
              }
              else if(menuName.equals("Exit"))
              {
                    System.exit(0);
              }
              else if(menuName.equals("Options..."))
              {
                   DRParametersDlg drParaDlg = new DRParametersDlg(drFrame);
                   double[] para = new double[9];

                   para[0] = array_widthStart;
                   para[1] = array_widthEnd;
                   para[2] = array_heightStart;
                   para[3] = array_heightEnd;
                   para[4] = dsSize;
                   para[5] = ssSize;
                   para[6] = pcfSize_h;
                   para[7] = weight_limit;
                   para[8] = fittingDegree;

                   drParaDlg.setParameters(para);
                   drParaDlg.setVisible(true);

                   if(drParaDlg.paraChanged)
                   {
                        double[] ret_para = drParaDlg.getParameters();
                        array_widthStart = (int)ret_para[0];
                        array_widthEnd = (int)ret_para[1];
                        array_heightStart = (int)ret_para[2];
                        array_heightEnd = (int)ret_para[3];
                        dsSize = (int)ret_para[4];
                        ssSize = (int)ret_para[5];
                        pcfSize_h = (int)ret_para[6];
                        weight_limit = ret_para[7];
                        fittingDegree = (int)ret_para[8];
                   }
              }
              else if(menuName.equals("Break Time"))
              {
                   String str = null;
                   str = JOptionPane.showInputDialog(drFrame, "Break time between data reductions (second): ",
                                                     (Double)((double)time_showPause/1000.0));
                   if(str != null)
                   {
                        try
                        {
                            double tmp;
                            tmp = Double.parseDouble(str);
                            time_showPause = (int)(tmp*1000);
                        }
                        catch(NumberFormatException err)
                        {
                            JOptionPane.showMessageDialog(drFrame, "Break time has a bad Value",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                        }
                   }
              }
              else if(menuName.equals("About"))
              {
                   JOptionPane.showMessageDialog(drFrame, "Tune-up Tool Software for Scuba-2 FTS\n\n"
                                                          + "by Baoshe Zhang(baoshe.zhang@uleth.ca)\n"
                                                          + "of AIG group of Univ. of Lethbridge\n\n"
                                                          + "Canada",
                                                    "About", JOptionPane.INFORMATION_MESSAGE);
              }
         }
    }

    class CheckBoxMenuItemEventDRFrame implements ItemListener
    {
         public void itemStateChanged(ItemEvent e)
         {
              JMenuItem source = (JMenuItem)(e.getSource());
              if("Interferogram".equals(source.getText()))
              {
                   Dimension d_size = displayArea.getSize();
                   if(e.getStateChange() == ItemEvent.SELECTED)
                   {
                         showIfgm_flag = true;
                   }
                   else
                   {
                         showIfgm_flag = false;
                   }
                   if(showIfgm_flag)
                   {
                       phasePlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/3));
                       pcfPlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/3));
                       spectrumPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/3));
                       ifgmPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/3));
                   }
                   else
                   {
                       phasePlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/2));
                       pcfPlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/2));
                       spectrumPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/2));
                       ifgmPlot.setPreferredSize(new Dimension(0, 0));
                   }
                   displayArea.revalidate();
              }
         }
    }

    class CloserDRFrame extends WindowAdapter
    {
         public void windowClosing(WindowEvent event)
         {
             System.exit(0);
         }
    }
    class ResizeDisplayArea extends ComponentAdapter
    {
         public void componentResized(ComponentEvent e)
         {
              Dimension d_size = displayArea.getSize();

              if(showIfgm_flag)
              {
                  phasePlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/3)); 
                  pcfPlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/3));
                  spectrumPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/3));
                  ifgmPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/3));
              }
              else
              {
                  phasePlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/2));
                  pcfPlot.setPreferredSize(new Dimension(d_size.width/2, d_size.height/2));
                  spectrumPlot.setPreferredSize(new Dimension(d_size.width, d_size.height/2));
                  ifgmPlot.setPreferredSize(new Dimension(0, 0));
              }

              displayArea.revalidate();
         }
    }
    class PauseDRFrame extends MouseInputAdapter
    {
         public void mouseClicked(MouseEvent e)
         {
              if(javax.swing.SwingUtilities.isLeftMouseButton(e)) pauseDR = !pauseDR;
              else
              {
                   pauseDR = true;

                   String currDir = null;
                   if(outFile_prefix != null)
                   {
                        int index = outFile_prefix.lastIndexOf('/');
                        currDir = outFile_prefix.substring(0, index);
                   }


                   JFileChooser fc = new JFileChooser(currDir);

                   int ret = fc.showSaveDialog(drFrame);
                   if(ret == JFileChooser.APPROVE_OPTION)
                   {
                        File file = fc.getSelectedFile();
                        outFile_prefix = file.getAbsolutePath();
                        try
                        {
                             String str;

                             BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_prefix+"_phase.dat"));
                             for(int i=0; i<drp.phase_fitting_debug.length; i++)
                             {
                                  double intensity = Math.sqrt(drp.intensity_square_orig_debug[i])/(2*drp.pc_dsSize);
                                  str = (i + " " + drp.phase_fitting_debug[i] + " " + 
                                            drp.phase_orig_debug[i] + " " + intensity + "\n");
                                  bw.write(str, 0, str.length());
                             }

                             bw.close();
                             bw = new BufferedWriter(new FileWriter(outFile_prefix+"_ifgm.dat"));
                             for(int i=0; i<drp.mirror_pos_debug.length; i++)
                             {
                                  str = (drp.mirror_pos_debug[i] + " " + drp.ifgm_debug[i] + "\n");
                                  bw.write(str, 0, str.length());
                             }

                             bw.close();
                             bw = new BufferedWriter(new FileWriter(outFile_prefix+"_pcf.dat"));
                             for(int i=0; i<drp.pcf_debug.length; i++)
                             {
                                  int x = -drp.pcf_debug.length/2 + 1 + i;
                                  str = (x + " " + drp.pcf_debug[i] + "\n");
                                  bw.write(str, 0, str.length());
                             }

                             bw.close();
                             bw = new BufferedWriter(new FileWriter(outFile_prefix+"_spectrum.dat"));
                             for(int i=0; i<drp.spectrum_debug.length; i++)
                             {
                                 str = (i + " " + drp.spectrum_debug[i] + "\n");
                                 bw.write(str, 0, str.length());
                             }

                             bw.close();
                             bw = new BufferedWriter(new FileWriter(outFile_prefix+"_info.dat"));
                             str = dataFile + "\n";
                             bw.write(str, 0, str.length());
                             str = ("Size of PCF: " + drp.pc_pcfSize + "(" + 2*pcfSize_h + ")\n");
                             bw.write(str, 0, str.length());
                             str = ("Size of the double-sided interferogram: " 
                                        + drp.pc_dsSize + "(" + dsSize + ")\n");
                             bw.write(str, 0, str.length());
                             str = ("Size of the singled-sided interferogram: " 
                                        + drp.pc_ssSize + "(" + ssSize + ")\n");
                             bw.write(str, 0, str.length());
                             str = ("Degree of phase-fitting: " + fittingDegree + "\n");
                             bw.write(str, 0, str.length());
                             str = ("Weight Limit (%): " + weight_limit*100 + "\n");
                             bw.write(str, 0, str.length());
                             str = ("Pixel: (" + current_WidthIndex + ", " + current_HeightIndex + ")\n");
                             bw.write(str, 0, str.length());
                             str = ("std error of phase fitting: " + drp.phaseFittingStdErr_debug + "\n");
                             bw.write(str, 0, str.length());
                             str = ("unit of the interferogram: " + drp.newInterval_ifgm_debug + "\n");
                             bw.write(str, 0, str.length());
                             str = ("unit of the spectrum: " + Math.PI/(drp.pc_ssSize*drp.newInterval_ifgm_debug));
                             bw.write(str, 0, str.length());

                             bw.close();
                        }
                        catch(IOException io_error)
                        {
                             io_error.printStackTrace();
                        }
                   }
              }
         }
    }
    class DRThread implements Runnable
    {
         public void run()
         {
              drp = new DRPipelineDebug(dataFile, pcfSize_h, dsSize, ssSize, fittingDegree, weight_limit);

              curr_pcfSize_h = pcfSize_h;
              curr_dsSize = dsSize;
              curr_ssSize = ssSize;
              curr_fittingDegree = fittingDegree;
              curr_weight_limit = weight_limit;

              exit_DR:
              for(int i=array_widthStart; i<=array_widthEnd; i++)
                 for(int j=array_heightStart; j<=array_heightEnd; j++)
                 {
                     showDRResult(i, j);
                     try
                     {
                         Thread.sleep(time_showPause);
                         while(pauseDR) Thread.sleep(2000);
                     }
                     catch(InterruptedException e)
                     {
                     }
                     if(stopDR_flag) break exit_DR;
                 }
              stopDR_flag = true;
              return;
         }
    }
}

class DRParametersDlg extends JDialog implements ActionListener
{

     public boolean paraChanged = false;
     JComboBox cbx_Width_start = null;
     JComboBox cbx_Width_end = null;
     JComboBox cbx_Height_start = null;
     JComboBox cbx_Height_end = null;
     JTextField tfd_dsSize = null;
     JTextField tfd_ssSize = null;
     JTextField tfd_pcfSize = null;
     JTextField tfd_weightLimit = null;
     JTextField tfd_fittingDegree = null;

     double[] new_para = new double[9];

     public DRParametersDlg(JFrame parentFrame)
     {
          super(parentFrame, true);
       
          paraChanged = false;
          setTitle("Options");
          setSize(500, 300);
//          setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         
          getContentPane().add(createTopPane());
     }

     private JPanel createTopPane()
     {
          JPanel topPanel = new JPanel();
          topPanel.setLayout(new GridBagLayout());

          GridBagConstraints c = new GridBagConstraints();
          c.anchor = GridBagConstraints.CENTER;
          c.fill = GridBagConstraints.HORIZONTAL;
          c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.weightx = 1.0;
          topPanel.add(createParametersPane(), c);

          c.fill = GridBagConstraints.NONE;
          c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.weightx = 0.0;
          c.anchor = GridBagConstraints.NORTH;
          JButton btn_Cancel = new JButton("Cancel");
          btn_Cancel.setActionCommand("Cancel");
          btn_Cancel.addActionListener(this);
          topPanel.add(btn_Cancel, c);
          c.gridx = 1; c.gridy = 1; c.gridwidth = 1; c.weightx = 0.0;
          c.anchor = GridBagConstraints.NORTH;
          JButton btn_OK = new JButton("OK");
          btn_OK.setActionCommand("OK");
          btn_OK.addActionListener(this);
          topPanel.add(btn_OK, c);

          return topPanel;
     }
     private JPanel createParametersPane()
     {
          Integer[] width_range = new Integer[40];
          Integer[] height_range = new Integer[32];
          for(int i=0; i<width_range.length; i++) width_range[i] = i;
          for(int i=0; i<height_range.length; i++) height_range[i] = i;

          JPanel paraPanel = new JPanel();
          paraPanel.setLayout(new GridBagLayout());

          GridBagConstraints c_para = new GridBagConstraints();

          JLabel comments = new JLabel("Pixel Range (x-axis): ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0; 
          c_para.gridx = 0; c_para.gridy = 0;
          paraPanel.add(comments, c_para);

          comments = new JLabel(" From: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 1; c_para.gridy = 0;
          paraPanel.add(comments, c_para);

          cbx_Width_start = new JComboBox(width_range);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 0;
          paraPanel.add(cbx_Width_start, c_para);

          comments = new JLabel(" To: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 1; c_para.gridy = 1;
          paraPanel.add(comments, c_para);

          cbx_Width_end = new JComboBox(width_range);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 1;
          paraPanel.add(cbx_Width_end, c_para);

          comments = new JLabel("Pixel Range (y-axis): ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 2;
          paraPanel.add(comments, c_para);

          comments = new JLabel(" From: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 1; c_para.gridy = 2;
          paraPanel.add(comments, c_para);


          cbx_Height_start = new JComboBox(height_range);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 2;
          paraPanel.add(cbx_Height_start, c_para);

          comments = new JLabel(" To: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 1; c_para.gridy = 3;
          paraPanel.add(comments, c_para);

          cbx_Height_end = new JComboBox(height_range);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 3;
          paraPanel.add(cbx_Height_end, c_para);


          comments = new JLabel("Size of the double-sided: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 4; c_para.gridwidth = 2;
          paraPanel.add(comments, c_para);

          tfd_dsSize = new JTextField(10);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 4;
          paraPanel.add(tfd_dsSize, c_para);

          comments = new JLabel("Size of the single-sided: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 5; c_para.gridwidth = 2;
          paraPanel.add(comments, c_para);

          tfd_ssSize = new JTextField(10);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 5;
          paraPanel.add(tfd_ssSize, c_para);


          comments = new JLabel("Half size of the PCF: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 6; c_para.gridwidth = 2;
          paraPanel.add(comments, c_para);

          tfd_pcfSize = new JTextField(10);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 6;
          paraPanel.add(tfd_pcfSize, c_para);

          comments = new JLabel("Weight Limit (%): ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 7; c_para.gridwidth = 2;
          paraPanel.add(comments, c_para);

          tfd_weightLimit = new JTextField(10);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 7;
          paraPanel.add(tfd_weightLimit, c_para);

          comments = new JLabel("Degree of Phase Fitting: ");
          c_para.fill = GridBagConstraints.NONE;
          c_para.weightx = 0.0;
          c_para.gridx = 0; c_para.gridy = 8; c_para.gridwidth = 2;
          paraPanel.add(comments, c_para);

          tfd_fittingDegree = new JTextField(10);
          c_para.fill = GridBagConstraints.HORIZONTAL;
          c_para.weightx = 1.0;
          c_para.gridx = 2; c_para.gridy = 8;
          paraPanel.add(tfd_fittingDegree, c_para);

          paraPanel.setBorder(BorderFactory.createCompoundBorder
                    (BorderFactory.createTitledBorder("Parameters of Data Reduction"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));

          return paraPanel;
     }
     public void setParameters(double[] para)
     {
         cbx_Width_start.setSelectedIndex((int)para[0]);
         cbx_Width_end.setSelectedIndex((int)para[1]);
         cbx_Height_start.setSelectedIndex((int)para[2]);
         cbx_Height_end.setSelectedIndex((int)para[3]);
         tfd_dsSize.setText(String.valueOf((int)para[4]));
         tfd_ssSize.setText(String.valueOf((int)para[5]));
         tfd_pcfSize.setText(String.valueOf((int)para[6]));
         tfd_weightLimit.setText(String.valueOf(para[7]*100));
         tfd_fittingDegree.setText(String.valueOf((int)para[8]));
     }

     public double[] getParameters()
     {
         return new_para;
     }

     public void actionPerformed(ActionEvent event)
     {
         if("OK".equals(event.getActionCommand()))
         {
              new_para[0] = (Integer)cbx_Width_start.getSelectedItem();
              new_para[1] = (Integer)cbx_Width_end.getSelectedItem();

              if(new_para[0] > new_para[1])
              {
                   JOptionPane.showMessageDialog(this, "values of pixel range in x-axis are invalid !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   return;
              }

              new_para[2] = (Integer)cbx_Height_start.getSelectedItem();
              new_para[3] = (Integer)cbx_Height_end.getSelectedItem();

              if(new_para[2] > new_para[3])
              {
                   JOptionPane.showMessageDialog(this, "value of pixel range in y-axis are invalid !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   return;
              }

              int error_Index = 0;
              try
              {
                   error_Index = 0;
                   new_para[4] = Integer.parseInt(tfd_dsSize.getText());
                   error_Index = 1;
                   new_para[5] = Integer.parseInt(tfd_ssSize.getText());
                   error_Index = 2;
                   new_para[6] = Integer.parseInt(tfd_pcfSize.getText());
                   error_Index = 3;
                   new_para[7] = Double.parseDouble(tfd_weightLimit.getText());
                   new_para[7] /= 100.0;
                   error_Index = 4;
                   new_para[8] = Integer.parseInt(tfd_fittingDegree.getText());
              }
              catch(NumberFormatException e)
              {
                   if(error_Index == 0)
                      JOptionPane.showMessageDialog(this, "size of the double-sided has a bad Value !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 1)
                      JOptionPane.showMessageDialog(this, "size of the single-sided has a bad Value !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 2)
                      JOptionPane.showMessageDialog(this, "size of the PCF has a bad Value !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 3)
                      JOptionPane.showMessageDialog(this, "size of the weight limit has a bad Value !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 4)
                      JOptionPane.showMessageDialog(this, "degree of phase fitting has a bad Value !!!",
                                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   return;
              }

              JOptionPane.showMessageDialog(this, "The parameters will take effect for next run!!!");
              paraChanged = true;
              dispose();
         }
         else if("Cancel".equals(event.getActionCommand()))
         {
              int n = JOptionPane.showConfirmDialog(this, "Discard the change ?", "Warning...",
                                                    JOptionPane.YES_NO_OPTION);
              if(n == JOptionPane.NO_OPTION) return;
              paraChanged = false;
              dispose();
         }
     }
}

