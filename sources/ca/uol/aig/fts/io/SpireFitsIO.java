package ca.uol.aig.fts.io;

import ca.uol.aig.fts.io.DataIO;
import herschel.ia.io.fits.FitsArchive;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.CompositeDataset;
import herschel.ia.dataset.TableDataset;
import herschel.ia.dataset.Column;
import herschel.ia.numeric.Numeric1dData;
import herschel.ia.numeric.Double1d;
import java.util.Iterator;
import java.io.IOException;
/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public class SpireFitsIO extends ca.uol.aig.fts.io.DataIO
{
     int arrayWidth;
     int arrayLength;
     int nPoints_Ifgm;
     double[][][] ifgmCube = null;
     double[] opd = null;
     String[] pixels_Name = null;
     String scanName = null;

     String spectrumFile = null;

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
          this.spectrumFile = (String)ioParams[1];

          try
          {
              FitsArchive fa_Ifgm = new FitsArchive();

              Product prod_Ifgm = fa_Ifgm.load(interferogramFile);

              CompositeDataset sdiScan = (CompositeDataset)prod_Ifgm.get("SPECF");
              scanName = (String)sdiScan.keySet().iterator().next();

              CompositeDataset pixels_Ifgm = (CompositeDataset)sdiScan.get(scanName);

              arrayWidth = pixels_Ifgm.size();
              arrayLength = 1;

              pixels_Name = new String[arrayWidth];
              ifgmCube = new double[arrayWidth][arrayLength][];

              int count = 0;
              for(Iterator i=pixels_Ifgm.keySet().iterator(); i.hasNext();)
              {
                   String pixel_Name = (String)i.next();
                   pixels_Name[count] = pixel_Name;
                   TableDataset single_Ifgm = (TableDataset)pixels_Ifgm.get(pixel_Name);
                   Numeric1dData opd_x = (Numeric1dData)single_Ifgm.getColumn("opd").getData();
                   Numeric1dData signal_x = (Numeric1dData)single_Ifgm.getColumn("signal")
                                           .getData();
                   pixels_Name[count] = pixel_Name;
                   opd = ((Double1d)opd_x).getArray();
                   nPoints_Ifgm = opd_x.getSize();
                   ifgmCube[count][0] = ((Double1d)signal_x).getArray();
                   count++;
              }
              if(spectrumFile != null)
              {
              }
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
          return ifgmCube[indexOfWidth][indexOfLength];
     }

     /**
      * save the spectrum cube to the spectrum file.
      * @param spectrumCube the spectrum cube.
      * @param wn_unit the unit of the wavenumbers.
      */
     public void saveSpectrum(Object spectrumCube, double wn_unit)
     {
          double[][][] spectrum = (double[][][])spectrumCube;

          TableDataset td_spectrum = new TableDataset("Half Spectrum Cube");

          double[] wavenumber = new double[spectrum[0][0].length];
          for(int i=0; i<wavenumber.length; i++) wavenumber[i] = i*wn_unit;
          td_spectrum.addColumn("wavenumber", new Column(new Double1d(wavenumber)));
          for(int i=0; i<arrayWidth; i++)
          {
               Double1d single_spectrum = new Double1d(spectrum[i][0]);
               td_spectrum.addColumn(pixels_Name[i], new Column(single_spectrum));
          }

          CompositeDataset cd_spectrum = new CompositeDataset("Detection Array");
          cd_spectrum.set(scanName, td_spectrum);

          Product prod_Spectrum = new Product("Spectrum Cube");
          prod_Spectrum.set("Spectrum", cd_spectrum);

          try
          {
               FitsArchive fa_Spectrum = new FitsArchive();
               fa_Spectrum.save(spectrumFile, prod_Spectrum);
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
