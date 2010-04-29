package test;

import uk.ac.starlink.hds.HDSObject;
import uk.ac.starlink.hds.HDSException;
import java.util.StringTokenizer;
import java.io.*;

/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public class TestSpectrum 
{

     Object   ifgm_cube;
     String   ifgm_cubeType;
     long[] ifgm_cubeShape;
     long ifgm_cubeSize;
     int arrayWidth;
     int arrayLength;
     int nPoints_Ifgm;

     HDSObject hdsInterferogram;

     /**
      * Constructor
      * @param interferogramFile the absolute path of an interferogram file for data reduction
      */ 
     public TestSpectrum(String interferogramFile) 
     {
          try
          {
              /* get an HDSObject pointing to the interferogram file */
              hdsInterferogram = HDSObject.hdsOpen(interferogramFile, "READ");

              /* get the dimensions and data type of about interferogram cube and an pointer to the actual
                 interferogram cube. 
              */
              HDSObject hdsData = hdsInterferogram.datFind("DATA_ARRAY").datFind("DATA");
              if(hdsData != null)
              {
                  ifgm_cubeShape = hdsData.datShape();
                  arrayWidth = (int)ifgm_cubeShape[0];
                  arrayLength = (int)ifgm_cubeShape[1];
                  nPoints_Ifgm = (int)ifgm_cubeShape[2];

                  ifgm_cubeSize = hdsData.datSize();
                  ifgm_cubeType = hdsData.datType();

                  if(ifgm_cubeType.equals("_UWORD")) ifgm_cube = hdsData.datGetvi();
                  else if(ifgm_cubeType.equals("_WORD")) ifgm_cube = hdsData.datGetvi();
                  else if(ifgm_cubeType.equals("_INTEGER")) ifgm_cube = hdsData.datGetvi();
                  else if(ifgm_cubeType.equals("_REAL")) ifgm_cube = hdsData.datGetvr();
                  else if(ifgm_cubeType.equals("_DOUBLE")) ifgm_cube = hdsData.datGetvd();
              }
          }
          catch(HDSException e)
          {
              System.out.println(e);
          }
     }

     /**
      * get the mirror position from the interferogram file
      */ 
     public double[] getMirrorPos()
     {
          try
          {
              HDSObject hdsPos;
              hdsPos = hdsInterferogram.datFind("MORE").datFind("FRAMEDATA").datFind("FTS_POS");
              if(hdsPos.datType().equals("_REAL")) 
              {
                   float[] pos = hdsPos.datGetvr();
                   double[] ftsPos = new double[pos.length];
                   for(int i=0;i<pos.length;i++) ftsPos[i] = pos[i];
                   return ftsPos;
              }
              if(hdsPos.datType().equals("_DOUBLE")) return hdsPos.datGetvd();
          }
          catch(HDSException e)
          {
              System.out.println(e);
          }
          return null;
     }
     /**
      * get the whole interferogram cube as 1-D data array stored in Fortran format
      */
     public Object getInterferogram()
     {
          return ifgm_cube;
     }
     /**
      * get the data type of the interferogram cube. Possible values:
      * _UWORD, _WORD, _INTEGER, _REAL, _DOUBLE.
      */
     public String get_ifgmType()
     {
         return ifgm_cubeType;
     }
     /**
      *  get the dimension of the interferogram cube.
      */
     public long[] get_ifgmCubeShape()
     {
         return ifgm_cubeShape;
     }
     /**
      * get the total number of the data in the interferogram cube.
      */
     public long get_ifgmCubeSize()
     {
         return ifgm_cubeSize;
     }
     /**
      *  get the size of the array in x-axis
      */
     public int get_arrayWidth()
     {
         return arrayWidth;
     }
     /**
      * the size of the array in y-axis
      */
     public int get_arrayLength()
     {
           return arrayLength;
     }
     /**
      * get the number of the data of one interferogram
      */
     public int get_npoints_ifgm()
     {
          return nPoints_Ifgm;
     }

     /**
      * get the specified inteferogram
      * @param indexOfWidth the index of the array pixel in x-axis starting from 0.
      * @param indexOfLength the index of the array pixel in y-axis starting from 0.
      */
     public double[] getInterferogram(int indexOfWidth, int indexOfLength)
     {
          int arraySize = arrayWidth * arrayLength;
          int index; 
          index = indexOfWidth + indexOfLength * arrayWidth;
          if(ifgm_cubeType.equals("_UWORD")  
             || ifgm_cubeType.equals("_WORD")
             || ifgm_cubeType.equals("_INTEGER"))
          { 
              double[] single_ifgm = new double[nPoints_Ifgm];
              int[] cube_ifgm = (int[])ifgm_cube;
              for(int k = 0; k < nPoints_Ifgm; k++)
              {
                  single_ifgm[k] = cube_ifgm[index];
                  index += arraySize;
              }
              return single_ifgm;
          }
          else if(ifgm_cubeType.equals("_REAL")) 
          {
              double[] single_ifgm = new double[nPoints_Ifgm];
              float[] cube_ifgm = (float[])ifgm_cube;
              for(int k = 0; k < nPoints_Ifgm; k++)
              {
                  single_ifgm[k] = cube_ifgm[index];
                  index += arraySize;
              }
              return single_ifgm;
          }
          else if(ifgm_cubeType.equals("_DOUBLE"))
          {
              double[] single_ifgm = new double[nPoints_Ifgm];

              double[] cube_ifgm = (double[])ifgm_cube;
              for(int k = 0; k < nPoints_Ifgm; k++)
              {
                  single_ifgm[k] = cube_ifgm[index];
                  index += arraySize;
              }
              return single_ifgm;
          }
          return null;
     }

     public static void main(String[] args)
     {
           String infile = "abc0";
           if(args.length != 0) infile = args[0];
           System.out.println("TestSpectrum: IN = " + infile);
           TestSpectrum ts = new TestSpectrum(infile);
           String outfile;

           String dir_data = "data";
           File data = new File("./" + dir_data);
           if(data.exists())
           {
                if(!data.isDirectory())
                {
                    System.out.println("\"data\" is not a directory!!!");
                    System.exit(0);
                }
           }
           else
           {
                if(!data.mkdir())
                {
                    System.out.println("can not create subdirectory \"data\"!!!");
                    System.exit(0);
                }
           }

           for(int ii=0; ii<3; ii++)
           {
              System.out.println("row = " + ii);
              for(int jj=0; jj<10; jj++)
              {
                   if(ii<10) outfile = "x0" + ii + "_";
                   else      outfile = "x"  + ii + "_";
                   if(jj<10) outfile += ("0" + jj + ".dat");
                   else      outfile += (      jj + ".dat");

                   outfile = dir_data + "/" + outfile;

                   try
                   {
                         String str;
                                  
                         BufferedWriter bd0 = new BufferedWriter(new FileWriter(outfile));
                         double[] spectrum0 = ts.getInterferogram(ii, jj); 
                         for(int i=0; i<spectrum0.length; i++)
                         {
                              str = Double.toString(Math.abs(spectrum0[i])) + "\n";
                              bd0.write(str, 0, str.length());
                         }
                         bd0.close();
                   }
                   catch(IOException e)
                   {
                        e.printStackTrace();
                   }
              }
           }
     }
}
