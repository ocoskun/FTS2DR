package ca.uol.aig.fts.fitting;
/**
 *  Optimized CubicSpline Interpolator
 *  @author Baoshe Zhang
 *  @author Astronomical Instrument Group of UoL
 */
public class CubicSplineInterpolation 
{
    private double[] _s, _idx, _p, _coeff_x, _idx26, _dxdx6;
    private double[] _x, _x_new;
    private int _n;
    private int[] _x_index;

    private boolean flag_x_Reversal = false;

    /**
     * Constructor.
     * @param x_old the values of x-axis to be interpolated.
     * @param x_new  the values of the interpolated x-axis.
     */
    public CubicSplineInterpolation(double[] x_old, double[] x_new) 
    {
          initCSI(x_old, x_new);
    }
    /**
     * Constructor.
     * @param x_old the values of x-axis to be interpolated.
     * To obtain the new values of x by evenly sampling.
     */
    public CubicSplineInterpolation(double[] x_old)
    {
          double[] x_new = new double[x_old.length];
          double x_interval = (x_old[x_old.length-1] - x_old[0])/(x_new.length-1);
          x_new[0] = x_old[0];
          for(int i=1; i<x_new.length-1; i++)
                x_new[i] = x_new[i-1] + x_interval;
          x_new[x_new.length-1] = x_old[x_old.length-1];

          if(x_interval < 0) flag_x_Reversal = true;
          initCSI(x_old, x_new);
    }

    /**
     * Constructor.
     * @param x_old the values of x-axis to be interpolated.
     * @param multiplier the number of new x's is equal to (multiplier 
     *              x the number of old x's - multiplier + 1).
     * <br>
     * Obtain the new values of x by evenly sampling.
     */
    public CubicSplineInterpolation(double[] x_old, int multiplier)
    {
          double[] x_new = new double[x_old.length * multiplier - multiplier + 1];
          double x_interval = (x_old[x_old.length-1] - x_old[0])/(x_new.length-1);
          x_new[0] = x_old[0];
          for(int i=1; i<x_new.length - 1; i++)
                x_new[i] = x_new[i-1] + x_interval;
          x_new[x_new.length-1] = x_old[x_old.length-1];

          if(x_interval < 0) flag_x_Reversal = true;
          initCSI(x_old, x_new);
    }

    /* initialization of various parameters of Cubic-Spline interpolation */
    private void initCSI(double[] x_old, double[] x_new)
    {
          if((x_new[0] < x_old[0]) || (x_new[x_new.length-1] > x_old[x_old.length - 1]))
          {
              throw new IllegalArgumentException("Abscissa out of range");
          }

          /* re-orginize x_new in the incremental order */
          if(flag_x_Reversal)
          {
               double x_temp;
               for(int i=0; i<x_new.length/2; i++)
               {
                    x_temp = x_new[i];
                    x_new[i] = x_new[x_new.length-1-i];
                    x_new[x_new.length-1-i] = x_temp;
               }
          }

          /* the following arrays or variables only related to x_old */
          _n = x_old.length;
          _x = x_old;
          _s = new double[_n];
          _idx = new double[_n];
          _idx26 = new double[_n];
          _p  = new double[_n];
          _dxdx6 = new double[_n];
          _coeff_x = new double[_n];
          _x_new = x_new;

          
          _coeff_x[0] = 0.0;
          for(int i = 1; i < _n-1; i++) 
          {
             _s[i] = (_x[i] - _x[i-1]) / (_x[i+1] - _x[i-1]);
             _idx[i] = 1.0/(_x[i] - _x[i-1]);
             _idx26[i] = 6.0/(_x[i+1] - _x[i-1]);
             _p[i] = 1.0/(_s[i] * _coeff_x[i-1] + 2.0);
             _coeff_x[i] = (_s[i] - 1.0) * _p[i];
             _dxdx6[i] = (_x[i] - _x[i-1])*(_x[i] - _x[i-1])/6.0;
          }
          _idx[_n-1] = 1.0/(_x[_n-1] - _x[_n-2]);
          _dxdx6[_n-1] = (_x[_n-1] - _x[_n-2])*(_x[_n-1] - _x[_n-2])/6.0;

          calc_index();
    }    


