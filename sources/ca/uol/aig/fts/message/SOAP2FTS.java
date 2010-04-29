/**
 *  The SOAP interface for FTS-2.
 */
package ca.uol.aig.fts.message;

import java.net.URL;
import uk.ac.starlink.soap.AppHttpSOAPServer;
import ca.uol.aig.fts.drpipeline.DRPipeline;

/**
 * SOAP2FTS has two main functions: as a SOAP server and as a SOAP service
 * provider. It creates a SOAP server for the data reduction pipeline, DRPipeline.
 * It has three operations: setParameters, dataReduction, and exitSOAP.
 */
public class SOAP2FTS
{

     static int pcfSize_h = 80;
     static int dsSize  = 300;
     static int ssSize  = 300;
     static int phaseFittingdegree = 2;
     static double weight_limit = 0.01;
     static double wn_lBound = 0D;
     static double wn_uBound = 1.0D;
     static int deglitching_flag = 0;
     static int numThread;

     AppHttpSOAPServer fts2soapServer = null;
     /**
      * setParameters is used to set parameters of data reduction.
      * @param pcfSize_h the half of the size of the phase correction function.
      * @param dsSize the half of the size of the double-side interferogram.
      * @param phaseFittingdegree the fitting degree of the phase correction function.
      * @param weight_limit used to control the weight of each points. When the amplitude of
      *        a point over the amplitude maxima is greater than weigth_limit, this point
      *        will be taken into account in the phase-fitting.
      */
     public void setParameters(int pcfSize_h, int dsSize, int ssSize, int phaseFittingdegree, 
                               double weight_limit, double wn_lBound, double wn_uBound, 
                               int deglitching_flag, int numThread)
     {
          this.pcfSize_h = pcfSize_h;
          this.dsSize = dsSize;
          this.ssSize = ssSize;
          this.phaseFittingdegree = phaseFittingdegree;
          this.weight_limit = weight_limit;
          this.wn_lBound = wn_lBound;
          this.wn_uBound = wn_uBound;
          this.deglitching_flag = deglitching_flag;
          this.numThread = numThread;
     }
     /**
      * start a data reduction.
      * @param inPath the path of the raw data file.
      * @param outPath the path of the reduced data file.
      */
     public void dataReduction(String inPath, String outPath)
     {
          System.out.println(">>> Data Reduction: <IN=" + inPath + ">:<OUT=" + outPath +">");
          Object[] ioParams = new Object[]{inPath, outPath};
          new DRPipeline(ioParams, pcfSize_h, dsSize, ssSize, phaseFittingdegree, 
                         weight_limit, wn_lBound, wn_uBound, deglitching_flag, numThread,
                         "Scuba2NDF");

          System.out.println(">>> End of Data Reduction <<<");
     }

     /**
      *  stop the SOAP server and exit.
      */
     public void exitSOAP()
     {
          try
          {
               fts2soapServer.stop();
               Thread.sleep(1000);
          }
          catch(Exception e)
          {
          }
          System.exit(1);
     }
     /**
      * start a SOAP server.
      * @param wsddFile the path of the web service definition file.
      * @param soapServerPort the socket port of this SOAP server.
      */
     public void startSOAPServer(String wsddFile, int soapServerPort)
     {
         try 
         {
              URL deployURL =  new URL("file://" + wsddFile);;
              fts2soapServer = new AppHttpSOAPServer(soapServerPort);
              fts2soapServer.start();
              fts2soapServer.addSOAPService(deployURL);
         }
         catch (Exception e) 
         {
              e.printStackTrace();
              System.exit(1);
         }

         /* Create and add a shutdown hook */
 
         final AppHttpSOAPServer fts2soapServer_x = fts2soapServer;
         Thread fts2hook = new Thread() 
                       {
                           public void run()
                           {
                                setName("Shutdown");
                                try 
                                {
                                     fts2soapServer_x.stop();
                                     /* Try to avoid JVM crash */
                                     Thread.sleep(1000);
                                }
                                catch(Exception e) 
                                {
                                }
                           }
                       };
          Runtime.getRuntime().addShutdownHook(fts2hook);
     }
}
