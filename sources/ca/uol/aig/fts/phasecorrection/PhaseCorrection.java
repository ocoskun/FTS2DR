package ca.uol.aig.fts.phasecorrection;

import ca.uol.aig.fftpack.Complex1D;
import ca.uol.aig.fftpack.RealDoubleFFT;
import ca.uol.aig.fftpack.RealDoubleFFT_Even;
import ca.uol.aig.fftpack.RealDoubleFFT_Odd;
import ca.uol.aig.fts.fitting.PolynomialFitting;

/**
 * Correct the phases of single-interferograms
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */
public class PhaseCorrection
{
     public double[] phase_orig_debug = null;
     public double[] phase_fitting_debug = null;
     public double[] intensity_square_orig_debug = null;
     public double[] pcf_debug = null;

     private int dsLength, ssLength, phaseFittingdegree, pcfSize_h;
     private int wavenumber_lBound, wavenumber_uBound;
     private int left_shifting;


     private double weight_limit_square;
     private RealDoubleFFT      pc_rfft   = null;
     private RealDoubleFFT_Even pc_rfft_e = null;
     private RealDoubleFFT_Odd  pc_rfft_o = null;
     
//   public int dsLength_debug, ssLength_debug, pcfSize_h_debug;
     /**
      * Constructor.
      * @param dsLength the half size of the double-sided interferogram.
      * @param ssLength the half size of the single-sided interferogram.
      * @param pcfSize_h the half size of the phase-correction function.
      * @param weight_limit used to remove insignificant points. The points whose
      *      amplitude is less than (weight_limit x amplitude maxima) will be not
      *      taken into account in the phase-fitting.
      */

     public PhaseCorrection(int dsLength, int ssLength, int phaseFittingdegree, int pcfSize_h, 
                            double weight_limit)
     {
          this.dsLength = dsLength;
          this.ssLength = ssLength;
          this.phaseFittingdegree = phaseFittingdegree;
          this.pcfSize_h = pcfSize_h;
          this.weight_limit_square = weight_limit * weight_limit;

          this.left_shifting = 0;

          this.wavenumber_lBound = 0;
          this.wavenumber_uBound = dsLength;

          pc_rfft   = new RealDoubleFFT(2*dsLength);
          pc_rfft_e = new RealDoubleFFT_Even(dsLength+1);
          pc_rfft_o = new RealDoubleFFT_Odd(dsLength-1);
/*
          System.out.println("Phase Correction: dsLength = " + this.dsLength +
                                             ", ssLength = " + this.ssLength +
                                              ", pcfSize = " + 2*this.pcfSize_h);
*/
     }

     /**
      * Constructor.
      * @param dsLength the half size of the double-sided interferogram.
      * @param ssLength the half size of the single-sided interferogram.
      * @param pcfSize_h the half size of the phase-correction function.
      * @param weight_limit used to remove insignificant points. The points whose
      *      amplitude is less than (weight_limit x amplitude maxima) will be not
      *      taken into account in the phase-fitting.
      * @param ZPD_index the position index of the ZPD.
      * @param interferogram_len the total length of an interferogram.
      * <br>
      * Note: This constructor will use the position of ZPD to determine new values for
      * dsLength and ssLength.
      */
     public PhaseCorrection(int dsLength, int ssLength, int phaseFittingdegree, int pcfSize_h,
                            double weight_limit, int ZPD_index, int interferogram_len)
     {
          this.dsLength = dsLength;
          this.ssLength = ssLength;
          this.phaseFittingdegree = phaseFittingdegree;
          this.pcfSize_h = pcfSize_h;
          this.weight_limit_square = weight_limit * weight_limit;

          /* get a new value of dsLength which satifies 2^a 3^b 5^c 7^d and
             is not greater than (ZPD_index+1)
          */
          if(ZPD_index+1 < this.dsLength) 
                this.dsLength = getNew_dsLength(ZPD_index+1);
          else
                this.dsLength = getNew_dsLength(this.dsLength);
          /* get anew value of dsLength which staifies 2^a 3^b 5^c and 
             is not greater than (interferogram_len - ZPD_index - pcfSize_h).
          */
          int ssLength_init = interferogram_len - ZPD_index - pcfSize_h;
          if(ssLength_init < this.ssLength) 
                this.ssLength = getNew_ssLength(ssLength_init);
          else
                this.ssLength = getNew_ssLength(this.ssLength);
           
          /* get the offset of the left shift */
          this.left_shifting = ZPD_index+1 - this.dsLength;

          this.wavenumber_lBound = 0;
          this.wavenumber_uBound = this.dsLength;

          pc_rfft   = new RealDoubleFFT(2*this.dsLength);
          pc_rfft_e = new RealDoubleFFT_Even(this.dsLength+1);
          pc_rfft_o = new RealDoubleFFT_Odd(this.dsLength-1);
/*
          System.out.println("Phase Correction: dsLength = " + this.dsLength +
                                             ", ssLength = " + this.ssLength +
                                             ", pcfSize  = " + 2*this.pcfSize_h);
*/
     }

