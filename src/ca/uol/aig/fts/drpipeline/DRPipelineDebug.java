package ca.uol.aig.fts.drpipeline;

import ca.uol.aig.fts.io.DataIO;
import ca.uol.aig.fts.fitting.CubicSplineInterpolation;
import ca.uol.aig.fts.phasecorrection.PhaseCorrection;
import ca.uol.aig.fftpack.RealDoubleFFT_Even;
import ca.uol.aig.fts.common.DRCommonDebug;
import ca.uol.aig.fts.deglitch.Deglitching;

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

      CubicSplineInterpolation csi2fts = null;
      PhaseCorrection pc2fts = null;
      RealDoubleFFT_Even fft2fts = null; 
      Deglitching deglitch2fts = null;
      
      int new_ssSize = 0;
      
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

            DRCommonDebug.wn_Bounds[0] = wn_lBound_percent;
            DRCommonDebug.wn_Bounds[1] = wn_uBound_percent;

            /* get the raw data from an interferogram file,
            */
            
            ndf_ifgm = getDataIO(ioParams, instrument);
            mirrorPos = ndf_ifgm.getOPD();

            if(instrument.equals("Scuba2NDF"))
            {
                 /* get the original irregular mirror positions */
                 DRCommonDebug.mirror_pos = (double[])mirrorPos;

                 csi2fts = new CubicSplineInterpolation((double[])mirrorPos);

                 DRCommonDebug.mirror_pos_interp = csi2fts.getNewPosition();
                 DRCommonDebug.newInterval_ifgm = csi2fts.getNewInterval();

                 int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
                 int interferogram_len = csi2fts.getInterferogramLength();

                 /* phase correction of interferograms */
                 pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                         pcfSize_h, weight_limit, index_ZPD,
                                         interferogram_len, 
                                         wn_lBound_percent, wn_uBound_percent);
                 DRCommonDebug.pc_dsSize = pc2fts.get_dsLength();
                 DRCommonDebug.pc_ssSize = pc2fts.get_ssLength();
                 DRCommonDebug.pc_pcfSize = pc2fts.get_pcfSize();

                 new_ssSize = pc2fts.get_ssLength();
                 fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

//               int tail_starting = index_ZPD + pc_dsSize;
                 deglitch2fts = new Deglitching(DRCommonDebug.pc_dsSize, index_ZPD);
            }
      }
      
      public void dataReduction_Debug(int index_w, int index_l, int deglitch_flag)
      {
            /* interpolation of interferograms */
            double[] single_ifgm;
            single_ifgm = ndf_ifgm.getInterferogram(index_w, index_l);

            DRCommonDebug.ifgm = single_ifgm;

            if(instrument.equals("SpireFits"))
            {
                 /* get the original irregular mirror positions */
                 double[][][] opd = (double[][][])mirrorPos;
                 DRCommonDebug.mirror_pos = opd[index_w][index_l];

                 csi2fts = new CubicSplineInterpolation(opd[index_w][index_l]);

                 DRCommonDebug.mirror_pos_interp = csi2fts.getNewPosition();
                 DRCommonDebug.newInterval_ifgm = csi2fts.getNewInterval();

                 int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
                 int interferogram_len = csi2fts.getInterferogramLength();

                 /* phase correction of interferograms */
                 pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                         pcfSize_h, weight_limit, index_ZPD,
                                         interferogram_len,
                                         DRCommonDebug.wn_Bounds[0], DRCommonDebug.wn_Bounds[1]);
                 DRCommonDebug.pc_dsSize = pc2fts.get_dsLength();
                 DRCommonDebug.pc_ssSize = pc2fts.get_ssLength();
                 DRCommonDebug.pc_pcfSize = pc2fts.get_pcfSize();

                 new_ssSize = pc2fts.get_ssLength();
                 fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

//               int tail_starting = index_ZPD + pc_dsSize;
                 deglitch2fts = new Deglitching(DRCommonDebug.pc_dsSize, index_ZPD);
            }

            double[] ifgm_interp = csi2fts.interpolate(single_ifgm);

            deglitch2fts.deglitch(ifgm_interp, deglitch_flag);
            DRCommonDebug.ifgm_interp = ifgm_interp;

            double[] fittingStderr = new double[1];
            double[] fittingParam = new double[fittingDegree+1];

            double[] ifgm_pc = pc2fts.getInterferogram(ifgm_interp, fittingStderr, fittingParam);

            DRCommonDebug.phaseFittingStdErr = fittingStderr[0];
//            phase_orig_debug = pc2fts.phase_orig_debug;
//            phase_fitting_debug = pc2fts.phase_fitting_debug;
//            intensity_square_orig_debug = pc2fts.intensity_square_orig_debug;
//            pcf_debug = pc2fts.pcf_debug;

/*
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
*/
            /* FFT of interferograms */
            fft2fts.ft(ifgm_pc);
            /* normalize */
            for(int kk=0; kk<ifgm_pc.length; kk++) 
            {
                 ifgm_pc[kk] = ifgm_pc[kk]/fft2fts.norm_factor;
                 if(ifgm_pc[kk] < 0) ifgm_pc[kk] = -ifgm_pc[kk];
            }

            DRCommonDebug.spectrum = ifgm_pc;
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
