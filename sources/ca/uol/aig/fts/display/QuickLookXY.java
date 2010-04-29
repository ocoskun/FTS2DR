package ca.uol.aig.fts.display;

import uk.ac.starlink.splat.data.SpecData;
import uk.ac.starlink.splat.data.MEMSpecDataImpl;
import uk.ac.starlink.splat.data.SpecDataComp;
import uk.ac.starlink.splat.util.SplatException;
import uk.ac.starlink.splat.plot.PlotControl;
import uk.ac.starlink.splat.util.Utilities;
import uk.ac.starlink.ast.Frame;
import uk.ac.starlink.ast.FrameSet;
import javax.swing.JFrame;
import java.util.prefs.Preferences;
import java.awt.BorderLayout;

/**
 * Display 1-D x-y curve.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge
 */
public class QuickLookXY extends JFrame
{
      private static Preferences prefs =
                 Preferences.userNodeForPackage(QuickLookXY.class);

      private PlotControl plotControl;
      /**
       * Constructor. Construct a PlotControl with default data.
       */
      public QuickLookXY()
      {
           this("");
      }
      public QuickLookXY(String x_Symbol)
      {
           try
           {
               double[] x = new double[2], y = new double[2];
               MEMSpecDataImpl mdi = new MEMSpecDataImpl("Demo");
               /* construct the default data to be displayed */
               for(int i=0; i<x.length; i++)
               {
                  x[i] = i;
                  y[i] = x[i];
               }

               mdi.setSimpleDataQuick(x, "a.u.", y);
               mdi.setDataLabel("Intensity");
               if(x_Symbol != "")
                   mdi.getAst().setC("Label(1)", x_Symbol);
               else
                   mdi.getAst().setC("Label(1)", "X");

               /* construct a PlotControl and its frame */
               plotControl = new PlotControl(new SpecDataComp(mdi));

               Frame fr = plotControl.getPlotCurrentFrame();
               fr.setTitle("Demo");

               /* the title of the JFrame */
               setTitle(Utilities.getTitle("Interferogram/Spectrum Display System of FTS-2"));


               setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

               getContentPane().add(plotControl, BorderLayout.CENTER);

               Utilities.setFrameSize((JFrame)this, 0, 0, prefs, "PlotControlFrame");

               Utilities.setComponentSize(plotControl.getPlot(), 0, 0, prefs, "DivaPlot");

               plotControl.getPlot().setBaseScale();
               setVisible(true);
           }
           catch(SplatException e)
           {
               e.printStackTrace();
           }
      }
      /**
       * Show an interferogram.
       * @param x the value of x-coordinate
       * @param y the value of y-coordinate
       * @param xUnit the string representing coordinate data unit
       * @param yUnit the string representing data unit
       * @param identifier the identifier for this curve. identifier can be any string.
       */
      public void showInterferogram(double[] x, double y[], String xUnit, String yUnit, String identifier)
      {
           try
           {
               /* set the data to be display */
               MEMSpecDataImpl mdi = new MEMSpecDataImpl(identifier);
               mdi.setSimpleDataQuick(x, yUnit, y);
               mdi.setDataLabel("Intensity");
               plotControl.setSpecDataComp(new SpecDataComp(mdi));

               /* set the frame */
               Frame fr = plotControl.getPlotCurrentFrame();
               fr.setTitle("Interferogram " + identifier);
               fr.setLabel(1, "Distance");
               fr.setUnit(1, xUnit);

               /* redraw the new curve */
               plotControl.updateThePlot(null);
           }
           catch(SplatException e)
           {
               e.printStackTrace();
           }
      }
      /**
       * Show a spectrum.
       * @param x the value of x-coordinate
       * @param y the value of y-coordinate
       * @param xUnit the string representing coordinate data unit
       * @param yUnit the string representing data unit
       * @param identifier the identifier for this curve. identifier can be any string.
       */
      public void showSpectrum(double[] x, double y[], String xUnit, String yUnit, String identifier)
      {
           try
           {
               /* set the data to be display */
               MEMSpecDataImpl mdi = new MEMSpecDataImpl(identifier);
               mdi.setSimpleDataQuick(x, yUnit, y);
               mdi.setDataLabel("Intensity");
               plotControl.setSpecDataComp(new SpecDataComp(mdi));

               /* set the frame */
               Frame fr = plotControl.getPlotCurrentFrame();
               fr.setTitle("Spectrum " + identifier);
               fr.setLabel(1, "Wavenumber");
               fr.setUnit(1, xUnit);

               /* redraw the new curve */
               plotControl.updateThePlot(null);
           }
           catch(SplatException e)
           {
               e.printStackTrace();
           }
      }
}
