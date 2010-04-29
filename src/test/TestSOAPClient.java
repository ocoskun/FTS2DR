package test;

//import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;


public class TestSOAPClient
{
     public static void main(String[] args)
     {
           try
           {

                String inPath = "/DR2FTS/test/test_soap/dr/";
                String outPath = "/DR2FTS/test/test_soap/dr/";

                /* set the network address of the SOAP Server */
                String endpoint = "http://localhost:8082/services/SOAP2FTS";
  
                Service  service = new Service();
                
                Call call = service.createCall();
 
                
                call.setTargetEndpointAddress(endpoint);

                /* call the operation: setParameters */
/*
                call.setOperationName(new QName("http://www.uleth.ca/", "setParameters"));
                int pcfSize_h = 60;
                int dsSize = 300;
                int ssSize = 6000;
                int fittingDegree = 2;
                float weight_limit = 0.1F;
                double wn_lBound = 0.05;
                double wn_uBound = 1.0;
                double deglitch_flag = 3;
                int numThread = 1;
                call.invoke(new Object[] {pcfSize_h, dsSize, ssSize, fittingDegree, 
                                          weight_limit, wn_lBound, wn_uBound, 
                                          deglitch_flag, numThread});  
*/
                /* call the operation: dataReduction */
/*
                call.setOperationName(new QName("http://www.uleth.ca/", "dataReduction"));
                String rawDataFile = "abc";
                String reducedDataFile = "abc0";
                call.invoke(new Object[] {inPath + rawDataFile, outPath + reducedDataFile});
*/

                /* call the operation: setParameters */
/*
                call.setOperationName(new QName("http://www.uleth.ca/", "setParameters"));
                pcfSize_h = 60;
                dsSize = 300;
                ssSize = 6000;
                fittingDegree = 2;
                weight_limit = 0.1F;
                numThread = 20;
                call.invoke(new Object[] {pcfSize_h, dsSize, ssSize, fittingDegree,
                                          weight_limit, numThread});
*/
                /* call the operation: dataReduction */
/*
                call.setOperationName(new QName("http://www.uleth.ca/", "dataReduction"));
                reducedDataFile = "abc1";
                call.invoke(new Object[] {inPath + rawDataFile, outPath + reducedDataFile});
*/
                /* call the operation: exitSOAP (stop the SOAP server) */
                call.setOperationName(new QName("http://www.uleth.ca/", "exitSOAP"));
                call.invoke(new Object[] {});
           }
           catch(Exception e)
           {
                System.err.println(e.toString());
           }
     }
}
