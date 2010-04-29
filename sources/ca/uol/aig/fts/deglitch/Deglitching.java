package ca.uol.aig.fts.deglitch;

import java.util.Arrays;

/**
 * De-glitching
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */

public class Deglitching
{
     final int clusterSize = 60;
     final double cutoff_StdDev_percent = 0.80;
     final double cutoff_StdDev_multiplier = 6;

     int tail_starting = 0;
     /**
      * Constructor.
      * @param tail_starting  From tail_starting to the end of an interferogram is the tail part.
      */
     public Deglitching(int tail_starting)
     {
           this.tail_starting = tail_starting;
     }

     /**
      * deglitch an interferogram.
      * @param intensity an interferogram.
      */

     public void deglitch(double[] intensity)
     {
           int numData = intensity.length;

           int deglitching_start = tail_starting;

           int numCluster = (numData - deglitching_start)/clusterSize;
           double[] clusterMean = new double[numCluster];
           double[] clusterDev  = new double[numCluster];

           double[] mean_stddev = new double[2];

           for(int i=0; i<numCluster; i++)
           {
               getMean_StdDev(intensity, deglitching_start+i*clusterSize, mean_stddev);
               clusterMean[i] = mean_stddev[0];
               clusterDev[i] = mean_stddev[1];
           }
           Arrays.sort(clusterDev);         

           double ave_stddev = 0;
           int count = 0;
           for(int i=0; i<(clusterDev.length*cutoff_StdDev_percent); i++) 
           {
                  ave_stddev += clusterDev[i];
                  count++; 
           }
           ave_stddev /= count;

           double cutoff_stddev = ave_stddev*cutoff_StdDev_multiplier;
           int starting_point = 0;
           double mean_t = 0;
           for(int i=0; i<numCluster; i++)
           {
               mean_t = clusterMean[i];
               starting_point = deglitching_start+i*clusterSize;
               for(int j=starting_point; j<starting_point+clusterSize; j++)
               {
                    if(intensity[j]>mean_t+cutoff_stddev) intensity[j] = mean_t+cutoff_stddev;
                    if(intensity[j]<mean_t-cutoff_stddev) intensity[j] = mean_t-cutoff_stddev;
               }
           }
           starting_point = deglitching_start+numCluster*clusterSize;

           if(starting_point < numData)
           {
               mean_t = 0.0;
               count = 0;
               for(int j=starting_point; j<numData; j++)
               {
                    mean_t += intensity[j];
                    count++;
               }
               mean_t /= count;
               for(int j=starting_point; j<numData; j++)
               {
                  if(intensity[j]>mean_t+cutoff_stddev) intensity[j] = mean_t+cutoff_stddev;
                  if(intensity[j]<mean_t-cutoff_stddev) intensity[j] = mean_t-cutoff_stddev;
               }
           }
     }
     void getMean_StdDev(double[] intensity, int starting_point, double[] mean_stddev)
     {
          double t = 0;
          double mean = 0; double stddev = 0;

          t = 0;
          for(int i=starting_point; i<starting_point+clusterSize; i++)
          {
               t += intensity[i];
          }
          mean = t / clusterSize;

          t = 0;
          for(int i=starting_point; i<starting_point+clusterSize; i++)
          {
               t += (intensity[i]-mean)*(intensity[i]-mean);
          }
          stddev = Math.sqrt(t/clusterSize);

          mean_stddev[0] = mean;
          mean_stddev[1] = stddev;
     }
}
