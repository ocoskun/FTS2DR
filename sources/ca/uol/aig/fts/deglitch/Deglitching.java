package ca.uol.aig.fts.deglitch;

import java.util.Arrays;

/**
 * De-glitching
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */

public class Deglitching
{
     final int core_clusterSize = 20;
     final int tail_clusterSize = 60;
     final double tail_cutoff_StdDev_percent = 0.80;
     final double tail_cutoff_StdDev_multiplier = 4;

     int dsSize, index_ZPD;
     /**
      * Constructor.
      * @param dsSize  the half size of the double-sided interferogram.
      * @param index_ZPD the index of the ZPD.
      */
     public Deglitching(int dsSize, int index_ZPD)
     {
           this.dsSize = dsSize;
           this.index_ZPD = index_ZPD;
     }

     /**
      * deglitch an interferogram.
      * @param ifgm an interferogram.
      */

     public void deglitch(double[] ifgm)
     {
           deglitch_CorePart(ifgm);
           deglitch_TailPart(ifgm);
     }
     
     /* 
         deglitch the tail part of an interferogram
     */
     void deglitch_TailPart(double[] ifgm)
     {
           int numData = ifgm.length;

           int tail_deglitching_start = dsSize + index_ZPD;

           int numCluster = (numData - tail_deglitching_start)/tail_clusterSize;
           double[] clusterMean = new double[numCluster];
           double[] clusterDev  = new double[numCluster];

           double[] mean_stddev = new double[2];

           for(int i=0; i<numCluster; i++)
           {
               getMean_StdDev(ifgm, tail_deglitching_start + i*tail_clusterSize, 
                                             tail_clusterSize, mean_stddev);
               clusterMean[i] = mean_stddev[0];
               clusterDev[i] = mean_stddev[1];
           }
           Arrays.sort(clusterDev);         

           double ave_stddev = 0;
           int count = 0;
           for(int i=0; i<(clusterDev.length*tail_cutoff_StdDev_percent); i++) 
           {
                  ave_stddev += clusterDev[i];
                  count++; 
           }
           ave_stddev /= count;

           double cutoff_stddev = ave_stddev*tail_cutoff_StdDev_multiplier;
           int starting_point = 0;
           double mean_t = 0;
           for(int i=0; i<numCluster; i++)
           {
               mean_t = clusterMean[i];
               starting_point = tail_deglitching_start + i*tail_clusterSize;
               for(int j=starting_point; j<starting_point+tail_clusterSize; j++)
               {
                    if(ifgm[j]>mean_t+cutoff_stddev) 
                                  ifgm[j] = mean_t+cutoff_stddev;
                    if(ifgm[j]<mean_t-cutoff_stddev) 
                                  ifgm[j] = mean_t-cutoff_stddev;
               }
           }
           starting_point = tail_deglitching_start + numCluster*tail_clusterSize;

           if(starting_point < numData)
           {
               mean_t = 0.0;
               count = 0;
               for(int j=starting_point; j<numData; j++)
               {
                    mean_t += ifgm[j];
                    count++;
               }
               mean_t /= count;
               for(int j=starting_point; j<numData; j++)
               {
                  if(ifgm[j]>mean_t+cutoff_stddev) 
                                    ifgm[j] = mean_t+cutoff_stddev;
                  if(ifgm[j]<mean_t-cutoff_stddev) 
                                    ifgm[j] = mean_t-cutoff_stddev;
               }
           }
     }
     void getMean_StdDev(double[] ifgm, int starting_point, int clusterSize, double[] mean_stddev)
     {
          double t = 0;
          double mean = 0; double stddev = 0;

          t = 0;
          for(int i=starting_point; i<starting_point+clusterSize; i++)
          {
               t += ifgm[i];
          }
          mean = t / clusterSize;

          t = 0;
          for(int i=starting_point; i<starting_point+clusterSize; i++)
          {
               t += (ifgm[i]-mean)*(ifgm[i]-mean);
          }
          stddev = Math.sqrt(t/clusterSize);

          mean_stddev[0] = mean;
          mean_stddev[1] = stddev;
     }

