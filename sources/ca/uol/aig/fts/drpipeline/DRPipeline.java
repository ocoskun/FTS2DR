package ca.uol.aig.fts.drpipeline;

import ca.uol.aig.fts.io.NDFIO;
import ca.uol.aig.fts.fitting.CubicSplineInterpolation;
import ca.uol.aig.fts.phasecorrection.PhaseCorrection;
import ca.uol.aig.fftpack.RealDoubleFFT_Even;
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
      double[] mirrorPos;
      NDFIO ndf_ifgm;
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
       */
      public DRPipeline(String in, String out, int pcfSize_h, 
                        int dsSize, int ssSize, int fittingDegree, 
                        double weight_limit, double ZPD_value)
      {
            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = ZPD_value;

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
       * <br>
       * In this constructor,  ZPD_value = 0.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit)
      {
            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = 0.0D;

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
       * @param numThread the number of the computing threads.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit, double ZPD_value, int numThread)
      {
            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = ZPD_value;

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
       * @param numThread the number of the computing threads.
       *
       * <br>
       * In this constructor, ZPD_value = 0.
       */
      public DRPipeline(String in, String out, int pcfSize_h,
                        int dsSize, int ssSize, int fittingDegree,
                        double weight_limit, int numThread)
      {
            this.dsSize = dsSize;
            this.ssSize = ssSize;
            this.pcfSize_h = pcfSize_h;
            this.fittingDegree = fittingDegree;
            this.weight_limit = weight_limit;
            this.zpd_value = 0.0D;

            dataReduction(in, out, numThread);
      }


      /* this subroutine is used to test individual modules of data reduction */
      private void test_dataReduction(String in, String out)
      {
            /* get the raw data from an interferogram file,
               create a new spectrum file
            */
            long t0 = System.currentTimeMillis();

            ndf_ifgm = new NDFIO(in, out);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getMirrorPos();

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
                                                     interferogram_len); 

            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

            ifgm_pc = new double[arrayWidth][arrayLength][];
            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     ifgm_pc[i][j] = pc2fts.getInterferogram(ifgm_interp[i][j]);
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
                           ifgm_pc[i][j][kk] /= fft2fts.norm_factor;
               }

            long t4 = System.currentTimeMillis();
            System.out.println("FFT Time : " + (t4 - t3));

            /* save the spectrum to a file */

            int arraySize = arrayWidth * arrayLength;
            int nPoints_Spectrum = ifgm_pc[0][0].length;
            int cubeSize = arraySize * nPoints_Spectrum; 
            float[] cube_spectrum = new float[cubeSize];
            int index = 0;
            for(int k = 0; k < nPoints_Spectrum; k++)
               for(int j=0; j<arrayLength; j++)
                  for(int i=0; i<arrayWidth; i++)
                  {
                        cube_spectrum[index] = (float)ifgm_pc[i][j][k];
                        index++;
                  }
            long[] spectrum_cube_shape = new long[3];
            spectrum_cube_shape[0] = arrayWidth;
            spectrum_cube_shape[1] = arrayLength;
            spectrum_cube_shape[2] = nPoints_Spectrum;
            ndf_ifgm.saveSpectrum(cube_spectrum, spectrum_cube_shape);

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
            ndf_ifgm = new NDFIO(in, out);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getMirrorPos();


            /* interpolation, phase-correction, and FFT of interferograms */
            int arrayWidth = ndf_ifgm.get_arrayWidth();
            int arrayLength = ndf_ifgm.get_arrayLength();
            ifgm_pc = new double[arrayWidth][arrayLength][];

            CubicSplineInterpolation csi2fts = new CubicSplineInterpolation(mirrorPos);

            int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
            int interferogram_len = csi2fts.getInterferogramLength();

            PhaseCorrection pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                                         pcfSize_h, weight_limit, index_ZPD,
                                                         interferogram_len);
            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

            int new_ssSize = pc2fts.get_ssLength();
            RealDoubleFFT_Even fft2fts = new RealDoubleFFT_Even(new_ssSize+1);

            double[] single_ifgm = null, ifgm_interp = null;

            for(int i=0; i<arrayWidth; i++)
               for(int j=0; j<arrayLength; j++)
               {
                     single_ifgm = ndf_ifgm.getInterferogram(i, j);
                     ifgm_interp = csi2fts.interpolate(single_ifgm);
                     ifgm_pc[i][j] = pc2fts.getInterferogram(ifgm_interp);
                     fft2fts.ft(ifgm_pc[i][j]);
                     /* normalize */
                     for(int kk=0; kk<ifgm_pc[i][j].length; kk++)
                           ifgm_pc[i][j][kk] /= fft2fts.norm_factor;
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
            ndf_ifgm = new NDFIO(in, out);

            /* get the original irregular mirror positions */
            mirrorPos = ndf_ifgm.getMirrorPos();

            /* interpolation, phase-correction, and FFT of interferograms */
            int arrayWidth = ndf_ifgm.get_arrayWidth();
            int arrayLength = ndf_ifgm.get_arrayLength();
            ifgm_pc = new double[arrayWidth][arrayLength][];

            CubicSplineInterpolation csi2fts = new CubicSplineInterpolation(mirrorPos);
            int index_ZPD = csi2fts.getIndex_ZPD(zpd_value);
            int interferogram_len = csi2fts.getInterferogramLength();

            PhaseCorrection pc2fts = new PhaseCorrection(dsSize, ssSize, fittingDegree,
                                                         pcfSize_h, weight_limit, index_ZPD,
                                                         interferogram_len);
            pc_dsSize = pc2fts.get_dsLength();
            pc_ssSize = pc2fts.get_ssLength();
            pc_pcfSize = pc2fts.get_pcfSize();

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
                                      csi2fts, pc2fts, fft2fts));

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
            int cubeSize = ifgm_pc.length * ifgm_pc[0].length * ifgm_pc[0][0].length; 
            float[] cube_spectrum = new float[cubeSize];
            int index = 0;
            /* convert the spectrum cube from 3-d data format to 1-d data format */
            for(int k = 0; k < ifgm_pc[0][0].length; k++)
               for(int j=0; j<ifgm_pc[0].length; j++)
                  for(int i=0; i<ifgm_pc.length; i++)
                  {
                        cube_spectrum[index] = (float)ifgm_pc[i][j][k];
                        index++;
                  }
            long[] spectrum_cube_shape = new long[3];
            spectrum_cube_shape[0] = ifgm_pc.length;
            spectrum_cube_shape[1] = ifgm_pc[0].length;
            spectrum_cube_shape[2] = ifgm_pc[0][0].length;
            ndf_ifgm.saveSpectrum(cube_spectrum, spectrum_cube_shape);

            ndf_ifgm.closeSpectrum();
      }
      
      /* the thread for data reduction */
      private class CalcSpectrum_Thread implements Runnable
      {
           int arrayWidth_start, arrayWidth_end;

           CubicSplineInterpolation csi2fts_x;
           PhaseCorrection pc2fts_x;
           RealDoubleFFT_Even fft2fts_x;

           /** 
             * Constructor
             * @arrayWidth_start the ordinal width number of the first pixel
             * @arrayWidth_end the ordinal width number of the last pixel
           */
           public CalcSpectrum_Thread(int arrayWidth_start, int arrayWidth_end,
                                      CubicSplineInterpolation csi2fts, 
                                      PhaseCorrection pc2fts,
                                      RealDoubleFFT_Even fft2fts)
           {
                 this.arrayWidth_start = arrayWidth_start;
                 this.arrayWidth_end   = arrayWidth_end;

                 this.csi2fts_x = csi2fts;
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

                for(int i=arrayWidth_start; i<=arrayWidth_end; i++)
                   for(int j=0; j<ifgm_pc[0].length; j++)
                   {
                       /* read an interferogram */
                       single_ifgm = ndf_ifgm.getInterferogram(i, j);  
                       /* do interpolation */
                       ifgm_interp = csi2fts_x.interpolate(single_ifgm); 
                       /* do phase-correction */
                       ifgm_pc[i][j] = pc2fts_x.getInterferogram(ifgm_interp);
                       /* do FFT, and store the data to ifgm_pc */
                       fft2fts_x.ft(ifgm_pc[i][j]);
                       /* normalize */
                       for(int kk=0; kk<ifgm_pc[i][j].length; kk++)
                             ifgm_pc[i][j][kk] /= fft2fts_x.norm_factor;

                       single_ifgm = null;
                       ifgm_interp = null;
                   }
           }
      }
}