     /**
      * Constructor.
      * @param dsLength the half size of the double-sided interferogram.
      * @param ssLength the half size of the single-sided interferogram.
      * @param pcfSize_h the half size of the phase-correction function.
      * @param weight_limit used to remove insignificant points. The points whose
      *      amplitude is less than (weight_limit x amplitude maxima) will be not
      *      taken into account in the phase-fitting.
      * @param ZPD_index the position index of the ZPD.
      * @param interferogram_len the total length of an interferogram.
      * @param wn_lBound_percent the lower bound (%) of the wavenumber range for phase-fitting.
      * @param wn_uBound_percent the upper bound (%) of the wavenumber range for phase-fitting.
      * <br>
      * Note: This constructor will use the position of ZPD to determine new values for
      * dsLength and ssLength.
      */
     public PhaseCorrection(int dsLength, int ssLength, int phaseFittingdegree, int pcfSize_h,
                            double weight_limit, int ZPD_index, int interferogram_len,
                            double wn_lBound_percent, double wn_uBound_percent)
     {
          this.dsLength = dsLength;
          this.ssLength = ssLength;
          this.phaseFittingdegree = phaseFittingdegree;
          this.pcfSize_h = pcfSize_h;
          this.weight_limit_square = weight_limit * weight_limit;

          /* get a new value of dsLength which satifies 2^a 3^b 5^c 7^d and
             is not greater than (ZPD_index+1)
          */
          if(ZPD_index+1 < this.dsLength)
                this.dsLength = getNew_dsLength(ZPD_index+1);
          else
                this.dsLength = getNew_dsLength(this.dsLength);
          /* get anew value of dsLength which staifies 2^a 3^b 5^c and
             is not greater than (interferogram_len - ZPD_index - pcfSize_h).
          */
          int ssLength_init = interferogram_len - ZPD_index - pcfSize_h;
          if(ssLength_init < this.ssLength)
                this.ssLength = getNew_ssLength(ssLength_init);
          else
                this.ssLength = getNew_ssLength(this.ssLength);

          /* get the offset of the left shift */
          this.left_shifting = ZPD_index+1 - this.dsLength;

          this.wavenumber_lBound = (int)(this.dsLength * wn_lBound_percent);
          this.wavenumber_uBound = (int)(this.dsLength * wn_uBound_percent);

          pc_rfft   = new RealDoubleFFT(2*this.dsLength);
          pc_rfft_e = new RealDoubleFFT_Even(this.dsLength+1);
          pc_rfft_o = new RealDoubleFFT_Odd(this.dsLength-1);
     }


     /* get a new dsLength when the original dsLength is not a good number for FFT */
     private int getNew_dsLength(int dsLength)
     {
         int num;
         for(int i=dsLength; i>=1; i--)
         {
             num = i;
             while(num%2 == 0) num /= 2;
             while(num%3 == 0) num /= 3;
             while(num%5 == 0) num /= 5;
             if(num%7 == 0) num /= 7;
             if(num == 1) return i;
         }
         return 1;
     }
     /* get a new ssLength when the original dsLength is not a good number for FFT */
     private int getNew_ssLength(int ssLength_init)
     {
         int num;
         for(int i=ssLength_init; i>=1; i--)
         {
             num = i;
             while(num%2 == 0) num /= 2;
             while(num%3 == 0) num /= 3;
             while(num%5 == 0) num /= 5;
             if(num == 1) return i;
         }
         return 1;
     }


     /**
      * get the size of the single-sided interferogram (half-size).
      * @return the value of dsLength.
      */
     public int get_ssLength()
     {
         return ssLength;
     }

     /**
      * get the size of the double-sided interferogram (half-size).
      * @return the value of dsLength.
      */
     public int get_dsLength()
     {
         return dsLength;
     }
     /**
      * get the size of the phase-correction function (full-size).
      * @return the value of pcfSize.
      */
     public int get_pcfSize()
     {
          if(pcfSize_h >= dsLength) return 2*dsLength;
          else return 2*pcfSize_h;
     }


