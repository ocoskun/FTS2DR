import ca.uol.aig.fts.drpipeline.*;

public class TestDRPipeline
{
    public static void main(String[] args)
    {
        int numThread = 1;
        String infile = "xyz", outfile = "zyx0";
        if(args.length != 0)
        {
              try
              {
                  numThread = Integer.parseInt(args[0]);
              }
              catch(NumberFormatException e)
              {
                  System.out.println(e.toString());
              }
              if(args.length>1) infile = args[1];
              if(args.length>2) outfile = args[2];
        }
        System.out.println("TestDRPipeline: numThread = " + numThread + ", IN = " + infile + ", OUT = " + outfile);

        long t0 = System.currentTimeMillis();
	DRPipeline drp = new DRPipeline(infile, outfile, 60, 300, 6000, 2, 0.1, numThread);
        System.out.println("Time = " + (System.currentTimeMillis() - t0));
    }
}
