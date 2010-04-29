package ca.uol.aig.fts.drpipeline;

import ca.uol.aig.fts.io.DataIO;
import ca.uol.aig.fts.fitting.CubicSplineInterpolation;
import ca.uol.aig.fts.phasecorrection.PhaseCorrection;
import ca.uol.aig.fftpack.RealDoubleFFT_Even;
import ca.uol.aig.fts.deglitch.Deglitching;

/**
 * DRPipeline acts as a data reduction pipeline. It will intergrate 
 * Interferogram/Spectrum I/O, Interpolation, PhaseCorrection,
 * and FFT into a pipeline.
 *
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */
public class DRPipeline 
{
      int dsSize, ssSize, pcfSize_h, fittingDegree;
      double weight_limit, zpd_value;
      double wn_lBound_percent, wn_uBound_percent;
      int deglitching_flag;
      double[] mirrorPos;
      DataIO ndf_ifgm;
      double[][][] ifgm_pc;
      double[][] phaseFitting_stderr = null;

      String instrument = null;
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

      /**
       * Constructor (single-thread version)
       * @param in the path of the raw data file.
       * @param out the path of the reduced data file.
       * @param pcfSize_h the half of the size of the phase correction function.
       * @param dsSize the half of the size of the doube-size interferogram.
       * @param ssSize the half of the size of the single-side interferogram.
       * @param fittingDegree the polynomial degree of phase fitting.
       * @param weight_limit when the amplitude of a point over the amplitude
       *        maxima is greater than weigth_limit, this point will be taken
       *        into account in phase fitting.
       * @param ZPD_value the value of zero-path distance.
       * @param wn_lBound_percent the lower bound (%) of wavenumber range for phase-fitting.
       * @param wn_uBound_percent the upper bound (%) of wavenumber range for phase-fitting.
       * @param deglitching_flag the flag of deglitching. The possible values:
       *        1: only deglitch the core part;
       *        2: only deglitch the tail part;
       *        3: deglitch both of the core part and the tail part.
       *      other values: no deglitching.
       * @param instrument For FTS-2, instrument = 'Scuba2NDF'.
       */
      public DRPipeline(String in, String out, int pcfSize_h, 
                        int dsSize, int ssSize, int fittingDegree, 
                        double weight_limit, double ZPD_value, 
                        double wn_lBound_percent, double wn_uBound_percent,
                        int deglitching_flag, String instrument)
      {
            this.instrument = instrument;

            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = ZPD_value;
            if(wn_lBound_percent < 0) wn_lBound_percent = 0;
            if(wn_lBound_percent > 1) wn_lBound_percent = 1;
            if(wn_uBound_percent < 0) wn_uBound_percent = 0;
            if(wn_uBound_percent > 1) wn_uBound_percent = 1;
            if(wn_lBound_percent >= wn_uBound_percent)
            {
                  wn_lBound_percent = 0;
                  wn_uBound_percent = 1;
            }
            this.wn_lBound_percent = wn_lBound_percent;
            this.wn_uBound_percent = wn_uBound_percent;
            this.deglitching_flag = deglitching_flag;

//          test_dataReduction(in, out); 
            dataReduction(in, out);
      }
      /**
       * Constructor (single-thread version with a default ZPD)
       * @param in the path of the raw data file.
       * @param out the path of the reduced data file.
       * @param pcfSize_h the half of the size of the phase correction function.
       * @param dsSize the half of the size of the doube-size interferogram.
       * @param ssSize the half of the size of the single-side interferogram.
       * @param fittingDegree the polynomial degree of phase fitting.
       * @param weight_limit when the amplitude of a point over the amplitude
       *        maxima is greater than weigth_limit, this point will be taken
       *        into account in phase fitting.
       * @param wn_lBound_percent the lower bound (%) of wavenumber range for phase-fitting.
       * @param wn_uBound_percent the upper bound (%) of wavenumber range for phase-fitting.
       * @param deglitching_flag the flag of deglitching. The possible values:
       *        1: only deglitch the core part;
       *        2: only deglitch the tail part;
       *        3: deglitch both of the core part and the tail part.
       *      other values: no deglitching.
       * @param instrument For FTS-2, instrument = 'Scuba2NDF'.
       * <br>
       * In this constructor,  ZPD_value = 0.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit, 
                        double wn_lBound_percent, double wn_uBound_percent,
                        int deglitching_flag, String instrument)
      {
            this.instrument = instrument;

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

            this.wn_lBound_percent = wn_lBound_percent;
            this.wn_uBound_percent = wn_uBound_percent;

            this.deglitching_flag = deglitching_flag;

            dataReduction(in, out);
      }

      /**
       * Constructor (multi-thread version)
       * @param in the path of the raw data file.
       * @param out the path of the reduced data file.
       * @param pcfSize_h the half of the size of the phase correction function.
       * @param dsSize the half of the size of the doube-size interferogram.
       * @param ssSize the half of the size of the single-size interferogram.
       * @param fittingDegree the polynomial degree of phase fitting.
       * @param weight_limit when the amplitude of a point over the amplitude
       *        maxima is greater than weigth_limit, this point will be taken
       *        into account in phase fitting.
       * @param ZPD_value the value of zero-path distance.
       * @param wn_lBound_percent the lower bound (%) of wavenumber range for phase-fitting.
       * @param wn_uBound_percent the upper bound (%) of wavenumber range for phase-fitting.
       * @param deglitching_flag the flag of deglitching. The possible values:
       *        1: only deglitch the core part;
       *        2: only deglitch the tail part;
       *        3: deglitch both of the core part and the tail part.
       *      other values: no deglitching.
       * @param numThread the number of the computing threads.
       * @param instrument For FTS-2, instrument = 'Scuba2NDF'.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit, double ZPD_value, 
                        double wn_lBound_percent, double wn_uBound_percent,
                        int deglitching_flag,
                        int numThread, String instrument)
      {
            this.instrument = instrument;

            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = ZPD_value;

            if(wn_lBound_percent < 0) wn_lBound_percent = 0;
            if(wn_lBound_percent > 1) wn_lBound_percent = 1;
            if(wn_uBound_percent < 0) wn_uBound_percent = 0;
            if(wn_uBound_percent > 1) wn_uBound_percent = 1;
            if(wn_lBound_percent >= wn_uBound_percent)
            {
                  wn_lBound_percent = 0;
                  wn_uBound_percent = 1;
            }

            this.wn_lBound_percent = wn_lBound_percent;
            this.wn_uBound_percent = wn_uBound_percent;
            this.deglitching_flag = deglitching_flag;

            dataReduction(in, out, numThread);
      }
      /**
       * Constructor (multi-thread version with a default ZPD)
       * @param in the path of the raw data file.
       * @param out the path of the reduced data file.
       * @param pcfSize_h the half of the size of the phase correction function.
       * @param dsSize the half of the size of the doube-size interferogram.
       * @param ssSize the half of the size of the single-size interferogram.
       * @param fittingDegree the polynomial degree of phase fitting.
       * @param weight_limit when the amplitude of a point over the amplitude
       *        maxima is greater than weigth_limit, this point will be taken
       *        into account in phase fitting.
       * @param wn_lBound_percent the lower bound (%) of wavenumber range for phase-fitting.
       * @param wn_uBound_percent the upper bound (%) of wavenumber range for phase-fitting.
       * @param deglitching_flag the flag of deglitching. The possible values:
       *        1: only deglitch the core part;
       *        2: only deglitch the tail part;
       *        3: deglitch both of the core part and the tail part.
       *      other values: no deglitching.
       * @param numThread the number of the computing threads.
       * @param instrument For FTS-2, instrument = 'Scuba2NDF'.
       *
       * <br>
       * In this constructor, ZPD_value = 0.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit, 
                        double wn_lBound_percent, double wn_uBound_percent,
                        int deglitching_flag,
                        int numThread, String instrument)
      {
            this.instrument = instrument;

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

            this.wn_lBound_percent = wn_lBound_percent;
            this.wn_uBound_percent = wn_uBound_percent;
            this.deglitching_flag = deglitching_flag;

            dataReduction(in, out, numThread);
      }

      /* get a DataIO object for an instrument 
       * @param in the path of the input file
       * @param out the path of the output file
       * @param instrument the instrument
       */
      private DataIO getDataIO(String in, String out, String instrument)
      {
            DataIO dataIO = null;
            try
            {
                dataIO = (DataIO)Class.forName("ca.uol.aig.fts.io." + instrument + "IO").newInstance();
                dataIO.init(in, out);
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

      /* this subroutine is used to test individual modules of data reduction */
      private void test_dataReduction(String in, String out)
      {
            /* get the raw data from an interferogram file,
               create a new spectrum file
            */
            long t0 = System.currentTimeMillis();

//            ndf_ifgm = new NDFIO(in, out);
             ndf_ifgm = getDataIO(in, out, instrument);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getOPD();

            long t1 = System.currentTimeMillis();
            System.out.println("Read Time : " + (t1 - t0));
            
            /* interpolation of interferograms */
            CubicSplineInterpolation csi2fts = new CubicSplineInterpolation(mirrorPos);
            double[] single_ifgm;
            int arrayWidth = ndf_ifgm.get_arrayWidth();
            int arrayLength = ndf_ifgm.get_arrayLength();
            double[][][] ifgm_interp = new double[arrayWidth][arrayLength][];

            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     single_ifgm = ndf_ifgm.getInterferogram(i, j);
                     ifgm_interp[i][j] = csi2fts.interpolate(single_ifgm);
               } 

            long t2 = System.currentTimeMillis();
            System.out.println("Interpolation Time : " + (t2 - t1));

            int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
            int interferogram_len = csi2fts.getInterferogramLength();

            /* phase correction of interferograms */

            PhaseCorrection pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree, 
                                                     pcfSize_h, weight_limit, index_ZPD, 
                                                     interferogram_len, 
                                                     wn_lBound_percent, wn_uBound_percent); 

            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

            /* deglitching of interferograms */
            Deglitching deglitch2fts = new Deglitching(pc_dsSize, index_ZPD);
            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     deglitch2fts.deglitch(ifgm_interp[i][j], deglitching_flag);
               }