    /* calculate the index of the new x-axis values. */
    private void calc_index()
    {
          /** Binary search for correct place in the table. */

          _x_index = new int[_x_new.length];

          int j, k;
          if(flag_x_Reversal)
          {
              for(int index=0; index<_x_new.length; index++)
              {
                  j = 0;
                  k = _n-1;
                  while (k - j > 1)
                  {
                      int i = (k + j) >> 1;
                      if (_x[i] < _x_new[index]) k = i;
                      else j = i;
                  }
                  _x_index[index] = k;
              }
          }
          else
          {
              for(int index=0; index<_x_new.length; index++)
              {
                  j = 0;
                  k = _n-1;
                  while (k - j > 1)
                  {
                      int i = (k + j) >> 1;
                      if (_x[i] > _x_new[index]) k = i;
                      else j = i;
                  }
                  _x_index[index] = k;
              }
          }
    }

    /* Use y_old to initialize the coefficient array. */
    private void initialize(double[] y, double[] coeff) 
    {
	  /* Solve for coefficients by tridiagonal algorithm. */

          double[] _y = y;

	  double[] z = new double[_n];
	  z[0] = 0.0;
	
          double[] _dy = new double[_n];
          for(int i = 1; i < _n; i++) _dy[i] = _y[i] - _y[i-1];

          double t;
	  for(int i = 1; i < _n-1; i++) 
          {
	       t = _dy[i+1] * _idx[i+1] - _dy[i] * _idx[i];
	       z[i] = (t * _idx26[i] - _s[i]*z[i-1]) * _p[i];
	  }
	
	  coeff[_n - 1] = 0.0;
	  for (int k = _n-2; k >= 0; k--) 
          {
	       coeff[k] = _coeff_x[k] * coeff[k+1] + z[k];
	  }
    }

    /* Interpolate a specified point indexed by i. */
    private double interpolate_x(int i, int k, double y0, double y1, double coeff0, double coeff1)
    {
          double a = (_x[k] - _x_new[i]) * _idx[k];
          double b = (_x_new[i] - _x[k-1]) * _idx[k];

          return a * y0 + b * y1
                 + ((a*a*a - a) * coeff0 + (b*b*b - b) * coeff1)
                 * _dxdx6[k];
    }

    /**
     * Get new positions.
     * @return the new values of x.
     */
    public double[] getNewPosition()
    {
        return _x_new;
    }
    /**
     * Get new interval between two adjacent points.
     * @return the value of the new interval between two adjacent resampling points.
     */
    public double getNewInterval()
    {
        return (_x_new[1] - _x_new[0]);
    }

    /**
     * Interpolate.
     * @param y_old the values of y-axis corresponding to x_old.
     * @return the values of the interpolate y values corresponding to x_new.
     */
    public double[] interpolate(double[] y_old)
    {

           double[] _coeff = new double[_n];
           initialize(y_old, _coeff);

           double[] y_new = new double[_x_new.length];
           for(int i=0; i<_x_new.length; i++) 
           {
                int k = _x_index[i];
                double y0 = y_old[k-1], y1 = y_old[k];
                double coeff0 = _coeff[k-1], coeff1 = _coeff[k];
                y_new[i] = interpolate_x(i, k, y0, y1, coeff0, coeff1);
           }

           return y_new;
    }

    /**
     * get the index of ZPD.
     * @param zpd the position of the real zpd.
     * @return the index of the point which is the neareat 
     *         to the real zpd in the left.
     */
    public int getIndex_ZPD(double zpd)
    {
          /** Binary search for correct place in the table. */
          int j = 0;
          int k = _n-1;
          while (k - j > 1)
          {
               int i = (k + j) >> 1;
               if (zpd < _x_new[i]) k = i;
               else j = i;
          }
          return j;
    }

    /**
     * get the length of the new interferogram.
     * @return the length of the new interferogram.
     */
    public int getInterferogramLength()
    {
         return _x_new.length;
    }
}
