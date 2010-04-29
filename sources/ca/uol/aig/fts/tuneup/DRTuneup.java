package ca.uol.aig.fts.tuneup;

import ca.uol.aig.fts.drpipeline.DRPipelineDebug;
import ca.uol.aig.fts.display.PlotXY;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import java.awt.Image;
import java.awt.Color;
import java.awt.Container;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;
import java.awt.event.ComponentAdapter;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.JLabel;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Tune-up tool software for FTS-2
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */

public class DRTuneup
{
     /* various controls */
     JFrame drFrame = null;
     JPanel infoPanel = null;
     JPanel displayPanel = null;
     JLabel noticeBoard = null;
     PlotXY phasePlot = null, pcfPlot = null;
     PlotXY spectrumPlot = null, ifgmPlot = null;

     JMenuItem startDR_menuItem = null, stopDR_menuItem;

     /* data reduction class */
     DRPipelineDebug drp = null;

     /* raw input NDF file */
     String rawNDFFile = null;
     /* the prefix of the output file */
     String outFile_prefix = null;

     /* parameters of data reduction */
     int para_pcfSize_h = 60, curr_pcfSize_h = para_pcfSize_h;
     int para_dsSize = 300, curr_dsSize = para_dsSize;
     int para_ssSize = 6000, curr_ssSize = para_ssSize;
     int para_fittingDegree = 2, curr_fittingDegree = para_fittingDegree;
     double para_weight_limit = 0.2, curr_weight_limit = para_weight_limit;
     double para_wn_lBound_percent = 0D, para_wn_uBound_percent = 1.0D;
     int curr_pc_dsSize;
     int curr_pc_ssSize;
     int curr_pc_pcfSize;

     /* flags used to control the display */
     boolean showIfgm_flag = true;
     boolean pauseDR_flag = false;
     boolean stopDR_flag = true;

     /* flags used to control data reduction */
     int deglitch_flag = 3;

     /* range of the pixels */
     int array_widthStart = 0, array_widthEnd = 0;
     int array_heightStart = 0, array_heightEnd = 0;

     /* break time between two consecutive data reduction */
     int time_showPause = 500;

     /* the current pixel */
     int current_WidthIndex = array_widthStart;
     int current_HeightIndex = array_heightEnd;

     /* temporary file prefix for Gnuplot */
     String tmp_prefix = ".tmp_fts2_aig";

     /* the current result from data reduction */

     public double[] curr_mirror_pos_orig_debug = null;
     public double[] curr_ifgm_orig_debug = null;
     public double[] curr_mirror_pos_interp_debug = null;
     public double[] curr_ifgm_interp_debug = null;
     public double[] curr_phase_orig_debug = null;
     public double[] curr_phase_fitting_debug = null;
     public double[] curr_intensity_square_orig_debug = null;
     public double[] curr_pcf_debug = null;
     public double[] curr_spectrum_debug = null;
     public double curr_phaseFittingStdErr_debug = 0;
     public double curr_newInterval_ifgm_debug = 0;

     /* the thread of data reduction */
     Thread drThread = null;

