import ca.uol.aig.fts.message.SOAP2FTS;

public class TestSOAPServer
{
    public static void main(String[] arg)
    {
          String wsddFile = "/DR2FTS/test/test_soap/fts2deploy.wsdd";
          int soapServerPort = 8082;
          SOAP2FTS sf = new SOAP2FTS();
          sf.startSOAPServer(wsddFile, soapServerPort);
    }
}
