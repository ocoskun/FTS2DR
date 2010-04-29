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
      * @param in the path of the input file.
      * @param out the path of the output file.
      */
     public abstract void init(String in, String out);
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
      * get the data type of the interferogram cube. Possible values:
      * _UWORD, _WORD, _INTEGER, _REAL, _DOUBLE.
        public abstract String get_ifgmDataType();
      */

     /**
      *  get the dimension of the interferogram cube.
         public abstract long[] get_ifgmCubeShape();
      */
     /**
      * get the total number of the data in the interferogram cube.
        public abstract long get_ifgmCubeSize();
      */

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
     public abstract int get_npoints_ifgm();

     /**
      * save the spectrum cube to the spectrum file.
      * @param spectrumCube the spectrum cube.
      */
     public abstract void saveSpectrum(Object spectrumCube);

     /**
      * close the spectrum file and/or the interferogram file.
      */
     public abstract void closeSpectrum();
}
