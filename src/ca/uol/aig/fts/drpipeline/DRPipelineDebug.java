package ca.uol.aig.fts.drpipeline;

import ca.uol.aig.fts.io.DataIO;
import ca.uol.aig.fts.fitting.CubicSplineInterpolation;
import ca.uol.aig.fts.phasecorrection.PhaseCorrection;
import ca.uol.aig.fftpack.RealDoubleFFT_Even;
import ca.uol.aig.fts.deglitch.Deglitching;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * DRPipeline acts as a data reduction pipeline. It will intergrate 
 * Interferogram/Spectrum I/O, Interpolation, PhaseCorrection,
 * and FFT into a pipeline.
 *
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */
public class DRPipelineDebug
{
      String instrument;

      int dsSize, ssSize, pcfSize_h, fittingDegree;
      double weight_limit, zpd_value;
      Object mirrorPos;
      DataIO ndf_ifgm;
      double[][][] ifgm_pc;

      /**
       * The exact size of the double-sided interferogram used in PhaseCorrection.
       * PhaseCorrection will use initial values of ZPD_value, dsSize, ssSize and
       * pcfSize_h to get new values for dsSize and ssSize.
       */
      public int pc_dsSize;

      /**
       * The exact size of the single-sided interferogram used in PhaseCorrection.
       * PhaseCorrection will use initial values of ZPD_value, dsSize, ssSize and
       * pcfSize_h to get new values for dsSize and ssSize.
       */

      public int pc_ssSize;
      /**
       * The exact size of the phasecorrection function used in PhaseCorrection.
       * PhaseCorrection will use initial values of ZPD_value, dsSize, ssSize and
       * pcfSize_h to get new values for dsSize and ssSize.
       */
      public int pc_pcfSize;

      public int zpd_index; 

      public double[] mirror_pos_orig_debug = null;
      public double[] ifgm_orig_debug = null;
      public double[] mirror_pos_interp_debug = null;
      public double[] ifgm_interp_debug = null;
      public double[] phase_orig_debug = null;
      public double[] phase_fitting_debug = null;
      public double[] intensity_square_orig_debug = null;
      public double[] pcf_debug = null;
      public double[] spectrum_debug = null;
      public double phaseFittingStdErr_debug = 0;
      public double newInterval_ifgm_debug = 0;
      public double[] wn_Bounds = new double[2];

      CubicSplineInterpolation csi2fts = null;
      PhaseCorrection pc2fts = null;
      RealDoubleFFT_Even fft2fts = null; 
      Deglitching deglitch2fts = null;

