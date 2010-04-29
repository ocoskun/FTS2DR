package test;

import herschel.ia.dataset.Product;
import herschel.ia.io.fits.FitsArchive;
/*
import herschel.spire.ia.dataset.SpireInterferogram1d;
import herschel.spire.ia.dataset.SpireInterferogramCompositeDataset;
import herschel.spire.ia.dataset.SpectrometerDetectorInterferogram;
 */
import java.io.IOException;
import java.util.Random;

public class MakeSpire 
{
     int scanNum = 4;
     String[] pixelName = { "SLWA1", "SSWC1", "SSWE3", "SLWE3", "SSWC2", "SLWC3", 
                            "SSWN2", "SSWE4", "SSWA3", "SSWG3", "SLWC2", "SSWN3",
                            "SSWN1", "SSWF2", "SSWC4", "SSWG1", "SSWE2", "SSWD7",
                            "SSWN6", "SSWT1", "SSWC6", "SSWD2", "SSWDP1", "SLWE2", 
                            "SLWT2", "SSWB2", "SSWF1", "SSWC3", "SSWE5", "SSWD4",
                            "SLWT1", "SSWR1", "SSWB3", "SLWD3", "SSWG4", "SSWD5",
                            "SLWB3", "SLWD2", "SLWD4", "SSWA2", "SSWB1", "SSWD6",
                            "SSWF5", "SLWB2", "SLWD1", "SSWA1", "SSWDP2", "SLWR1",
                            "SSWD3", "SSWB4", "SLWC1", "SLWB1", "SLWE1", "SSWE1",
                            "SLWB4", "SSWE6", "SLWDP2", "SSWD1", "SSWT2", "SSWF4",
                            "SLWC4", "SSWF3", "SSWB5", "SLWA2", "SSWG2", "SSWC5",
                            "SLWDP1", "SSWN4", "SLWA3", "SSWA4", "SSWN5", "SLWC5"};
     double x_interval = 0.1;

     /**
      * save the spectrum cube to the spectrum file
      * @param spectrum the spectrum cube in 1-D Fortran format
      * @param pos      the mirror positions
      */
     public void saveInterferogram(double[] spectrum, double[] pos)
     {
     }
     /**
      * close the handle of the spectrum file
      */

     void createSpire(double[][][] ifgm_cube, double[][] pos, String outfile)
     {
         Product sdi = null;
/*         
          SpectrometerDetectorInterferogram sdi= new SpectrometerDetectorInterferogram();

          for(int j=0; j<scanNum; j++)
          {
             SpireInterferogramCompositeDataset scan = new SpireInterferogramCompositeDataset();
             for(int i=0; i<pixelName.length; i++)
             {
                 SpireInterferogram1d pixel = new SpireInterferogram1d();
                 pixel.setPixelName(pixelName[i]);
                 pixel.setOpd(new Double1d(pos[j]));
                 pixel.setSignal(new Double1d(ifgm_cube[j][i]));
                 scan.setPixel(pixel);
             }
             sdi.set(String.format("%1$04d", j+1), scan);
          }
*/
          FitsArchive fa_ifgm = new FitsArchive();
          try
          {
              fa_ifgm.save(outfile, sdi);
          }
          catch(IOException e)
          {
               e.printStackTrace();
          }
     }

