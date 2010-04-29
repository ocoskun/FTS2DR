package ca.uol.aig.fts.io;

import ca.uol.aig.fts.io.DataIO;
import java.io.*;

/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public class PlainTextIO extends ca.uol.aig.fts.io.DataIO
{
     int arrayWidth;
     int arrayLength;
     int nPoints_Ifgm;
     double[][][] ifgmCube = null;
     double[] opd = null;
     BufferedWriter outputStream;

     /**
      * Initializer.
      * @param interferogramFile the absolute path of an interferogram file for data reduction.
      * @param spectrumFile      the absolute path of the spectrum file from data reduction.
      * <br>
      * Note: no extension for these two data files. Their default extension is .sdf.
      */ 
     public void init(String interferogramFile, String spectrumFile)
     {
           try
           {
               BufferedReader inputStream = new BufferedReader(new FileReader(interferogramFile));
               outputStream = new BufferedWriter(new FileWriter(spectrumFile));  
               String inLine;
               String[] str;
               inLine = inputStream.readLine();
               str = inLine.split(" ");
               arrayWidth = Integer.parseInt(str[0]);
               arrayLength = Integer.parseInt(str[1]);
               nPoints_Ifgm = Integer.parseInt(str[2]);

               opd = new double[nPoints_Ifgm];
               ifgmCube = new double[arrayWidth][arrayLength][nPoints_Ifgm];
               for(int i=0; i<nPoints_Ifgm; i++)
               {
                    inLine = inputStream.readLine();
                    str = inLine.split(" ");
                    opd[i] = Double.parseDouble(str[0]);
                    int index = 1;
                    for(int j=0; j<arrayWidth; j++)
                       for(int k=0; k<arrayLength; k++)
                       {
                           ifgmCube[j][k][i] = Double.parseDouble(str[index]);
                           index++;
                       }
               }
               inputStream.close();
           }
           catch(IOException e)
           {
               e.printStackTrace();
           }
     }

     /**
      * get the mirror position or the OPD from the interferogram file.
      */ 
     public double[] getOPD()
     {
          return opd;
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
     public int get_npoints_ifgm()
     {
          return nPoints_Ifgm;
     }

     /**
      * get the specified inteferogram.
      * @param indexOfWidth the index of the array pixel in x-axis starting from 0.
      * @param indexOfLength the index of the array pixel in y-axis starting from 0.
      */
     public double[] getInterferogram(int indexOfWidth, int indexOfLength)
     {
          double[] single_ifgm = new double[nPoints_Ifgm];
          for(int i=0; i<nPoints_Ifgm; i++) single_ifgm[i] = ifgmCube[indexOfWidth][indexOfLength][i];
          return single_ifgm;
     }

     /**
      * save the spectrum cube to the spectrum file.
      * @param spectrumCube the spectrum cube.
      */
     public void saveSpectrum(Object spectrumCube)
     {
          double[][][] spectrum = (double[][][])spectrumCube;
          int nPoints_Spectrum = spectrum[0][0].length;
          String str = null;
          try
          {
              for(int i=0; i<nPoints_Spectrum; i++)
              {
                    outputStream.write(i + " ");

                    for(int j=0; j<arrayWidth; j++)
                      for(int k=0; k<arrayLength; k++)
                      {
                          outputStream.write(spectrum[j][k][i] + " ");
                      }
                    outputStream.write("\n");
              }
          }
          catch(IOException e)
          {
               e.printStackTrace();
          }
     }

     /**
      * close the handle of the spectrum file.
      */
     public void closeSpectrum()
     {
     }
}
