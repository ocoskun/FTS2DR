package test;

import herschel.ia.io.fits.FitsArchive;
import herschel.ia.dataset.Product;
import herschel.ia.dataset.CompositeDataset;
import herschel.ia.dataset.TableDataset;
import herschel.ia.dataset.Column;
import herschel.ia.numeric.Numeric1dData;
import herschel.ia.numeric.Double1d;
import java.util.Iterator;
import java.io.IOException;


public class TestSpire
{
     public static void main(String[] args)
     {
          try
          {
              FitsArchive fa_Ifgm = new FitsArchive();
              //Product prod_Ifgm = fa_Ifgm.load("SDI_HR.fits");
              //Product prod_Ifgm = fa_Ifgm.load("/mnt/bluesky/spire/data/dummy.fits");
              //Product prod_Ifgm = fa_Ifgm.load("dummy.fits");
              Product prod_Ifgm = fa_Ifgm.load("/mnt/bluesky/users/Zhang/data/ssw/ILT_PERF_SMC_300117ED_HCSS_IFGM.fits");
              for(Iterator j=prod_Ifgm.keySet().iterator(); j.hasNext();)
              {
                   String scanName = (String)j.next();
                   System.out.println("******** " + scanName + " *************");
                   CompositeDataset sdiScan = (CompositeDataset)prod_Ifgm.get(scanName);
                   for(Iterator i=sdiScan.keySet().iterator(); i.hasNext();)
                   {
                       String pixel_Name = (String)i.next();
                       System.out.println("---------- " + pixel_Name + " ------------");
                       TableDataset single_Ifgm = (TableDataset)sdiScan.get(pixel_Name);
                       double[] x, y;
//System.out.println("Num of Column:" + single_Ifgm.getColumnCount());
//System.out.println(single_Ifgm.nameOf(0));
//System.out.println(single_Ifgm.nameOf(1));

                       Numeric1dData opd = null;
                       Numeric1dData signal = null; 

                       for(int iii=0; iii<single_Ifgm.getColumnCount(); iii++)
                       {
                            String columnName = single_Ifgm.nameOf(iii);
                            if(columnName.compareToIgnoreCase("opd") == 0)
                                opd = (Numeric1dData)single_Ifgm.getColumn(columnName).getData();
                            if(columnName.compareToIgnoreCase("signal") == 0)
                                signal = (Numeric1dData)single_Ifgm.getColumn(columnName).getData();
                       }

for(int iii=0; iii<opd.length(); iii++)
{
     System.out.println(((Double1d)opd).get(iii) + " " + ((Double1d)signal).get(iii));
}
System.exit(0);


                       x = ((Double1d)opd).getArray();
                       y = ((Double1d)signal).getArray();
                       for(int kkk = 0; kkk < x.length; kkk++)
                       {
                            System.out.println(String.format("%1$f  %2$f", x[kkk], y[kkk]));
                       }
                       System.exit(0);

//                       System.out.println(signal.getSize());
                   }
              }
          }
          catch(IOException e)
          {
              e.printStackTrace();
          }
     }
}
