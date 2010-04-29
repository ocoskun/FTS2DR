import uk.ac.starlink.hds.HDSObject;
import uk.ac.starlink.hds.HDSException;
import java.util.StringTokenizer;
import java.util.Random;
import java.io.*;

public class MakeNDF 
{

     int arrayWidth = 40;
     int arrayLength = 32;
     double x_interval = 0.1;

     HDSObject hdsInterferogram;

     public MakeNDF(String interferogramFile) 
     {
          try
          {
              /* create a new spectrum NDF file. */
              long[] dims = new long[0];
              String interferogramFile_rel = interferogramFile.substring(
                                  interferogramFile.lastIndexOf("/")+1, 
                                  interferogramFile.length());
              hdsInterferogram = HDSObject.hdsNew(interferogramFile, 
                                  interferogramFile_rel, "NDF", dims);

              /* create a structure 'DATA_ARRAY' in this file. */
              hdsInterferogram.datNew("DATA_ARRAY", "ARRAY", dims);
          }
          catch(HDSException e)
          {
              System.out.println(e);
          }
     }

     /**
      * get the mirror position from the interferogram file
      */ 

     public void saveMirrorPos(float[] pos)
     {
          try
          {
              long[] dims = new long[1],  dimx = new long[0];
              dims[0] = pos.length;

              HDSObject hdsPos;
              
              hdsInterferogram.datNew("MORE", "EXT", dimx); 
              hdsInterferogram.datFind("MORE").datNew("FRAMEDATA", "SCUBA2_FM_PAR", dimx);
              hdsInterferogram.datFind("MORE").datFind("FRAMEDATA").datNew("FTS_POS", "_REAL", dims);
              hdsInterferogram.datFind("MORE").datFind("FRAMEDATA").datFind("FTS_POS").datPutvr(pos);
          }
          catch(HDSException e)
          {
              System.out.println(e);
          }
     }


