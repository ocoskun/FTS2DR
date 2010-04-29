package ca.uol.aig.fts.fitting;
import Jama.Matrix;
/**
 * PolynomialFitting uses weighted lease-square methods to fit a 1-D curve.
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */ 
public class PolynomialFitting
{
     int polyDegree;
     double[] fittingParam = null;

     double[] x_orig = null, y_orig = null, weights_orig = null;
     /**
      * Constructor.
      * @param polyDegree the degree of the fitting polynomial.
      */
     public PolynomialFitting(int polyDegree)
     {
          this.polyDegree = polyDegree;
          fittingParam = new double[polyDegree+1]; 
     }
     /* get the matrix (left side) of the linear equation */
     double[][] getCoeff(double[] x, double[] weights)
     {
          double[] z = new double[x.length];
          double[] coeff = new double[2*polyDegree+1];
          for(int i=0; i<z.length; i++) z[i] = weights[i];
          coeff[0] = sumArray(z);

          for(int i=1; i<2*polyDegree+1; i++)
          {
              z = dotProduct(x, z);
              coeff[i] = sumArray(z); 
          }
          double[][] matrixElement = new double[polyDegree+1][polyDegree+1];
          for(int i=0; i<polyDegree+1; i++)
             for(int j=0; j<polyDegree+1; j++)
             {
                 matrixElement[i][j] = coeff[i+j];
//               System.out.println("("+i+", " +j+") = " +coeff[i+j]);
             }
          return matrixElement;
     }
     /* get the right side of the linear equation */
     double[] getValue(double[] x, double[] y, double[] weights)
     {
          double[] z = new double[x.length];
          double[] value = new double[polyDegree+1];

          for(int i=0; i<z.length; i++) z[i] = weights[i] * y[i];

          value[0] = sumArray(z);

          for(int i=1; i<polyDegree+1; i++)
          {
             z = dotProduct(x, z);
             value[i] = sumArray(z);
          } 
          return value; 
     }
     /*  dot product of two 1-D arrays */
     double[] dotProduct(double[] x, double[] y)
     {
          double[] z = new double[x.length];
          for(int i=0; i<z.length; i++)
          {
               z[i] = x[i] * y[i];
          }
          return z;
     }
     /* the sum of all elements of a 1-D array */
     double sumArray(double[] x)
     {
          double sum = 0.0;
          for(int i=0; i<x.length; i++) sum += x[i];
          return sum;
     }
     /**
      *  use these parameters to get a least square fitting.
      * @param x the coordinates.
      * @param y the values, y = y(x).
      * @param weights the weights of every x.
      */
     public void fit(double[] x, double[] y, double[] weights)
     {
          x_orig = x;
          y_orig = y;
          weights_orig = weights;
          Matrix lsf_matrix = new Matrix(getCoeff(x, weights));
          double[] value = getValue(x, y, weights);
          Matrix lsf_value  = new Matrix(value, value.length);
          fittingParam = lsf_matrix.solve(lsf_value).getColumnPackedCopy();
     }

     /**
      * get the standard error of the current fitting.
      * @return the standard deviation.
      */
     public double getSTDDev()
     {
          double error = 0, total_weights = 0;
          double[] y_fitting = getResult(x_orig);
          for(int i=0; i<y_orig.length; i++)
          {
               error += weights_orig[i]*(y_fitting[i]-y_orig[i])*(y_fitting[i]-y_orig[i]);
               total_weights += weights_orig[i];
/*
               System.out.println(x_orig[i] + " " + y_orig[i] 
                                  + " " + y_fitting[i] + " " + weights_orig[i]);
*/
          }
          error = Math.sqrt(error/total_weights);
          return error;
     }

     /**
      * get the fitting parameters.
      * @return the fitting parameters.
      */
     public double[] getFittingParam()
     {
          return fittingParam;
     }
     /**
      * use the fitting polynomial to get the new x-y curve.
      * @param x the new coordinates.
      * @return the new values corresponding to x.
      */
     public double[] getResult(double[] x)
     {
          double[] y = new double[x.length];
          double xn;
          for(int i=0; i<y.length; i++)
          {
               y[i] = 0;
               xn = 1.0;
               for(int j=0; j<polyDegree+1; j++)
               {
                    y[i] += fittingParam[j] * xn;
                    xn *= x[i];
               }
          }
          return y;
     }
     /**
      * use the fitting polynomial to get the new x-y.
      * @param x the new coordinate.
      * @return the new value corresponding to x.
      */
     public double getResult(double x)
     {
          double y;
          double xn;
          y = 0;
          xn = 1.0;
          for(int j=0; j<polyDegree+1; j++)
          {
              y += fittingParam[j] * xn;
              xn *= x;
          }
          return y;
     }
}