            phaseFitting_stderr = new double[arrayWidth][arrayLength];
            ifgm_pc = new double[arrayWidth][arrayLength][];
            double[] fittingStderr = new double[1];
            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     ifgm_pc[i][j] = pc2fts.getInterferogram(ifgm_interp[i][j], fittingStderr);
                     phaseFitting_stderr[i][j] = fittingStderr[0];
               }

            long t3 = System.currentTimeMillis();
            System.out.println("Phase Corretion Time : " + (t3 - t2));

            /* FFT of interferograms */
            RealDoubleFFT_Even fft2fts = new RealDoubleFFT_Even(ifgm_pc[0][0].length);
            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     fft2fts.ft(ifgm_pc[i][j]);
                     /* normalize */
                     for(int kk=0; kk<ifgm_pc[i][j].length; kk++)
                     {
                           ifgm_pc[i][j][kk] /= fft2fts.norm_factor;
                           if(ifgm_pc[i][j][kk] < 0) ifgm_pc[i][j][kk] = -ifgm_pc[i][j][kk];
                     }
               }

            long t4 = System.currentTimeMillis();
            System.out.println("FFT Time : " + (t4 - t3));

            /* save the spectrum to a file */
            ndf_ifgm.saveSpectrum(ifgm_pc);

            ndf_ifgm.closeSpectrum();

            long t5 = System.currentTimeMillis();
            System.out.println("Write Time : " + (t5 - t4));
            System.out.println("Total Time : " + (t5 - t0));
      }

      /* single-thread version of data reduction */
      private void dataReduction(String in, String out)
      {
            /* get the raw data from an interferogram file,
             *  create a new spectrum file
             */
//            ndf_ifgm = new NDFIO(in, out);
            ndf_ifgm = getDataIO(in, out, instrument);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getOPD();


            /* interpolation, phase-correction, and FFT of interferograms */
            int arrayWidth = ndf_ifgm.get_arrayWidth();
            int arrayLength = ndf_ifgm.get_arrayLength();

            phaseFitting_stderr = new double[arrayWidth][arrayLength];
            ifgm_pc = new double[arrayWidth][arrayLength][];

            CubicSplineInterpolation csi2fts = new CubicSplineInterpolation(mirrorPos);

            int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
            int interferogram_len = csi2fts.getInterferogramLength();

            PhaseCorrection pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                                         pcfSize_h, weight_limit, index_ZPD,
                                                         interferogram_len, 
                                                         wn_lBound_percent, wn_uBound_percent);
            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

            /* deglitching of interferograms */
            Deglitching deglitch2fts = new Deglitching(pc_dsSize, index_ZPD);

            int new_ssSize = pc2fts.get_ssLength();
            RealDoubleFFT_Even fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

            double[] single_ifgm = null, ifgm_interp = null;

            double[] fittingStderr = new double[1];
            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     single_ifgm = ndf_ifgm.getInterferogram(i, j);
                     ifgm_interp = csi2fts.interpolate(single_ifgm);
                     deglitch2fts.deglitch(ifgm_interp, deglitching_flag);
                     ifgm_pc[i][j] = pc2fts.getInterferogram(ifgm_interp, fittingStderr);
                     phaseFitting_stderr[i][j] = fittingStderr[0];
                     fft2fts.ft(ifgm_pc[i][j]);
                     /* normalize */
                     for(int kk=0; kk<ifgm_pc[i][j].length; kk++)
                     {
                           ifgm_pc[i][j][kk] /= fft2fts.norm_factor;
                           if(ifgm_pc[i][j][kk] < 0) ifgm_pc[i][j][kk] = -ifgm_pc[i][j][kk];
                     }
                     single_ifgm = null;
                     ifgm_interp = null;
               } 

            /* save the spectrum to a file */
            saveSpectrum();
      }

      /* multi-thread version of data reduction */
      private void dataReduction(String in, String out, int numThread)
      {
            /* when numThread is less than 2, use single-thread version of data reduction */
            if(numThread <= 1)
            {
                 dataReduction(in, out);
                 return;
            }

            /* get the raw data from an interferogram file,
               create a new spectrum file
            */
//            ndf_ifgm = new NDFIO(in, out);
            ndf_ifgm = getDataIO(in, out, instrument);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getOPD();

            /* interpolation, phase-correction, and FFT of interferograms */
            int arrayWidth = ndf_ifgm.get_arrayWidth();
            int arrayLength = ndf_ifgm.get_arrayLength();

            phaseFitting_stderr = new double[arrayWidth][arrayLength];
            ifgm_pc = new double[arrayWidth][arrayLength][];

            CubicSplineInterpolation csi2fts = new CubicSplineInterpolation(mirrorPos);
            int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
            int interferogram_len = csi2fts.getInterferogramLength();

            PhaseCorrection pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                                         pcfSize_h, weight_limit, index_ZPD,
                                                         interferogram_len,
                                                         wn_lBound_percent, wn_uBound_percent);
            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

            /* deglitching of interferograms */
            Deglitching deglitch2fts = new Deglitching(pc_dsSize, index_ZPD);

            int new_ssSize = pc2fts.get_ssLength();
            RealDoubleFFT_Even fft2fts = new RealDoubleFFT_Even(new_ssSize+1);


            /* create numThread computation threads */
            if(numThread > arrayWidth) numThread = arrayWidth;
            Thread[] spectra_thread = new Thread[numThread];
                 
            int subarraySize = arrayWidth/numThread;
            int array_residual = arrayWidth - subarraySize * numThread;

            int arrayWidth_start = 0, arrayWidth_end = 0;
            for(int i=0; i<numThread; i++)
            {
                arrayWidth_end = arrayWidth_start + subarraySize;
                if(i < array_residual) arrayWidth_end += 1;

                spectra_thread[i] = new Thread(new
                         CalcSpectrum_Thread(arrayWidth_start, arrayWidth_end - 1,
                                      csi2fts, deglitch2fts, pc2fts, fft2fts));

                arrayWidth_start = arrayWidth_end;
            }

            /* start the numThread computation threads */
            for(int i=0; i<numThread; i++) spectra_thread[i].start();

            /* test if all computation threads are finished */
            boolean finished = false;
            while(!finished)
            {
                 finished = true;
                 for(int i=0; i<numThread; i++)
                 {
                     if(spectra_thread[i].isAlive()) 
                     {
                         finished = false;
                         break;
                     }
                 }
                 try
                 {
                     Thread.sleep(1000); /* the main thread will sleep 1 second */
                 }
                 catch(InterruptedException e)
                 {
                 }
            }

            /* save the spectrum to a file */
            saveSpectrum();
      }
     
      /* save the spectrum to a data file */
      private void saveSpectrum()
      {
            ndf_ifgm.saveSpectrum(ifgm_pc);

            ndf_ifgm.closeSpectrum();
      }
      
      /* the thread for data reduction */
      private class CalcSpectrum_Thread implements Runnable
      {
           int arrayWidth_start, arrayWidth_end;

           CubicSplineInterpolation csi2fts_x;
           Deglitching deglitch2fts_x;
           PhaseCorrection pc2fts_x;
           RealDoubleFFT_Even fft2fts_x;
           /** 
             * Constructor
             * @arrayWidth_start the ordinal width number of the first pixel
             * @arrayWidth_end the ordinal width number of the last pixel
           */
           public CalcSpectrum_Thread(int arrayWidth_start, int arrayWidth_end,
                                      CubicSplineInterpolation csi2fts, 
                                      Deglitching deglitch2fts,
                                      PhaseCorrection pc2fts,
                                      RealDoubleFFT_Even fft2fts)
           {
                 this.arrayWidth_start = arrayWidth_start;
                 this.arrayWidth_end   = arrayWidth_end;

                 this.csi2fts_x = csi2fts;
                 this.deglitch2fts_x = deglitch2fts;
                 this.pc2fts_x = pc2fts;
                 this.fft2fts_x = fft2fts;
           }
           public void run()
           {
                 calcSpectrum_MT();
           }
           /* this thread will do interpolation, phase-correction, and FFT to a part
            * of the interferogram cube 
            */
           void calcSpectrum_MT()
           {
                 /* interpolation, phase-correction, and FFT of interferograms */

                double[] single_ifgm, ifgm_interp;

                double[] fittingStderr = new double[1];
                for(int i=arrayWidth_start; i<=arrayWidth_end; i++)
                   for(int j=0; j<ifgm_pc[0].length; j++)
                   {
                       /* read an interferogram */
                       single_ifgm = ndf_ifgm.getInterferogram(i, j);  
                       /* do interpolation */
                       ifgm_interp = csi2fts_x.interpolate(single_ifgm); 
                       /* do deglitching */
                       deglitch2fts_x.deglitch(ifgm_interp, deglitching_flag);
                       /* do phase-correction */
                       ifgm_pc[i][j] = pc2fts_x.getInterferogram(ifgm_interp, fittingStderr);
                       phaseFitting_stderr[i][j] = fittingStderr[0];
                       /* do FFT, and store the data to ifgm_pc */
                       fft2fts_x.ft(ifgm_pc[i][j]);
                       /* normalize */
                       for(int kk=0; kk<ifgm_pc[i][j].length; kk++)
                       {
                             ifgm_pc[i][j][kk] /= fft2fts_x.norm_factor;
                             if(ifgm_pc[i][j][kk] < 0) ifgm_pc[i][j][kk] = -ifgm_pc[i][j][kk];
                       }
                       single_ifgm = null;
                       ifgm_interp = null;
                   }
           }
      }
}
