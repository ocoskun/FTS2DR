package ca.uol.aig.fts.io;

/**
 * Read an interferogram file and create a spectrum file.
 * @author Baoshe Zhang
 * @author Astronomical Instrument Group, University of Lethbridge 
 */
public abstract class DataIO 
{

     /**
      *  initialization
      * @param ioParams the parameters related to the In/Out data files.
      */
     public abstract void init(Object[] ioParams);
     /**
      * get the mirror position from the interferogram file.
      */ 
     public abstract double[] getOPD();

     /**
      * get the whole interferogram cube.
        public abstract Object getInterferogram();
     */

     /**
      * get a specified inteferogram.
      * @param indexOfWidth the index of the array pixel in x-axis starting from 0.
      * @param indexOfLength the index of the array pixel in y-axis starting from 0.
      */
     public abstract double[] getInterferogram(int indexOfWidth, int indexOfLength);

     /**
      *  get the size of the array in x-axis.
      */
     public abstract int get_arrayWidth();
     /**
      * get the size of the array in y-axis.
      */
     public abstract int get_arrayLength();
     /**
      * get the number of the data of one interferogram.
      */
     public abstract int get_nPoints_Ifgm();

     /**
      * save the spectrum cube to the spectrum file.
      * @param spectrumCube the spectrum cube.
      */
     public abstract void saveSpectrum(Object spectrumCube, double wavenumber_unit);

     /**
      * close the spectrum file and/or the interferogram file.
      */
     public abstract void closeSpectrum();
}