     /* show the DR result of pixel(index_w, index_h) */
     void showDRResult(int index_w, int index_h)
     {
          drp.dataReduction_Debug(index_w, index_h, deglitch_flag);

          try
          {
                Thread.sleep(time_showPause);
                while(pauseDR_flag) Thread.sleep(2000);
          }
          catch(InterruptedException e)
          {
          } 

          curr_mirror_pos_orig_debug = drp.mirror_pos_orig_debug;
          curr_ifgm_orig_debug = drp.ifgm_orig_debug;
          curr_mirror_pos_interp_debug = drp.mirror_pos_interp_debug;
          curr_ifgm_interp_debug = drp.ifgm_interp_debug;
          curr_phase_orig_debug = drp.phase_orig_debug;
          curr_phase_fitting_debug = drp.phase_fitting_debug;
          curr_intensity_square_orig_debug = drp.intensity_square_orig_debug;
          curr_pcf_debug = drp.pcf_debug;
          curr_spectrum_debug = drp.spectrum_debug;
          curr_phaseFittingStdErr_debug = drp.phaseFittingStdErr_debug;
          curr_newInterval_ifgm_debug = drp.newInterval_ifgm_debug;

          curr_pc_dsSize = drp.pc_dsSize;
          curr_pc_ssSize = drp.pc_ssSize;
          curr_pc_pcfSize = drp.pc_pcfSize;

          double[] freq = new double[curr_phase_fitting_debug.length];
          double[][] phase_left = new double[2][];
          double[][] intensity_right = new double[1][];
          phase_left[0] = curr_phase_fitting_debug;
          phase_left[1] = curr_phase_orig_debug; 
          intensity_right[0] = new double[curr_intensity_square_orig_debug.length];
          for(int i=0; i<curr_phase_fitting_debug.length; i++)
          {
               freq[i] = i;
               intensity_right[0][i] = Math.sqrt(curr_intensity_square_orig_debug[i])
                                       /(2*curr_pc_dsSize);
          }
          phasePlot.plot(freq, phase_left, intensity_right);
          double[] x = new double[curr_pcf_debug.length];
          for(int i=0; i<curr_pcf_debug.length; i++)
          {
              x[i] = -curr_pcf_debug.length/2 + 1 + i; 
          }
          double[][] pcf_left = new double[1][];
          double[][] pcf_right = null;
          pcf_left[0] = curr_pcf_debug;
          pcfPlot.plot(x, pcf_left, pcf_right);

          double[] freq_whole = new double[curr_spectrum_debug.length]; 
          for(int i=0; i<curr_spectrum_debug.length; i++) freq_whole[i] = i;
          double[][] spectrum_left = new double[1][];
          double[][] spectrum_right = null;
          spectrum_left[0] = curr_spectrum_debug;
          spectrumPlot.plot(freq_whole, spectrum_left, spectrum_right);

          if(showIfgm_flag)
          {
              double[][] ifgm_left = new double[1][];
              double[][] ifgm_right = null;
              ifgm_left[0] = curr_ifgm_orig_debug;
              ifgmPlot.plot(curr_mirror_pos_orig_debug, ifgm_left, ifgm_right);
          }

          String info_str;
          info_str = "Pixel: (" + index_w + ", " + index_h + ") ==>"
                     + "[std error=" + String.format("%.2g", curr_phaseFittingStdErr_debug) + "]"
                     + ":[PCF:" + curr_pc_pcfSize +"(" + 2*curr_pcfSize_h + ")]"
                     + ":[DS:" + curr_pc_dsSize + "(" + curr_dsSize + ")]"
                     + ":[SS:" + curr_pc_ssSize + "(" + curr_ssSize + ")]"
                     + ":[Weight limit(%):" + curr_weight_limit*100 + "]"
                     + ":[Fitting:" + curr_fittingDegree + "]";

if(curr_phaseFittingStdErr_debug > 0.5)
{
    System.out.println(index_w + ":" + index_h);
}
          noticeBoard.setText(info_str);
          current_WidthIndex = index_w;
          current_HeightIndex = index_h;
     }

     /* create a JFrame for the whole program */
     public void initDRFrame()
     {
          drFrame = new JFrame("FTS-2 Data Reduction");

          drFrame.setJMenuBar(createMenuBar());
          Container content = drFrame.getContentPane();
          content.setBackground(Color.WHITE);
          content.setLayout(new BorderLayout());

          infoPanel = new JPanel();
          displayPanel = new JPanel();

          infoPanel.setBackground(Color.GREEN);

          noticeBoard = new JLabel("    Information Board    ");
          infoPanel.add(noticeBoard);
          infoPanel.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));

          displayPanel.setBackground(Color.WHITE);

          displayPanel.setLayout(new GridBagLayout());
          GridBagConstraints c = new GridBagConstraints();
          phasePlot = new PlotXY("Phase Fitting");
          pcfPlot = new PlotXY("Phase Correction Function");
          spectrumPlot = new PlotXY("Spectrum");
          ifgmPlot = new PlotXY("Interferogram");

          c.fill = GridBagConstraints.BOTH;
          c.gridx = 0; 
          c.gridy = 0;
          displayPanel.add(phasePlot, c);
          c.gridx = 1; 
          c.gridy = 0;
          displayPanel.add(pcfPlot, c);
          c.gridx = 0; 
          c.gridy = 1; 
          c.gridwidth = 2;
          displayPanel.add(spectrumPlot, c);
          c.gridx = 0; 
          c.gridy = 2; 
          c.gridwidth = 2;
          displayPanel.add(ifgmPlot, c);


          content.add(infoPanel, BorderLayout.NORTH);
          content.add(displayPanel, BorderLayout.CENTER);

          drFrame.pack();
          drFrame.setVisible(true);