     /* smooth the phases and get rid of phase jitters */ 
     void phaseSmoothing(double[] phase)
     {
          int[] phase_jitter = new int[phase.length];

          phase_jitter[0] = 0;
          for(int i=1; i<phase.length; i++)
          {
               phase_jitter[i] = phase_jitter[i-1];
               if(Math.abs(phase[i]-phase[i-1]) > Math.PI*2.0/3.0)
               {
                   if(phase[i] > phase[i-1])phase_jitter[i]-=1;
                   else phase_jitter[i]+=1;
               }
          }
          for(int i=0; i<phase.length; i++)
             phase[i] = phase[i] + Math.PI * phase_jitter[i];
     }

     /* get the phases from double-side interferograms */
     double[] getPhase(double[] dsInterferogram, double[] stderr)
     {

          double[] m_dsInterferogram = new double[2*dsLength];

          boolean new_PCF;
          new_PCF = true;
          if(new_PCF)
          {
              System.arraycopy(dsInterferogram, 0, m_dsInterferogram, 0, 2*dsLength);
          }
          else
          {
              System.arraycopy(dsInterferogram, dsLength-1, m_dsInterferogram, 0, dsLength+1);
              System.arraycopy(dsInterferogram, 0, m_dsInterferogram, dsLength+1, dsLength-1);
          }

          Complex1D cSpectrum = new Complex1D();

          pc_rfft.ft(m_dsInterferogram, cSpectrum);

          double[] weights = new double[cSpectrum.x.length];
/*
          int num_ignore_low_frequences = cSpectrum.y.length/20;
          for(int i=0; i<num_ignore_low_frequences; i++) weights[i] = 0.0D;
*/
          for(int i=0; i<wavenumber_lBound; i++) weights[i] = 0.0D;
          for(int i=wavenumber_uBound+1; i<cSpectrum.x.length; i++) weights[i] = 0.0D;

          double weight_max = 0;
          for(int i=wavenumber_lBound; i<=wavenumber_uBound; i++)
          {
               weights[i] = cSpectrum.x[i] * cSpectrum.x[i] + cSpectrum.y[i] * cSpectrum.y[i];
               if(weights[i] > weight_max) weight_max = weights[i];
          }
          weights[cSpectrum.x.length-1] = 0.0;

          if(weight_max <=0) weight_max = 1;

          double[] phase = new double[cSpectrum.x.length];
          double phase_t;
          for(int i=0; i<cSpectrum.x.length; i++)
          {
               phase_t = Math.atan2(cSpectrum.y[i], cSpectrum.x[i]);
               if(new_PCF)
               {
                    phase_t -= Math.PI/(cSpectrum.x.length-1) * i;
               }
               if(phase_t < 0.000)
               {
                    phase_t += Math.PI;
                    if(phase_t < 0.00) phase_t += Math.PI;
               }
               phase[i] = phase_t;
          }

          phaseSmoothing(phase);

          phase_orig_debug = phase;
          intensity_square_orig_debug = weights;

          double weights_abs_min = weight_max * weight_limit_square;

          double[] phase_weights = new double[3*cSpectrum.x.length+1];
          int coord_index = 0;
          int phase_index = cSpectrum.x.length;
          int weight_index = 2*cSpectrum.x.length;

          for(int i=0; i<cSpectrum.x.length; i++)
          {
               if(weights[i] > weights_abs_min)
               { 
                   phase_weights[coord_index++] = i;
                   phase_weights[phase_index++] = phase[i];
                   phase_weights[weight_index++] = weights[i]/weight_max;
               }
          }
          phase_weights[3*cSpectrum.x.length] = coord_index;

          return piecewisePhaseFitting(phase_weights, stderr);
 
//        return phaseFitting(phase_weights);
     }

