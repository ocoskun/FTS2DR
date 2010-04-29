package ca.uol.aig.fts.io;

import ca.uol.aig.fts.io.DataIO;
import java.io.*;

/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public class BinaryIO extends ca.uol.aig.fts.io.DataIO
{
     int arrayWidth;
     int arrayLength;
     int nPoints_Ifgm;
     double[][][] ifgmCube = null;
     double[] opd = null;

     String spectrumFile;

     /**
      * Initializer.
      * @param ioParams the parameters related to the In/Out data files. The first one is the 
      * absolute path of an interferogram file for data reduction; the second one is
      * the absolute path of the spectrum file from data reduction.
      * <br>
      * Note: no extension for these two data files. Their default extension is .sdf.
      */ 
     public void init(Object[] ioParams)
     {
           String interferogramFile = (String)ioParams[0];
           this.spectrumFile = (String)ioParams[1];

           try
           {
               FileInputStream inputStream = new FileInputStream(interferogramFile);
               DataInputStream dis = new DataInputStream(inputStream);

               arrayWidth = dis.readInt();
               arrayLength = dis.readInt();
               nPoints_Ifgm = dis.readInt();

               int dataType = dis.readInt();

               opd = new double[nPoints_Ifgm];
               ifgmCube = new double[arrayWidth][arrayLength][nPoints_Ifgm];

               if(dataType == 4)
               {
                    for(int i=0; i<nPoints_Ifgm; i++)
                    {
                         opd[i] = dis.readFloat();
                         for(int j=0; j<arrayWidth; j++)
                         for(int k=0; k<arrayLength; k++)
                         {
                              ifgmCube[j][k][i] = dis.readFloat();
                         }
                     }
               }
               else if(dataType == 8)
               {
                    for(int i=0; i<nPoints_Ifgm; i++)
                    {
                         opd[i] = dis.readDouble();
                         for(int j=0; j<arrayWidth; j++)
                         for(int k=0; k<arrayLength; k++)
                         {
                              ifgmCube[j][k][i] = dis.readDouble();
                         }
                     }
               }
               else
               {
                     System.err.println("data type is not supported yet!");
                     System.exit(0);
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
     public int get_nPoints_Ifgm()
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
      * @param wn_unit the unit of the wavenumbers.
      */
     public void saveSpectrum(Object spectrumCube, double wn_unit)
     {
          double[][][] spectrum = (double[][][])spectrumCube;
          int nPoints_Spectrum = spectrum[0][0].length;

          String str = null;
          try
          {
              FileOutputStream outputStream = new FileOutputStream(spectrumFile);
              DataOutputStream dos = new DataOutputStream(outputStream);


              dos.writeInt(arrayWidth);
              dos.writeInt(arrayLength);
              dos.writeInt(nPoints_Spectrum);
              dos.writeInt((int)8);

              for(int i=0; i<nPoints_Spectrum; i++)
              {
                    dos.writeDouble(i*wn_unit/(nPoints_Spectrum-1));

                    for(int j=0; j<arrayWidth; j++)
                      for(int k=0; k<arrayLength; k++)
                      {
                          dos.writeDouble(spectrum[j][k][i]);
                      }
              }
              outputStream.flush();
              outputStream.close();
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