     void createTestData(String outfile)
     {
           final int DS_LEN  = 400;
           final int SS_LEN = 5000;
           final int PCF_LEN = 80;

           final int nPoints_Ifgm = DS_LEN + SS_LEN + PCF_LEN;
           final int LEFT_SHIFT = DS_LEN;

           final double NS_Ratio = 0.0;
           final double POS_FLUC = 0.1;
           final double d0 = 0.1*(2.0*Math.PI);
           final double d1 = 1.7; 
           final double d2 = 2.5;
           
           final double glitch_starting = 250;
           final double glitch_width = 0;
           final double glitch_endding = glitch_starting+glitch_width;
 
           boolean Reverse = true;

//         Random gen_rand = new Random(199500);

           double[][] pos = new double[scanNum][nPoints_Ifgm];

           double[][][] ifgm_cube = new double[scanNum][pixelName.length][nPoints_Ifgm];

           double[][] pos0 = new double[scanNum][nPoints_Ifgm];
           double[] ifgm0 = new double[nPoints_Ifgm];
           double[] ifgm1 = new double[nPoints_Ifgm];

           for(int j=0; j<scanNum; j++)
           {
               double max_rand_coeff = 0.0D;

               if(j%2 == 0) Reverse = false;
               else Reverse = true;

               for(int k=0; k<nPoints_Ifgm; k++)
               {
                   double x, x0, a1, f1, p1, aa, bb, cc, total, rand_coeff;
                   double aa0, bb0, cc0, total0;

                   Random gen_rand = new Random(199500 + k *100 + j*1789);

                   x0 = (double)(k - LEFT_SHIFT);
                   if(k==0 || k==nPoints_Ifgm-1)
                        x = x0;
                   else
                        x = x0 + POS_FLUC*(gen_rand.nextDouble()-0.5);

                   if(Reverse)
                   {
                       pos[j][nPoints_Ifgm-k-1] = (float)(x * x_interval);
                       pos0[j][nPoints_Ifgm-k-1] = (float)(x0 * x_interval);
                   }
                   else
                   {
                       pos[j][k] = (float)(x * x_interval);
                       pos0[j][k] = (float)(x0 * x_interval);
                   }


                   double fac;
                   int fn;

                   aa = 0;
                   aa0 = 0;

                   fac = 1;
                   fn = (int)((800-300)/fac);
                   for(int jjj=0;jjj<fn;jjj++)
                   {  
                       f1 = (300+jjj*fac) * Math.PI / SS_LEN;
                       p1 = d0 + d1 * f1 + d2 * f1 *f1;
                       a1 = (jjj+1)/30.0+5;
                       aa += a1*Math.cos(f1*x + p1);
                       aa0 += a1*Math.cos(f1*x0);
                   }
                
                   fac = 1.5;
                   fn = (int)((3000-2000)/fac);
                   for(int jjj=0;jjj<fn;jjj++)
                   {
                       f1 = (2000+jjj*fac) * Math.PI / SS_LEN;
                       p1 = d0 + d1 * f1 + d2 * f1 *f1;
                       a1 = (jjj+1)/60.0+5;
                       aa += a1*Math.cos(f1*x + p1);
                       aa0 += a1*Math.cos(f1*x0);
                   }

                   bb = 0;
                   bb0 = 0;

                   fac = 1;
                   fn = (int)((1800-1000)/fac);
                   for(int jjj=0;jjj<fn;jjj++)
                   {
                       f1 = (1000+jjj*fac) * Math.PI / SS_LEN;
                       p1 = d0 + d1 * f1 + d2 * f1 *f1;
                       a1 = (jjj+1-fn/2)/30.0;
                       a1 = (200-a1*a1)/15.0+8;
                       bb += a1*Math.cos(f1*x + p1);
                       bb0 += a1*Math.cos(f1*x0);
                   }

                   cc = 0;
                   cc0 = 0;
                   fac = 95;
                   fn = (int)(2000/fac);
                   for(int jjj=0;jjj<fn;jjj++)
                   {
                       f1 = (0+jjj*fac) * Math.PI / SS_LEN;
                       p1 = d0 + d1 * f1 + d2 * f1 *f1;
                       a1 = 5.0;
                       cc += a1*Math.cos(f1*x + p1);
                       cc0 += a1*Math.cos(f1*x0);
                   }

                   double glitch_err = 0, glitch_err0 = 0;
                   fac = 0.5;
                   fn = (int)((5000-1000)/fac);

                   if(x>=glitch_starting && x<=glitch_endding)
                   {
                       for(int jjj=0;jjj<fn;jjj++)
                       {
                          f1 = (500+jjj*fac) * Math.PI / SS_LEN;
                          p1 = d0 + d1 * f1 + d2 * f1 *f1;
                          a1 = 30*f1*f1;
                          glitch_err += a1*Math.cos(f1*x + p1);
                          glitch_err0 += a1*Math.cos(f1*x0);
                       }
                   }


                   total0 = aa0 + bb0 + cc0 + glitch_err0;
                   if(Reverse)
                      ifgm0[nPoints_Ifgm-k-1] = (float)total0;
                   else
                      ifgm0[k] = (float)total0;

                   total = aa + bb + cc + glitch_err;
/*
                   rand_coeff = NS_Ratio*(gen_rand.nextDouble());
                   total += rand_coeff*total;
*/
                   if(Reverse)
                      ifgm1[nPoints_Ifgm-k-1] = (float)total;
                   else
                      ifgm1[k] = (float)total;

                   double max_coeff_x = 0.0D;
                   for(int i=0; i<pixelName.length; i++)
                   {
                       
                       rand_coeff = NS_Ratio*(gen_rand.nextDouble());
                       if(rand_coeff > max_coeff_x)
                       {
                           max_coeff_x = rand_coeff;
                       }
                       double totalx = total + rand_coeff*total;

                       if(Reverse)
                           ifgm_cube[j][i][nPoints_Ifgm-k-1] = (float)totalx;
                       else
                           ifgm_cube[j][i][k] = (float)totalx;
                   }
                   if(max_coeff_x > max_rand_coeff)
                   {
                       max_rand_coeff = max_coeff_x;
                   }
//                 System.out.println(k + ":" + "max_coeff_x = " + max_coeff_x);
               }

               System.out.println(">>>> max_rand_coeff = " + max_rand_coeff + " <<<<");
           }
           createSpire(ifgm_cube, pos, outfile);
     }

     public static void main(String[] args)
     {
           String outfile = "xyz.fits";
           if(args.length != 0)
           {
               outfile = args[0];
           }
           System.out.println("MakeSpire: OUT = " + outfile);

           MakeSpire ms = new MakeSpire();

           ms.createTestData(outfile);
     }
}