//        drFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("DRTuneup.png"));
          drFrame.setIconImage(createImageIcon("img/DRTuneup.png"));
          drFrame.addWindowListener(new WindowEventDRFrame());
          drFrame.addComponentListener(new ComponentEventDRFrame());
          displayPanel.addMouseListener(new MouseEventDRFrame());
          displayPanel.setVisible(false);
          infoPanel.setVisible(false);
    }
    protected static Image createImageIcon(String path)
    {
        java.net.URL imgURL = DRTuneup.class.getResource(path);
        if(imgURL != null)
            return (new ImageIcon(imgURL)).getImage();
        else return null;
    }

    /* create the main MenuBar for the program */
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

         menuItem = new JMenuItem("Save the current result ...", KeyEvent.VK_A);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menu.addSeparator();


         menuItem = new JMenuItem("Show the current result in Gnuplot", KeyEvent.VK_G);
         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
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

         menu.addSeparator();

         cbMenuItem = new JCheckBoxMenuItem("Deglitching(Core)", true);
         cbMenuItem.setMnemonic(KeyEvent.VK_C);
         cbMenuItem.addItemListener(cbEvent);
         menu.add(cbMenuItem);

         cbMenuItem = new JCheckBoxMenuItem("Deglitching(Tail)", true);
         cbMenuItem.setMnemonic(KeyEvent.VK_T);
         cbMenuItem.addItemListener(cbEvent);
         menu.add(cbMenuItem);

         menuBar.add(menu);

         menu = new JMenu("Tools");
         menu.setMnemonic(KeyEvent.VK_T);
         menuBar.add(menu);

         menuItem = new JMenuItem("Run", KeyEvent.VK_R);
         startDR_menuItem = menuItem;
         startDR_menuItem.setEnabled(false);

         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                        ActionEvent.ALT_MASK));
         menuItem.addActionListener(menuEvent);
         menu.add(menuItem);

         menuItem = new JMenuItem("Stop", KeyEvent.VK_S);
         stopDR_menuItem = menuItem;
         stopDR_menuItem.setEnabled(false);

         menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
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
    /* file filter for the menu File->"Choose a data file..." */
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

    /* event handler of the main menu bar */
    class MenuEventDRFrame implements ActionListener
    {
         public void actionPerformed(ActionEvent e)
         {
              JMenuItem source = (JMenuItem)e.getSource();
              String menuName = source.getText();
              if(menuName.equals("Choose a data file ..."))
              {
                    String currDir = null;
                    if(rawNDFFile != null)
                    {
                        int index = rawNDFFile.lastIndexOf('/');
                        currDir = rawNDFFile.substring(0, index);
                    }

                    JFileChooser fc = new JFileChooser(currDir);
                    fc.addChoosableFileFilter(new sdfFileFilter());

                    int ret = fc.showOpenDialog(drFrame);
                    if(ret == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fc.getSelectedFile();
                        String fileName = file.getAbsolutePath();
                        int index = fileName.lastIndexOf('.');
                        rawNDFFile = fileName.substring(0, index);
                        startDR_menuItem.setEnabled(true);
                    }
              }
              else if(menuName.equals("Save the current result ..."))
              {
                    saveCurrentResult();
              }
              else if(menuName.equals("Show the current result in Gnuplot"))
              {
                    showInGnuplot();
              }
              else if(menuName.equals("Run"))
              {
                    if(rawNDFFile == null) return;
                    if(stopDR_flag == false) return;
                    infoPanel.setVisible(true);
                    displayPanel.setVisible(true);
                    pauseDR_flag = false;
                    infoPanel.setBackground(Color.GREEN);
                    stopDR_flag  = false;
                    startDR_menuItem.setEnabled(false);
                    stopDR_menuItem.setEnabled(true);
                    drThread = new Thread(new DRThread());
                    drThread.start();
              }
              else if(menuName.equals("Stop"))
              {
                    if(stopDR_flag == true) return;
                    if(drThread != null) 
                    {
                         stopDR_flag = true;
                         infoPanel.setBackground(Color.RED);
                         startDR_menuItem.setEnabled(true);
                         stopDR_menuItem.setEnabled(false);
                    }
              }
              else if(menuName.equals("Exit"))
              {
                    System.exit(0);
              }
              else if(menuName.equals("Options..."))
              {
                   DRParametersDlg drParaDlg = new DRParametersDlg(drFrame);
                   double[] para = new double[11];

                   para[0] = array_widthStart;
                   para[1] = array_widthEnd;
                   para[2] = array_heightStart;
                   para[3] = array_heightEnd;
                   para[4] = para_dsSize;
                   para[5] = para_ssSize;
                   para[6] = para_pcfSize_h;
                   para[7] = para_weight_limit;
                   para[8] = para_fittingDegree;
                   para[9] = para_wn_lBound_percent;
                   para[10] = para_wn_uBound_percent;

                   drParaDlg.setParameters(para);
                   drParaDlg.setVisible(true);

                   if(drParaDlg.paraChanged)
                   {
                        double[] new_para = drParaDlg.getParameters();
                        array_widthStart = (int)new_para[0];
                        array_widthEnd = (int)new_para[1];
                        array_heightStart = (int)new_para[2];
                        array_heightEnd = (int)new_para[3];
                        para_dsSize = (int)new_para[4];
                        para_ssSize = (int)new_para[5];
                        para_pcfSize_h = (int)new_para[6];
                        para_weight_limit = new_para[7];
                        para_fittingDegree = (int)new_para[8];
                        para_wn_lBound_percent = new_para[9];
                        para_wn_uBound_percent = new_para[10];
                   }
              }
              else if(menuName.equals("Break Time"))
              {
                   String str = null;
                   str = JOptionPane.showInputDialog(drFrame, 
                                      "Break time between data reductions (second): ",
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
                   JOptionPane.showMessageDialog(drFrame, 
                            "<HTML><center><FONT COLOR=#008080><FONT SIZE=4><U>"
                           + "Tune-up Tool Software for Scuba-2 FTS</U></FONT></center>"
                           + "</FONT></center><br>"
                           + "<center>by Baoshe Zhang"
                           + "(<A HREF=mailto:baoshe.zhang@uleth.ca>baoshe.zhang@uleth.ca</A>)</center>"
                           + "<center>of <FONT COLOR=#0000ff>AIG Group</FONT>"
                           + " of University of Lethbridge</center></HTML>",
                           "About", JOptionPane.INFORMATION_MESSAGE);
              }
         }
    }

    /* event handler for the menu View->Interferogram */
    class CheckBoxMenuItemEventDRFrame implements ItemListener
    {
         public void itemStateChanged(ItemEvent e)
         {
              JMenuItem source = (JMenuItem)(e.getSource());
              if("Interferogram".equals(source.getText()))
              {
                   Dimension d_size = displayPanel.getSize();
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
                   displayPanel.revalidate();
              }
              else if("Deglitching(Core)".equals(source.getText()))
              {
                  if(e.getStateChange() == ItemEvent.SELECTED)
                        deglitch_flag = deglitch_flag | 1;
                  else
                        deglitch_flag = deglitch_flag & 2;
              }
              else if("Deglitching(Tail)".equals(source.getText()))
              {
                  if(e.getStateChange() == ItemEvent.SELECTED)
                        deglitch_flag = deglitch_flag | 2;
                  else
                        deglitch_flag = deglitch_flag & 1;
              }
         }
    }

    /* window event handler */
    class WindowEventDRFrame extends WindowAdapter
    {
         public void windowClosing(WindowEvent event)
         {
             System.exit(0);
         }
    }

    /* component event handler */
    class ComponentEventDRFrame extends ComponentAdapter
    {
         public void componentResized(ComponentEvent e)
         {
              Dimension d_size = displayPanel.getSize();

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

              displayPanel.revalidate();
         }
    }

    /* mouse event handler */
    class MouseEventDRFrame extends MouseInputAdapter
    {
         public void mouseClicked(MouseEvent e)
         {
              if(javax.swing.SwingUtilities.isLeftMouseButton(e)) 
              {
                   pauseDR_flag = !pauseDR_flag;
                   if(stopDR_flag == false)
                   {
                        if(pauseDR_flag) infoPanel.setBackground(Color.ORANGE);
                        else infoPanel.setBackground(Color.GREEN);
                   }
              }
         }
    }

    void saveCurrentResult()
    {
         if(curr_phase_fitting_debug == null) return;
         if(pauseDR_flag == false && stopDR_flag == false) 
         {
              JOptionPane.showMessageDialog(drFrame,
                       "Pause or stop the data reduction !!!",
                       "Warning", JOptionPane.WARNING_MESSAGE);

              return;
         }
         pauseDR_flag = true;
//       infoPanel.setBackground(Color.ORANGE);
                   
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
              saveResultToFile(outFile_prefix);
         }
    }
    void saveResultToFile(String outFile_prefix)
    {
         {
              try
              {
                    String str;

                    BufferedWriter bw = new BufferedWriter(
                                           new FileWriter(outFile_prefix+"_phase.dat"));
                    for(int i=0; i<curr_phase_fitting_debug.length; i++)
                    {
                         double intensity = Math.sqrt(curr_intensity_square_orig_debug[i])
                                                            /(2*curr_pc_dsSize);
                         str = (i + "  " + curr_phase_fitting_debug[i] + "  " + 
                                       curr_phase_orig_debug[i] + " " + intensity + "\n");
                         bw.write(str, 0, str.length());
                    }
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(outFile_prefix+"_ifgm_orig.dat"));
                    for(int i=0; i<curr_mirror_pos_orig_debug.length; i++)
                    {
                          str = (curr_mirror_pos_orig_debug[i] + "   " 
                                         + curr_ifgm_orig_debug[i] + "\n");
                          bw.write(str, 0, str.length());
                    } 
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(outFile_prefix+"_ifgm_interp.dat"));
                    for(int i=0; i<curr_mirror_pos_interp_debug.length; i++)
                    {
                          str = (curr_mirror_pos_interp_debug[i] + "   " 
                                        + curr_ifgm_interp_debug[i] + "\n");
                          bw.write(str, 0, str.length());
                    }
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(outFile_prefix+"_pcf.dat"));
                    for(int i=0; i<curr_pcf_debug.length; i++)
                    {
                          int x = -curr_pcf_debug.length/2 + 1 + i;
                          str = (x + "   " + curr_pcf_debug[i] + "\n");
                          bw.write(str, 0, str.length());
                    }
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(outFile_prefix+"_spectrum.dat"));
                    double unit_spectrum = Math.PI/(curr_pc_ssSize*curr_newInterval_ifgm_debug);
                    for(int i=0; i<curr_spectrum_debug.length; i++)
                    {
                          str = (i + "   " + curr_spectrum_debug[i] 
                                          + "   " + (i*unit_spectrum) + "\n");
                          bw.write(str, 0, str.length());
                    }
                    bw.close();

                    bw = new BufferedWriter(new FileWriter(outFile_prefix+"_info.dat"));
                    str = rawNDFFile + "\n";
                    bw.write(str, 0, str.length());
                    str = ("Size of PCF: " + curr_pc_pcfSize 
                                           + "(" + 2*curr_pcfSize_h + ")\n");
                    bw.write(str, 0, str.length());
                    str = ("Size of the double-sided interferogram: " 
                                   + curr_pc_dsSize + "(" + curr_dsSize + ")\n");
                    bw.write(str, 0, str.length());
                    str = ("Size of the singled-sided interferogram: " 
                                      + curr_pc_ssSize + "(" + curr_ssSize + ")\n");
                    bw.write(str, 0, str.length());
                    str = ("Degree of phase-fitting: " + curr_fittingDegree + "\n");
                    bw.write(str, 0, str.length());
                    str = ("Weight Limit (%): " + curr_weight_limit*100 + "\n");
                    bw.write(str, 0, str.length());
                    str = ("Pixel: (" + current_WidthIndex + ", " 
                                         + current_HeightIndex + ")\n");
                    bw.write(str, 0, str.length());
                    str = ("Std error of phase fitting: " 
                                + curr_phaseFittingStdErr_debug + "\n");
                    bw.write(str, 0, str.length());
                    str = ("Unit of x-axis of the interferogram: " 
                                  + curr_newInterval_ifgm_debug + "\n");
                    bw.write(str, 0, str.length());
                    str = ("Unit of x-axis of the spectrum: " + 
                              Math.PI/(curr_pc_ssSize*curr_newInterval_ifgm_debug));
                    bw.write(str, 0, str.length());

                    bw.close();
              } 
              catch(IOException io_error)
              {
                    io_error.printStackTrace();
              }
         }
    }
    void showInGnuplot()
    {
         if(curr_phase_fitting_debug == null) return;
         if(pauseDR_flag == false && stopDR_flag == false)
         {
              JOptionPane.showMessageDialog(drFrame,
                       "Pause or stop the data reduction !!!",
                       "Warning", JOptionPane.WARNING_MESSAGE);

              return;
         }

         try
         {
              saveResultToFile(tmp_prefix);
              (new File(tmp_prefix + "_info.dat")).deleteOnExit();
              (new File(tmp_prefix + "_ifgm_orig.dat")).deleteOnExit();
              (new File(tmp_prefix + "_ifgm_interp.dat")).deleteOnExit();
              (new File(tmp_prefix + "_phase.dat")).deleteOnExit();
              (new File(tmp_prefix + "_pcf.dat")).deleteOnExit();
              (new File(tmp_prefix + "_spectrum.dat")).deleteOnExit();
              Runtime.getRuntime().exec("gnuplot -persist " 
                        + DRTuneup.class.getResource("gnuplot/fts2_phase.gnu").getFile());
              Runtime.getRuntime().exec("gnuplot -persist "
                        + DRTuneup.class.getResource("gnuplot/fts2_ifgm.gnu").getFile());
              Runtime.getRuntime().exec("gnuplot -persist "
                        + DRTuneup.class.getResource("gnuplot/fts2_pcf.gnu").getFile());
              Runtime.getRuntime().exec("gnuplot -persist "
                        + DRTuneup.class.getResource("gnuplot/fts2_spectrum.gnu").getFile());
         }
         catch(IOException e)
         {
              e.printStackTrace();
         }
    }

    /* the data reduction thread */
    class DRThread implements Runnable
    {
         public void run()
         {
              drp = new DRPipelineDebug(rawNDFFile, para_pcfSize_h, para_dsSize, 
                           para_ssSize, para_fittingDegree, para_weight_limit, 
                           para_wn_lBound_percent, para_wn_uBound_percent);

              curr_pcfSize_h = para_pcfSize_h;
              curr_dsSize = para_dsSize;
              curr_ssSize = para_ssSize;
              curr_fittingDegree = para_fittingDegree;
              curr_weight_limit = para_weight_limit;

              exit_DR:
              for(int i=array_widthStart; i<=array_widthEnd; i++)
                 for(int j=array_heightStart; j<=array_heightEnd; j++)
                 {
                     showDRResult(i, j);
                     if(stopDR_flag) break exit_DR;
                 }
              stopDR_flag = true;
              startDR_menuItem.setEnabled(true);
              stopDR_menuItem.setEnabled(false);

              return;
         }
    }
}

