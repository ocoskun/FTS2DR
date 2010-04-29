package test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class TestBinary
{

     int arrayWidth, arrayLength, nPoints_Spectrum, dataType;

     double[][][] spectrum = null;
     double[] wn = null;

     void loadSpectrum(String spectrumFile)
     {
           try
           {
                FileInputStream inputStream = new FileInputStream(spectrumFile);
                DataInputStream dis = new DataInputStream(inputStream);

                arrayWidth = dis.readInt();
                arrayLength = dis.readInt();
                nPoints_Spectrum = dis.readInt();
                dataType = dis.readInt();

                wn = new double[nPoints_Spectrum];
                spectrum = new double[arrayWidth][arrayLength][nPoints_Spectrum];
           
                for(int k = 0; k < nPoints_Spectrum; k++)
                {
                    wn[k] = dis.readDouble();
                    for(int j=0; j< arrayLength; j++)
                      for(int i=0; i< arrayWidth; i++)
                      {
                           spectrum[i][j][k] = dis.readDouble();
                      }
                }
                inputStream.close();
           }
           catch(IOException e)
           {
                e.printStackTrace();
           }
     }

     public static void main(String[] args)
     {
           String inFile = "xyz";
           if(args.length != 0)
           {
               inFile = args[0];
           }
           System.out.println("MakeBinary: IN = " + inFile);
           TestBinary tb = new TestBinary();

           tb.loadSpectrum(inFile);
           for(int i=0; i< tb.wn.length; i++)
               System.out.println(tb.wn[i] + " " + tb.spectrum[0][0][i]);
     }
}
