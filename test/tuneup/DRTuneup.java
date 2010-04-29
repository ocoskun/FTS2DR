import ca.uol.aig.fts.drpipeline.DRPipelineDebug;
import ca.uol.aig.fts.display.PlotXY;
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

public class DRTuneup
{
     DRPipelineDebug drp = null;
     PlotXY phasePlot = null, pcfPlot = null, spectrumPlot = null;
     JFrame drFrame = null;
     JPanel controlArea = null;
     JPanel displayArea = null;
     JLabel noticeBoard = null;

     boolean pauseDR = false;

     public static void main(String[] args)
     {
         DRTuneup tp = new DRTuneup();
         tp.createDRFrame();


         String infile = "abc";
         if(args.length != 0)
         {
               infile = args[0];
         }
         System.out.println("data file: " + infile);
         tp.drp = new DRPipelineDebug(infile, 60, 300, 6000, 2, 0.2);

         for(int i=0; i<40; i++)
            for(int j=0; j<32; j++)
            {
                tp.showDRResult(i, j);
                try
                {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e)
                {
                }
            }
     }
     void showDRResult(int index_w, int index_l)
     {
          drp.dataReduction_Debug(index_w, index_l);

          while(pauseDR)
          {
                try
                {
                    Thread.sleep(3000);
                }
                catch(InterruptedException e)
                {
                }
          }
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

          noticeBoard.setText("Pixel: (" + index_w + ", " + index_l + ")");
     }
     public void createDRFrame()
     {
          drFrame = new JFrame("FTS-2 Data Reduction");

          Container content = drFrame.getContentPane();
          content.setBackground(Color.WHITE);
          content.setLayout(new GridBagLayout());

          controlArea = new JPanel();
          displayArea = new JPanel();

          controlArea.setBackground(Color.GRAY);

          noticeBoard = new JLabel("    Information     ");
          controlArea.add(noticeBoard);

          displayArea.setBackground(Color.WHITE);

          displayArea.setLayout(new GridBagLayout());
          GridBagConstraints c_display = new GridBagConstraints();
          phasePlot = new PlotXY("Phase Fitting");
          pcfPlot = new PlotXY("Phase Correction Function");
          spectrumPlot = new PlotXY("Spectrum");
          spectrumPlot.setSize(200, 50);

          c_display.fill = GridBagConstraints.BOTH;
          c_display.gridx = 0; c_display.gridy = 0;
          displayArea.add(phasePlot, c_display);
          c_display.gridx = 1; c_display.gridy = 0;
          displayArea.add(pcfPlot, c_display);
          c_display.gridx = 0; c_display.gridy = 1; c_display.gridwidth = 2;
          displayArea.add(spectrumPlot, c_display);


          GridBagConstraints c_drFrame = new GridBagConstraints();
          c_drFrame.fill = GridBagConstraints.NONE;
          c_drFrame.gridx = 0; c_drFrame.gridy = 0; c_drFrame.gridheight = 1;
          content.add(controlArea, c_drFrame);
          c_drFrame.fill = GridBagConstraints.BOTH;
          c_drFrame.gridx = 0; c_drFrame.gridy = 1; c_drFrame.gridheight = 1;
          content.add(displayArea, c_drFrame);

          drFrame.pack();
          drFrame.setVisible(true);
//          drFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
          drFrame.addWindowListener(new CloserDRFrame());
          drFrame.addComponentListener(new ResizeDisplayArea());
          drFrame.addMouseListener(new PauseDRFrame());
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
              Dimension size = drFrame.getSize();
//            controlArea.setPreferredSize(new Dimension(size.width-10, 1*size.height/6-10));
              displayArea.setPreferredSize(new Dimension(size.width-10, 5*size.height/6-10));
              phasePlot.setPreferredSize(new Dimension(size.width/2-10, 5*size.height/12-10)); 
              pcfPlot.setPreferredSize(new Dimension(size.width/2-10, 5*size.height/12-10));
              spectrumPlot.setPreferredSize(new Dimension(size.width-20, 5*size.height/12-20));
//            drFrame.getContentPane().revalidate();
              drFrame.validate();
         }
    }
    class PauseDRFrame extends MouseInputAdapter
    {
         public void mouseClicked(MouseEvent e)
         {
              pauseDR = !pauseDR;
         }
    }
}
