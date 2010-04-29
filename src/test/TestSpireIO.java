package test;

import ca.uol.aig.fts.io.SpireFitsIO;

public class TestSpireIO
{
     public static void main(String[] args)
     {
          SpireFitsIO sfi = new SpireFitsIO();
          sfi.init(new Object[]{"dummy", "xxx"});
          double[][][] x = (double[][][])sfi.getOPD();
     }
}