/* the parameter dialog for the menu Tools->Options... */
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
     JTextField tfd_wn_lBound = null;
     JTextField tfd_wn_uBound = null;

     double[] new_para = new double[11];

     public DRParametersDlg(JFrame parentFrame)
     {
          super(parentFrame, true);
       
          paraChanged = false;
          setTitle("Options");
          setSize(500, 330);
//        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
         
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

          GridBagConstraints c = new GridBagConstraints();

          JLabel comments = new JLabel("Pixel Range (x-axis): ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0; 
          c.gridx = 0; 
          c.gridy = 0;
          paraPanel.add(comments, c);

          comments = new JLabel(" From: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 1; 
          c.gridy = 0;
          paraPanel.add(comments, c);

          cbx_Width_start = new JComboBox(width_range);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 0;
          paraPanel.add(cbx_Width_start, c);

          comments = new JLabel(" To: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 1;
          c.gridy = 1;
          paraPanel.add(comments, c);

          cbx_Width_end = new JComboBox(width_range);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 1;
          paraPanel.add(cbx_Width_end, c);

          comments = new JLabel("Pixel Range (y-axis): ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 2;
          paraPanel.add(comments, c);

          comments = new JLabel(" From: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 1; 
          c.gridy = 2;
          paraPanel.add(comments, c);


          cbx_Height_start = new JComboBox(height_range);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 2;
          paraPanel.add(cbx_Height_start, c);

          comments = new JLabel(" To: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 1; 
          c.gridy = 3;
          paraPanel.add(comments, c);

          cbx_Height_end = new JComboBox(height_range);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 3;
          paraPanel.add(cbx_Height_end, c);

          comments = new JLabel("Size of the double-sided: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 4; 
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_dsSize = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 4;
          paraPanel.add(tfd_dsSize, c);

          comments = new JLabel("Size of the single-sided: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 5; 
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_ssSize = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 5;
          paraPanel.add(tfd_ssSize, c);

          comments = new JLabel("Half size of the PCF: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 6; 
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_pcfSize = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 6;
          paraPanel.add(tfd_pcfSize, c);

          comments = new JLabel("Weight Limit (%): ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 7; c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_weightLimit = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 7;
          paraPanel.add(tfd_weightLimit, c);

          comments = new JLabel("Degree of Phase Fitting: ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0; 
          c.gridy = 8; 
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_fittingDegree = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2; 
          c.gridy = 8;
          paraPanel.add(tfd_fittingDegree, c);


          comments = new JLabel("Wavenumber Range(lower bound)(%): ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0;
          c.gridy = 9;
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_wn_lBound = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2;
          c.gridy = 9;
          paraPanel.add(tfd_wn_lBound, c);

          comments = new JLabel("Wavenumber Range(upper bound)(%): ");
          c.fill = GridBagConstraints.NONE;
          c.weightx = 0.0;
          c.gridx = 0;
          c.gridy = 10;
          c.gridwidth = 2;
          paraPanel.add(comments, c);

          tfd_wn_uBound = new JTextField(10);
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1.0;
          c.gridx = 2;
          c.gridy = 10;
          paraPanel.add(tfd_wn_uBound, c);


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
          tfd_wn_lBound.setText(String.valueOf(para[9]*100));
          tfd_wn_uBound.setText(String.valueOf(para[10]*100));
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
                   JOptionPane.showMessageDialog(this, 
                                   "Values of pixel range in x-axis are invalid !!!",
                                   "Bad Value", JOptionPane.ERROR_MESSAGE);
                   return;
              }

              new_para[2] = (Integer)cbx_Height_start.getSelectedItem();
              new_para[3] = (Integer)cbx_Height_end.getSelectedItem();

              if(new_para[2] > new_para[3])
              {
                   JOptionPane.showMessageDialog(this, 
                                    "Values of pixel range in y-axis are invalid !!!",
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
                   error_Index = 5;
                   new_para[9] = Double.parseDouble(tfd_wn_lBound.getText());
                   new_para[9] /= 100.0;
                   new_para[10] = Double.parseDouble(tfd_wn_uBound.getText());
                   new_para[10] /= 100.0;
              }
              catch(NumberFormatException e)
              {
                   if(error_Index == 0)
                      JOptionPane.showMessageDialog(this, 
                                        "Size of the double-sided has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 1)
                      JOptionPane.showMessageDialog(this, 
                                        "Size of the single-sided has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 2)
                      JOptionPane.showMessageDialog(this, 
                                        "Size of the PCF has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 3)
                      JOptionPane.showMessageDialog(this, 
                                        "Size of the weight limit has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 4)
                      JOptionPane.showMessageDialog(this, 
                                        "Degree of phase fitting has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);
                   else if(error_Index == 5)
                      JOptionPane.showMessageDialog(this,
                                        "Wavenumber ranges has a bad Value !!!",
                                        "Bad Value", JOptionPane.ERROR_MESSAGE);

                   return;
              }

              if(new_para[9] > new_para[10] || new_para[9]<0 || new_para[9]>1.0
                                            || new_para[10]<0 || new_para[10]>1.0)
              {
                   JOptionPane.showMessageDialog(this,
                                    "Values of wavenumber range are invalid !!!",
                                    "Bad Value", JOptionPane.ERROR_MESSAGE);
                   return;
              }

              JOptionPane.showMessageDialog(this, 
                                        "The parameters will take effect for next run!!!");
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