     /*
          the cutoff function of the core part.
     */
     double core_cutoff(int x, double y, 
                          double mean_t, double amplitude, double freq)
     {
          double t = Math.abs(freq*x);

          double y_t;

          if(t < Math.PI/2)
          {
               if(t > 1.0e-9)
                    y_t = amplitude*Math.sin(t)/t;
               else
                    y_t = amplitude;
          }
          else
          {
               y_t = amplitude/t;
          }

          double new_y;
          double error;

          if(y > mean_t)
          {
               error = y - mean_t;
               if(error > 2*y_t) 
                      new_y = y_t + mean_t;
               else 
                      new_y = y;
          }
          else
          {
               error = mean_t-y;
               if(error > 2*y_t) 
                      new_y = mean_t - y_t;
               else 
                      new_y = y;
          }
          return new_y;
     }

     /*
         deglitch the core part of an interferogram
     */
     void deglitch_CorePart(double[] ifgm)
     {
          double mean_t = 0;
          for(int i=-dsSize+1+index_ZPD; i<dsSize+index_ZPD; i++)
          {
              mean_t += ifgm[i];
          }
          mean_t /= (2*dsSize-1);

          double max_t, min_t;

          max_t = -Double.MAX_VALUE;
          min_t =  Double.MAX_VALUE;;

          for(int i=-core_clusterSize/2+1+index_ZPD; 
                         i<core_clusterSize/2+index_ZPD; i++)
          {
                if(ifgm[i] > max_t) max_t = ifgm[i];
                if(ifgm[i] < min_t) min_t = ifgm[i];
          }
          double cutoff_amp = (max_t-min_t)*3.0/4.0;


          int cluster_num = dsSize/core_clusterSize+1;
          double[] stddev = new double[cluster_num];
          double[] rev_stddev = new double[cluster_num];

          int index = 0;
          double t;
          for(int i=0; i<cluster_num; i++)
          {
               t = 0;
               for(int j=(i-1)*core_clusterSize; j<i*core_clusterSize; j++)
               {
                    index = index_ZPD + j;
                    t += (ifgm[index]-mean_t)*(ifgm[index] - mean_t);
               }
               t = Math.sqrt(t/core_clusterSize);
               stddev[i] = t;

               t = 0;
               for(int j=(i-1)*core_clusterSize; j<i*core_clusterSize; j++)
               {
                    index = index_ZPD - j;
                    t += (ifgm[index]-mean_t)*(ifgm[index] - mean_t);
               }
               t = Math.sqrt(t/core_clusterSize);
               rev_stddev[i] = t;
          }

          double max_stddev;

          if(stddev[0]>stddev[1]) 
               max_stddev = stddev[0];
          else        
               max_stddev = stddev[1];

          double core_length = core_clusterSize*(cluster_num-1);
          for(int i=2; i<cluster_num; i++)
          {
              if(stddev[i] < 0.1*max_stddev)
              {
                   core_length = core_clusterSize * (i-1);
                   break;
              }
          }

          double rev_core_length = core_clusterSize*(cluster_num-1);
          for(int i=2; i<cluster_num; i++)
          {
              if(rev_stddev[i] < 0.1*max_stddev)
              {
                   rev_core_length = core_clusterSize * (i-1);
                   break;
              }
          }

          double cutoff_freq;
          if(core_length < rev_core_length) 
                cutoff_freq = 10.0/core_length;
          else   
                cutoff_freq = 10.0/rev_core_length;

          for(int i=-dsSize+1+index_ZPD; i<dsSize+index_ZPD; i++)
          {
               ifgm[i] = core_cutoff(i-index_ZPD, ifgm[i], mean_t,
                              cutoff_amp, cutoff_freq);
          }
     }
}