      /* this subroutine is used for the purpose of debug */
      public DRPipelineDebug(Object[] ioParams, int pcfSize_h, int dsSize, int ssSize, 
                        int fittingDegree, double weight_limit, 
                        double wn_lBound_percent, double wn_uBound_percent, String instrument)
      {
            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = 0.0D;

            if(wn_lBound_percent < 0) wn_lBound_percent = 0;
            if(wn_lBound_percent > 1) wn_lBound_percent = 1;
            if(wn_uBound_percent < 0) wn_uBound_percent = 0;
            if(wn_uBound_percent > 1) wn_uBound_percent = 1;
            if(wn_lBound_percent >= wn_uBound_percent)
            {
                  wn_lBound_percent = 0;
                  wn_uBound_percent = 1;
            }

            this.wn_Bounds[0] = wn_lBound_percent;
            this.wn_Bounds[1] = wn_uBound_percent;

            /* get the raw data from an interferogram file,
            */
            
            ndf_ifgm = getDataIO(ioParams, instrument);
            mirrorPos = ndf_ifgm.getOPD();

            if(instrument.equals("Scuba2NDF"))
            {
                 /* get the original irregular mirror positions */
                 mirror_pos_orig_debug = (double[])mirrorPos;

                 csi2fts = new CubicSplineInterpolation((double[])mirrorPos);

                 mirror_pos_interp_debug = csi2fts.getNewPosition();
                 newInterval_ifgm_debug = csi2fts.getNewInterval();

                 int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
                 int interferogram_len = csi2fts.getInterferogramLength();

                 /* phase correction of interferograms */
                 pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                         pcfSize_h, weight_limit, index_ZPD,
                                         interferogram_len, 
                                         wn_lBound_percent, wn_uBound_percent);
                 pc_dsSize = pc2fts.get_dsLength();
                 pc_ssSize = pc2fts.get_ssLength();
                 pc_pcfSize = pc2fts.get_pcfSize();

                 int new_ssSize = pc2fts.get_ssLength();
                 fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

//               int tail_starting = index_ZPD + pc_dsSize;
                 deglitch2fts = new Deglitching(pc_dsSize, index_ZPD);
            }
      }
      
      public void dataReduction_Debug(int index_w, int index_l, int deglitch_flag)
      {
            /* interpolation of interferograms */
            double[] single_ifgm;
            single_ifgm = ndf_ifgm.getInterferogram(index_w, index_l);

            ifgm_orig_debug = single_ifgm;

            int new_ssSize = 0;
            if(instrument.equals("SpireFits"))
            {
                 /* get the original irregular mirror positions */
                 double[][][] opd = (double[][][])mirrorPos;
                 mirror_pos_orig_debug = opd[index_w][index_l];

                 csi2fts = new CubicSplineInterpolation(opd[index_w][index_l]);

                 mirror_pos_interp_debug = csi2fts.getNewPosition();
                 newInterval_ifgm_debug = csi2fts.getNewInterval();

                 int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
                 int interferogram_len = csi2fts.getInterferogramLength();

                 /* phase correction of interferograms */
                 pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                         pcfSize_h, weight_limit, index_ZPD,
                                         interferogram_len,
                                         this.wn_Bounds[0], this.wn_Bounds[1]);
                 pc_dsSize = pc2fts.get_dsLength();
                 pc_ssSize = pc2fts.get_ssLength();
                 pc_pcfSize = pc2fts.get_pcfSize();

                 new_ssSize = pc2fts.get_ssLength();
                 fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

//               int tail_starting = index_ZPD + pc_dsSize;
                 deglitch2fts = new Deglitching(pc_dsSize, index_ZPD);
            }

            double[] ifgm_interp = csi2fts.interpolate(single_ifgm);

            deglitch2fts.deglitch(ifgm_interp, deglitch_flag);
            ifgm_interp_debug = ifgm_interp;

            double[] fittingStderr = new double[1];
          
            double[] ifgm_pc = null;
            if(getSTDError(ifgm_interp) > 1.0e-10)
            { 
                 ifgm_pc = pc2fts.getInterferogram(ifgm_interp, fittingStderr);

                 phaseFittingStdErr_debug = fittingStderr[0];
                 phase_orig_debug = pc2fts.phase_orig_debug;
                 phase_fitting_debug = pc2fts.phase_fitting_debug;
                 intensity_square_orig_debug = pc2fts.intensity_square_orig_debug;
                 pcf_debug = pc2fts.pcf_debug;
            }
            else
            {
                 System.out.println("Interferogram (" + index_w + ", "
                                     + index_l + ") is too flat!!!");

                 ifgm_pc = new double[new_ssSize+1];

                 fittingStderr[0] = 1.0;
                 phaseFittingStdErr_debug = fittingStderr[0];

                 phase_orig_debug = new double[pc_dsSize];
                 phase_fitting_debug = new double[pc_dsSize];
                 intensity_square_orig_debug = new double[pc_dsSize];
                 pcf_debug = new double[pc_pcfSize];
            }

try
{
   FileOutputStream out;
   PrintStream p;
    
   out = new FileOutputStream(index_w + "_" + index_l + ".dat");
   p = new PrintStream(out);
   p.println(newInterval_ifgm_debug);
   for(int xyzabc = 0; xyzabc < ifgm_pc.length; xyzabc++)
   {
      p.println(xyzabc + " " + ifgm_pc[xyzabc]);
   }
   p.close();
   out.close();
}
catch(IOException e)
{
   e.printStackTrace();
}

            /* FFT of interferograms */
            fft2fts.ft(ifgm_pc);
            /* normalize */
            for(int kk=0; kk<ifgm_pc.length; kk++) 
            {
                 ifgm_pc[kk] = ifgm_pc[kk]/fft2fts.norm_factor;
                 if(ifgm_pc[kk] < 0) ifgm_pc[kk] = -ifgm_pc[kk];
            }

            spectrum_debug = ifgm_pc;
      }

      /* get the standard deviation for a data array */
      private double getSTDError(double[] x)
      {
            double sumValue = 0;
            double meanValue = 0;
            double squareSum = 0;

            for(int i=0; i<x.length; i++) sumValue += x[i];
            meanValue = sumValue/x.length;

            for(int i=0; i<x.length; i++) squareSum += (x[i]-meanValue)*(x[i]-meanValue);
            return Math.sqrt(squareSum/x.length);
      }
      /* get a DataIO object for an instrument
       * @param ioParams the parameters related to the In/Out data files.
       * @param instrument the instrument.
       */
      private DataIO getDataIO(Object[] ioParams, String instrument)
      {
            DataIO dataIO = null;

            this.instrument = instrument;

            try
            {
                dataIO = (DataIO)Class.forName("ca.uol.aig.fts.io." + instrument + "IO").newInstance();
                dataIO.init(ioParams);
            }
            catch(ClassNotFoundException e)
            {
                System.out.println(e);
            }
            catch(InstantiationException e)
            {
                System.out.println(e);
            }
            catch(IllegalAccessException e)
            {
                System.out.println(e);
            }
            catch(IllegalArgumentException e)
            {
                System.out.println(e);
            }
            return dataIO;
      }

}