     /* get the new phases from weighted fitting */
     double[] phaseFitting(double[] phase_weights)
     {
          int NPoints = (int)phase_weights[phase_weights.length-1];
          double[] wavenumber = new double[NPoints];
          double[] phase = new double[NPoints];
          double[] weights = new double[NPoints];
          for(int i=0; i<NPoints;i++)
          {
               wavenumber[i] = phase_weights[i];
               phase[i] = phase_weights[dsLength+1+i];
               weights[i] = phase_weights[2*(dsLength+1)+i];
          }

          PolynomialFitting pnf = new PolynomialFitting(phaseFittingdegree);
          double[] new_phase;

          pnf.fit(wavenumber, phase, weights);
/*
          double[] fittingParam = pnf.getFittingParam();
          for(int i=0; i<fittingParam.length; i++)
              System.out.println(i + ":" + fittingParam[i]);
          System.exit(0);
*/
          double[] new_wavenumber = new double[dsLength+1];
          for(int i=0; i<dsLength+1; i++) new_wavenumber[i] = i;

          new_phase = pnf.getResult(new_wavenumber);

          phase_fitting_debug = new_phase;

          return new_phase;
     }
     /* get the new phases from weighted fitting */
     double[] piecewisePhaseFitting(double[] phase_weights, double[] stderr)
     {
          int NPoints = (int)phase_weights[phase_weights.length-1];
          double[] wavenumber = new double[NPoints];
          double[] phase = new double[NPoints];
          double[] weights = new double[NPoints];

          int[] bandIndex = new int[NPoints];

          bandIndex[0] = 0;
          int bandNumber = 0;
          for(int i=0; i<NPoints;i++)
          {
               wavenumber[i] = phase_weights[i];
               phase[i] = phase_weights[dsLength+1+i];
               weights[i] = phase_weights[2*(dsLength+1)+i];
               if(i>0 && (wavenumber[i]-wavenumber[i-1]>1)) 
               { 
                    if((i-bandIndex[bandNumber])>phaseFittingdegree) 
                    {
                         bandNumber++; 
                         bandIndex[bandNumber] = i;
                    }
               }
          }
          if((NPoints-bandIndex[bandNumber])>phaseFittingdegree) 
          {
               bandNumber++;
               bandIndex[bandNumber] = NPoints;
          }

          PolynomialFitting pnf = new PolynomialFitting(phaseFittingdegree);

          if(bandNumber > 1)
          {
              double[] constantPhase = new double[bandNumber];

              double[] middle_points = new double[bandNumber+1];
              middle_points[0] = wavenumber[0];
              for(int i=1; i<bandNumber; i++)
              {
                  int index = bandIndex[i];
                  middle_points[i] = (wavenumber[index]+wavenumber[index-1])/2;
              }
              middle_points[bandNumber] = wavenumber[NPoints-1];
         
//              System.out.println("num of bands = " + bandNumber);

              double[][] middle_phase = new double[bandNumber][2];
              for(int i=0; i<bandNumber; i++)
              {
                  int num_points = bandIndex[i+1] - bandIndex[i];
                  double[] band_Wavenumber = new double[num_points];
                  System.arraycopy(wavenumber, bandIndex[i], band_Wavenumber, 0, num_points);

                  double[] band_Phase = new double[num_points];
                  System.arraycopy(phase, bandIndex[i], band_Phase, 0, num_points);

                  double[] band_Weights = new double[num_points];
                  System.arraycopy(weights, bandIndex[i], band_Weights, 0, num_points);

//                  System.out.println("band No. = " + i);

                  pnf.fit(band_Wavenumber, band_Phase, band_Weights);
/*
                  for(int jj=0; jj<band_Wavenumber.length; jj++)
                  {
                        System.out.println(band_Wavenumber[jj] + ":" +
                                          band_Phase[jj] + ":" + band_Weights[jj]);
                  }
*/

                  double[] fittingParam = pnf.getFittingParam();
                  constantPhase[i] = fittingParam[0];

                  middle_phase[i][0] = pnf.getResult(middle_points[i]);
                  middle_phase[i][1] = pnf.getResult(middle_points[i+1]);
              }


              double[] bandPhase_Inc = new double[bandNumber];
              bandPhase_Inc[0] = 0;

              for(int i=1; i<bandNumber; i++)
              {
                  int interval = 0;
                  if(middle_phase[i][0]>middle_phase[i-1][1])
                       interval = (int)((middle_phase[i][0]-middle_phase[i-1][1])/Math.PI+0.4444);
                  else interval = (int)((middle_phase[i][0]-middle_phase[i-1][1])/Math.PI-0.4444);
                  bandPhase_Inc[i] = bandPhase_Inc[i-1] - interval * Math.PI;
              }


              for(int i=0; i<bandNumber; i++)
              {
                  for(int k=bandIndex[i]; k<bandIndex[i+1]; k++)
                  {
                     phase[k] += bandPhase_Inc[i];
                  }
              }
          }

//        double[] new_phase;

          pnf.fit(wavenumber, phase, weights);

//        System.out.println("std error = " + pnf.getSTDDev());
          stderr[0] = pnf.getSTDDev();

          double[] new_wavenumber = new double[dsLength+1];
          for(int i=0; i<dsLength+1; i++) new_wavenumber[i] = i;

          double[] new_phase = pnf.getResult(new_wavenumber);

          phase_fitting_debug = new_phase;

          return new_phase;
     }


