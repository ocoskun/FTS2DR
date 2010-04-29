package ca.uol.aig.fts.io;

import uk.ac.starlink.hds.HDSObject;
import uk.ac.starlink.hds.HDSException;
import java.util.StringTokenizer;
/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public class Scuba2NDFIO extends ca.uol.aig.fts.io.DataIO
{
     Object   ifgm_cube;
     String   ifgm_cubeType;
     long[] ifgm_cubeShape;
     long ifgm_cubeSize;
     int arrayWidth;
     int arrayLength;
     int nPoints_Ifgm;

     HDSObject hdsInterferogram = null, hdsSpectrum = null;

     /**
      * Initializer.
      * @param ioParams the parameters related to the In/Out data file. The first one is
      * the absolute path of an interferogram file for data reduction; the second one is
      * the absolute path of the spectrum file from data reduction.
      * <br>
      * Note: no extension for these two data files. Their default extension is .sdf.
      */ 
     public void init(Object[] ioParams)
     {
          String interferogramFile = (String)ioParams[0];
          String spectrumFile = (String)ioParams[1];
          try
          {
              /* get an HDSObject pointing to the interferogram file */
              hdsInterferogram = HDSObject.hdsOpen(interferogramFile, "READ");

              /* create a new spectrum NDF file. */
              long[] dims = new long[0];

              if(spectrumFile != null)
              {
                   /* get the filename of the raw data file */
                   String spectrumFile_rel = spectrumFile.substring(spectrumFile.lastIndexOf("/")+1, 
                                                               spectrumFile.length());
                   hdsSpectrum = HDSObject.hdsNew(spectrumFile, spectrumFile_rel, "NDF", dims);

                   /* create a structure 'DATA_ARRAY' in this file. */
                   hdsSpectrum.datNew("DATA_ARRAY", "ARRAY", dims);

                   /* if the structure 'MORE' exists in the interferogram file, save this structure to
                     the spectrum NDF file.
                   */
                   if(hdsInterferogram.datThere("MORE"))
                   {
                       HDSObject hdsMore;
                       hdsMore = hdsInterferogram.datFind("MORE");
                       hdsMore.datCopy(hdsSpectrum, "MORE");
                   }
              }

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
      * get the mirror position or the OPD from the interferogram file.
      */ 
     public Object getOPD()
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
      * get the data type of the interferogram cube. Possible values:
      * _UWORD, _WORD, _INTEGER, _REAL, _DOUBLE.
      */
     public String get_ifgmDataType()
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
      *  get the size of the array in x-axis.
      */
     public int get_arrayWidth()
     {
         return arrayWidth;
     }
     /**
      * get the size of the array in y-axis.
      */
     public int get_arrayLength()
     {
           return arrayLength;
     }
     /**
      * get the number of the data of one interferogram.
      */
     public int get_nPoints_Ifgm()
     {
          return nPoints_Ifgm;
     }

     /**
      * get the whole interferogram cube as 1-D data array stored in Fortran format.
      */
     public Object getInterferogram()
     {
          return ifgm_cube;
     }

     /**
      * get the specified inteferogram.
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

     /**
      * save the spectrum cube to the spectrum file.
      * @param spectrumCube the spectrum cube.
      * @param wn_unit the unit of the wavenumbers.
      */
     public void saveSpectrum(Object spectrumCube, double wn_unit)
     {
         try
         {
            //  int nPoints_Ifgm = 0;
              if(spectrumCube instanceof float[][][])
              {
                   float[][][] cube_spectrum = (float[][][])spectrumCube;

                   long[] dims = new long[3];
                   dims[0] = cube_spectrum.length;
                   dims[1] = cube_spectrum[0].length;
                   dims[2] = cube_spectrum[0][0].length;

                   nPoints_Ifgm = (int)dims[2];

                   int cubeSize = (int)(dims[0] * dims[1] * dims[2]);

                   float[] spectrum = new float[cubeSize];
                   int index = 0;
                   /* convert the spectrum cube from 3-d data format to 1-d data format */
                   for(int k=0; k<dims[2]; k++)
                     for(int j=0; j<dims[1]; j++)
                       for(int i=0; i<dims[0]; i++)
                       {
                           spectrum[index] = cube_spectrum[i][j][k];
                           index++;
                       }

                   hdsSpectrum.datFind("DATA_ARRAY").datNew("DATA", "_REAL", dims);
                   hdsSpectrum.datFind("DATA_ARRAY").datFind("DATA").datPutvr(spectrum);
              }
              else if(spectrumCube instanceof double[][][])
              {
                   double[][][] cube_spectrum = (double[][][])spectrumCube;

                   long[] dims = new long[3];
                   dims[0] = cube_spectrum.length;
                   dims[1] = cube_spectrum[0].length;
                   dims[2] = cube_spectrum[0][0].length;

                   nPoints_Ifgm = (int)dims[2];

                   int cubeSize = (int)(dims[0] * dims[1] * dims[2]);

                   float[] spectrum = new float[cubeSize];
                   int index = 0;
                   /* convert the spectrum cube from 3-d data format to 1-d data format */
                   for(int k=0; k<dims[2]; k++)
                     for(int j=0; j<dims[1]; j++)
                       for(int i=0; i<dims[0]; i++)
                       {
                           spectrum[index] = (float)cube_spectrum[i][j][k];
                           index++;
                       }
                   hdsSpectrum.datFind("DATA_ARRAY").datNew("DATA", "_REAL", dims);
                   hdsSpectrum.datFind("DATA_ARRAY").datFind("DATA").datPutvr(spectrum);
/*
                   hdsSpectrum.datFind("DATA_ARRAY").datNew("DATA", "_DOUBLE", dims);
                   hdsSpectrum.datFind("DATA_ARRAY").datFind("DATA").datPutvd((double[])spectrum);
*/
              }
              else
              {
                   System.out.println("The data type is not supported! And the data is not saved!!!");
              }

              double[] waveNumber = new double[nPoints_Ifgm];
              long[] wnDims = new long[1];
              wnDims[0] = nPoints_Ifgm;
              for(int i=0; i<nPoints_Ifgm; i++)
                  waveNumber[i] = i*wn_unit/(nPoints_Ifgm-1);
              hdsSpectrum.datFind("MORE").datFind("FRAMEDATA").datErase("FTS_POS");
              hdsSpectrum.datFind("MORE").datFind("FRAMEDATA").datNew("FTS_WN", "_DOUBLE", wnDims);
              hdsSpectrum.datFind("MORE").datFind("FRAMEDATA").datFind("FTS_WN").datPutvd(waveNumber);
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
     }

     /**
      * close the handle of the spectrum file.
      */
     public void closeSpectrum()
     {
         try
         {
              hdsSpectrum.datAnnul();
              hdsSpectrum = null;
         }
         catch(HDSException e)
         {
              System.out.println(e);
         }
     }

     /**
      * create a primitive data in the spectrum file.
      * @param compositeName the composite name of the new data separated by #, e.g., XYZ#ABC#XNAME.
      * @param obj  the value of this new data. Possible data types: String, int, double.
      */ 
     public void newDataToSpectrum(String compositeName, Object obj)
     {
          StringTokenizer st = new StringTokenizer(compositeName, "#");
          String elementName;
          String data_type = obj.getClass().getName();
          try
          {
              HDSObject hdsMore;
              long[] dims = new long[0];
              hdsMore = hdsSpectrum.datFind("MORE");
              elementName = "FRAMEDATA";
              do
              {
                   hdsMore = hdsMore.datFind(elementName);
                   elementName = st.nextToken();
              }while(st.hasMoreTokens());

              if(data_type.equals("java.lang.Integer"))
              {
                   hdsMore.datNew(elementName, "_Integer", dims);
                   hdsMore.datFind(elementName).datPut0d((Integer)obj);
              }
              else if(data_type.equals("java.lang.Double"))
              {
                   hdsMore.datNew(elementName, "_DOUBLE", dims);
                   hdsMore.datFind(elementName).datPut0d((Double)obj);
              }
              else if(data_type.equals("java.lang.String"))
              {
                   String dataStr = (String)obj;
                   String char_len = ((Integer)((String)obj).length()).toString();
                   
                   hdsMore.datNew(elementName, "_CHAR*"+char_len, dims);
                   hdsMore.datFind(elementName).datPut0c((String)obj);
              }
          }
          catch(HDSException e)
          {
              System.out.println(e);
          }
     }
}