     /**
      * save the spectrum cube to the spectrum file
      * @param spectrum the spectrum cube in 1-D Fortran data order
      * @param dims     the dimension information of the spectrum
      */
     public void saveInterferogram(int[] spectrum, long[] dims)
     {
         try
         {
              hdsInterferogram.datFind("DATA_ARRAY").datNew("DATA", "_INTEGER", dims);
              hdsInterferogram.datFind("DATA_ARRAY").datFind("DATA").datPutvi(spectrum);
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
     }
     /**
      * save the spectrum cube to the spectrum file
      * @param spectrum the spectrum cube in 1-D Fortran data order
      * @param dims     the dimension information of the spectrum
      */
     public void saveInterferogram(float[] spectrum, long[] dims)
     {

         for(int i=0; i<dims.length; i++)
            System.out.println("dim[" + i + "]=" + dims[i]);

         try
         {
              hdsInterferogram.datFind("DATA_ARRAY").datNew("DATA", "_REAL", dims);
              hdsInterferogram.datFind("DATA_ARRAY").datFind("DATA").datPutvr(spectrum);
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
 
     }
     /**
      * save the spectrum cube to the spectrum file
      * @param spectrum the spectrum cube in 1-D Fortran format
      * @param dims     the dimension information of the spectrum
      */
     public void saveInterferogram(double[] spectrum, long[] dims)
     {
         try
         {
              hdsInterferogram.datFind("DATA_ARRAY").datNew("DATA", "_DOUBLE", dims);
              hdsInterferogram.datFind("DATA_ARRAY").datFind("DATA").datPutvd(spectrum);
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
     }
     /**
      * close the handle of the spectrum file
      */
     public void closeInterferogram()
     {
         try
         {
              hdsInterferogram.datAnnul();
              hdsInterferogram = null;
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
     }

     void createNDF(float[][][] ifgm_cube, float[] pos)
     {
           saveMirrorPos(pos);

           long[] ifgm_cubeShape = new long[3];
           ifgm_cubeShape[0] = ifgm_cube.length;
           ifgm_cubeShape[1] = ifgm_cube[0].length;
           ifgm_cubeShape[2] = ifgm_cube[0][0].length;


           long ifgm_cubeSize = ifgm_cubeShape[0]*ifgm_cubeShape[1]*ifgm_cubeShape[2];

           float[] ifgm = new float[(int)ifgm_cubeSize];

           int index = 0;
           for(int k = 0; k < ifgm_cube[0][0].length; k++)
              for(int j=0; j< ifgm_cube[0].length; j++)
                 for(int i=0; i< ifgm_cube.length; i++)
                 {
                      ifgm[index] = ifgm_cube[i][j][k];
                      index++;
                 }
           saveInterferogram(ifgm, ifgm_cubeShape);
           closeInterferogram();
     }

     void saveIfgm(float[] ifgm, float[] pos, String fileName)
     {
           String str;
           try
           {
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                for(int i=0; i<pos.length; i++)
                {
                    str = (pos[i] + " " + ifgm[i] + "\n");
                    bw.write(str, 0, str.length());
                }
                bw.close();
           }
           catch(IOException e)
           {
                e.printStackTrace();
           }
     }


     void createTestData()
     {

           final int DS_LEN  = 400;
           final int SS_LEN = 5000;
           final int PCF_LEN = 80;

           final int nPoints_Ifgm = DS_LEN + SS_LEN + PCF_LEN;
           final int LEFT_SHIFT = DS_LEN;

           final double NS_Ratio = 0.2;
           final double POS_FLUC = 0.1;
           final double d0 = 0.1*(2.0*Math.PI);
           final double d1 = 1.7; 
           final double d2 = 2.5;
           
           final double glitch_starting = 250;
           final double glitch_width = 20;
           final double glitch_endding = glitch_starting+glitch_width;
 
           final boolean Reverse = true;

//         Random gen_rand = new Random(199500);

           float[] pos = new float[nPoints_Ifgm];

           float[][][] ifgm_cube = new float[arrayWidth][arrayLength][nPoints_Ifgm];

           float[] pos0 = new float[nPoints_Ifgm];
           float[] ifgm0 = new float[nPoints_Ifgm];
           float[] ifgm1 = new float[nPoints_Ifgm];

           double max_rand_coeff = 0.0D;
           for(int k=0; k<nPoints_Ifgm; k++)
           {
                double x, x0, a1, f1, p1, aa, bb, cc, total, rand_coeff;
                double aa0, bb0, cc0, total0;

                Random gen_rand = new Random(199500 + k *100);

                x0 = (double)(k - LEFT_SHIFT);
                if(k==0 || k==nPoints_Ifgm-1)
                     x = x0;
                else
                     x = x0 + POS_FLUC*(gen_rand.nextDouble()-0.5);

                if(Reverse)
                {
                    pos[nPoints_Ifgm-k-1] = (float)(x * x_interval);
                    pos0[nPoints_Ifgm-k-1] = (float)(x0 * x_interval);
                }
                else
                {
                    pos[k] = (float)(x * x_interval);
                    pos0[k] = (float)(x0 * x_interval);
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
                for(int i=0; i<arrayWidth; i++)
                   for(int j=0; j<arrayLength; j++)
                   {
                       
                       rand_coeff = NS_Ratio*(gen_rand.nextDouble());
                       if(rand_coeff > max_coeff_x)
                       {
                            max_coeff_x = rand_coeff;
                       }
                       double totalx = total + rand_coeff*total;

                       if(Reverse)
                          ifgm_cube[i][j][nPoints_Ifgm-k-1] = (float)totalx;
                       else
                          ifgm_cube[i][j][k] = (float)totalx;
                   }
                if(max_coeff_x > max_rand_coeff)
                {
                     max_rand_coeff = max_coeff_x;
                }
//                System.out.println(k + ":" + "max_coeff_x = " + max_coeff_x);
           }
           System.out.println(">>>> max_rand_coeff = " + max_rand_coeff + " <<<<");

           saveIfgm(ifgm0, pos0, "fft_xy0.dat");
           saveIfgm(ifgm1, pos,  "fft_xy.dat");
           saveIfgm(ifgm_cube[0][0], pos, "fft_xy1.dat");

           createNDF(ifgm_cube, pos);
     }

     public static void main(String[] args)
     {
           String outfile = "xyz";
           if(args.length != 0)
           {
               outfile = args[0];
           }
           System.out.println("MakeNDF: OUT = " + outfile);
           MakeNDF mn = new MakeNDF(outfile);

           mn.createTestData();
     }
}