     /* get a new phase-correction function from the phases */
     double[] calcPCF(double[] phase)
     {
          double[] cos_phase = new double[dsLength+1];
          double[] sin_phase = new double[dsLength-1];

          for(int i=0; i<dsLength+1; i++)
          {
               cos_phase[i] = Math.cos(phase[i]);
          }

          for(int i=0; i<dsLength-1; i++)
          {
               sin_phase[i] = Math.sin(phase[i+1]);
          }

          pc_rfft_e.bt(cos_phase);

          synchronized(pc_rfft_o) /* RealDoubleFFT_Odd is not thread-safe */
          {
               pc_rfft_o.bt(sin_phase);
          }

          double[] pcf;
          if(pcfSize_h >= dsLength)
          {
              pcf = new double[2*dsLength];

              pcf[dsLength-1] = cos_phase[0];
              for(int i=1; i<dsLength; i++)
              {
                 pcf[dsLength-1+i] = cos_phase[i] + sin_phase[i-1];
                 pcf[dsLength-1-i] = cos_phase[i] - sin_phase[i-1];
              }
              pcf[2*dsLength-1] = cos_phase[dsLength];
          }
          else
          {
              pcf = new double[2*pcfSize_h];

              pcf[pcfSize_h-1] = cos_phase[0];

              for(int i=1; i<pcfSize_h; i++)
              {
                 pcf[pcfSize_h-1+i] = cos_phase[i] + sin_phase[i-1];
                 pcf[pcfSize_h-1-i] = cos_phase[i] - sin_phase[i-1];
              }
              pcf[2*pcfSize_h-1] = cos_phase[pcfSize_h] + sin_phase[pcfSize_h-1];
          }

          pcf_debug = pcf;

          return pcf;
     }

     /* convolution between x and y. */
     double[] conv(double[] x, double[] y)
     {
         double temp;
         int k;

         int offset = dsLength+pcfSize_h-2;

         if(x.length < offset)
             throw new IllegalArgumentException("The length of the interferogram is too small!");
         if(x.length < ssLength+dsLength+pcfSize_h-1)
             System.err.println("The length of an interferogram should not less than " 
                                 + (ssLength+dsLength+pcfSize_h-1) + ".\n"
                                 + "Note: the values of the last "
                                 + (ssLength+dsLength+pcfSize_h-1-x.length)
                                 + " willl be set to zero!!!");

         double[] value = new double[ssLength+1];
         for(int i = offset; i < ssLength+1+offset; i++)
         {
              temp = 0;
              for(int j=0; j<y.length; j++)
              {
                   k = i - j;
                   if(k < 0) k += x.length;
//                   if(k > x.length) k -= x.length;
                   if(k < x.length) temp = temp + x[k]*y[j];
              }
              value[i-offset] = temp/(2.0*dsLength);
         }
         return value;
     }

     /* get a new pcfSize_h */
     private int getNew_pcfSize_h(int interferogram_len)
     {
          int num = interferogram_len - (ssLength + dsLength + left_shifting - 1);
          if(pcfSize_h > num) return num;
          else return pcfSize_h;
     }
     /**
      * get the phase-corrected one-side interferogram.
      * @param compositeInterferogram the composite interferogram, including a short double-side
      *                   and a long one-side
      * @return the phose-corrected one-side interferogram. The length of the phase-corrected 
      * interferogram is equal to (ssLength + 1). 
      */
     public double[] getInterferogram(double[] compositeInterferogram, double[] phaseFitting_stderr)
     {
          double[] dsInterferogram = new double[2*dsLength];
          System.arraycopy(compositeInterferogram, left_shifting, dsInterferogram, 0, 2*dsLength);

          double[] new_compositeInterferogram = new double[compositeInterferogram.length-left_shifting];
          System.arraycopy(compositeInterferogram, left_shifting, 
                           new_compositeInterferogram, 0, new_compositeInterferogram.length);

          double[] stderr = new double[1];
          double[] phase = getPhase(dsInterferogram, phaseFitting_stderr);

//          System.out.println("<<< std error >>> = " + phaseFitting_stderr[0]);

          double[] pcf = calcPCF(phase);

          return conv(new_compositeInterferogram, pcf);
     }
}
