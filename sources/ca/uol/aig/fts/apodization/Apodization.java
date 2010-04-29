package ca.uol.aig.fts.apodization;
/**
 * a collection of apodization functions.
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */
public class Apodization
{
      private double[] ap_factor;
      /**
       * Constructor
       * @param a the low bound of the data range
       * @param b the upper bound of the data range
       * @param NPoints the number of the total points
       * @param apf_name the name of the apodization function
       */
      public Apodization(double a, double b, int NPoints, String apf_name)
      {
           if(a>1 || a<-1 || b>1 || b<-1 || a>b) 
                 throw new IllegalArgumentException("invalid value of a or b !!!");
           if(NPoints < 2) throw new IllegalArgumentException("NPoints must be greater than 1 !!!");
           if(apf_name.equals("Battlett")) ap_factor = apf_Battlett(a, b, NPoints);
           else if(apf_name.equals("Blackman")) ap_factor = apf_Blackman(a, b, NPoints);
           else if(apf_name.equals("Connes")) ap_factor = apf_Connes(a, b, NPoints);
           else if(apf_name.equals("Cosine")) ap_factor = apf_Cosine(a, b, NPoints);
           else if(apf_name.equals("Gaussian")) ap_factor = apf_Gaussian(a, b, NPoints);
           else if(apf_name.equals("Hamming")) ap_factor = apf_Hamming(a, b, NPoints);
           else if(apf_name.equals("Hanning")) ap_factor = apf_Hanning(a, b, NPoints);
           else if(apf_name.equals("Uniform")) ap_factor = apf_Uniform(a, b, NPoints);
           else if(apf_name.equals("Welch")) ap_factor = apf_Welch(a, b, NPoints);
           else throw new IllegalArgumentException("Invalid Apodization Function Name !!!");
      }
      /**
       *  perform the apodization
       * @param y the data to be apodized
       */
      public void execute(double[] y)
      {
           for(int i=0; i<y.length; i++) y[i] = y[i]*ap_factor[i];
      }
      /* get a Battlett apodization sequence */
      private double[] apf_Battlett(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = 1.0 - Math.abs(x);
                x += interval;
           }
           return y;
      }
      /* get a Blackman apodization sequence */
      private double[] apf_Blackman(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x, x_temp;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                x_temp = Math.cos(x*Math.PI);
                y[i] = 0.42 + 0.5*x_temp + 0.08*(2*x_temp*x_temp - 1.0);
                x += interval;
           }
           return y;
      }
      /* get a Connes apodization sequence */
      private double[] apf_Connes(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = (1.0 - x*x)*(1.0 - x*x);
                x += interval;
           }
           return y;
      }
      /* get a Cosine apodization sequence */
      private double[] apf_Cosine(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = Math.cos(x*Math.PI/2.0);
                x += interval;
           }
           return y;
      }
      /* get a Gaussian apodization sequence */
      private double[] apf_Gaussian(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = Math.exp(-x*x/2);
                x += interval;
           }
           return y;
      }
      /* get a Hamming apodization sequence */
      private double[] apf_Hamming(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = 0.54 + 0.46*Math.cos(x*Math.PI);
                x += interval;
           }
           return y;
      }
      /* get a Hanning apodization sequence */
      private double[] apf_Hanning(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = (1.0 + Math.cos(x*Math.PI))/2.0;
                x += interval;
           }
           return y;
      }
      /* get a Uniform apodization sequence */
      private double[] apf_Uniform(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = 1.0;
                x += interval;
           }
           return y;
      }
      /* get a Welch apodization sequence */
      private double[] apf_Welch(double a, double b, int NPoints)
      {
           double interval = (b-a)/(NPoints-1);
           double[] y = new double[NPoints];
           double x;
           x = a;
           for(int i=0; i<NPoints; i++)
           {
                y[i] = 1.0 - x*x;
                x += interval;
           }
           return y;
      }
}
