package ca.uol.aig.fts.display;

import javax.swing.JPanel;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;

public class PlotXY extends JPanel
{
     String title;
     double[] x_bottom;
     double[][] y_left, y_right;

     double[][] x_add_left = new double[10][], y_add_left = new double[10][];
     double[][] x_add_right = new double[10][], y_add_right = new double[10][];
     int num_add_left = 0;
     int num_add_right = 0;

     double x_min_bottom, x_max_bottom;
     double y_min_right, y_max_right, y_min_left, y_max_left;

     final int leftmargin = 60;
     final int rightmargin = 60;
     final int topmargin = 30;
     final int bottommargin = 20;

     int npoints = 0;
     int width, height;

     Graphics g;

     public PlotXY(String title)
     {
         setPreferredSize(new Dimension(400, 250));
         setBackground(Color.WHITE);
         this.title = title;
         this.width = getWidth();
         this.height = getHeight();
         this.g = this.getGraphics();
     }
     public void paintComponent(Graphics g)
     {
         this.g = g;
         super.paintComponent(g);
         int w = getWidth();
         int h = getHeight();

         if(w < (leftmargin+rightmargin+10)) w = leftmargin+rightmargin+10;
         if(h < (topmargin+bottommargin+10)) h = topmargin+bottommargin+10;
         this.width = w;
         this.height = h;

         g.drawString(title, w/2-20, topmargin-10);

         drawCoordinate();

         g.clipRect(leftmargin, topmargin, width-rightmargin-leftmargin, 
                    height-bottommargin-topmargin);
         drawCurves();

         g.setClip(null);
     }
     private void drawCurves()
     {
         if(y_left != null)
         {
             for(int i=0; i<y_left.length; i++)
             {
                 if(y_left[i] != null)
                 {
                     if(y_left[i].length > 0) 
                     {
                          g.setColor(new Color(255, (i*100)%256, 30));
                          if(i%2 == 0)
                              drawSingleCurve(x_bottom, y_left[i], 'l', '-');
                          else
                              drawSingleCurve(x_bottom, y_left[i], 'l', '.');
                     }
                 }
             }
         }
         if(y_right != null)
         {
             for(int i=0; i<y_right.length; i++)
             {
                 if(y_right[i] != null)
                 {
                     if(y_right[i].length > 0)
                     {
                          g.setColor(new Color(30, (i*100)%256, 255));
                          if(i%2 == 0)
                             drawSingleCurve(x_bottom, y_right[i], 'r', '-');
                          else
                             drawSingleCurve(x_bottom, y_right[i], 'r', '.');
                     }
                 }
             }
         }

         for(int i=0; i<num_add_left; i++)
         {
             g.setColor(new Color(255, (i*100)%256, 30));
             drawSingleCurve(x_add_left[i], y_add_left[i], 'l', '.');
         }
         for(int i=0; i<num_add_right; i++)
         {
             g.setColor(new Color(30, (i*100)%256, 255));
             drawSingleCurve(x_add_right[i], y_add_right[i], 'r', '.');
         }
     }

     private void drawSingleCurve(double[] x, double[] y, char left_right, char linepoint)
     {
         if(x == null) return;

         int NUM = x.length;

         int[] a = new int[NUM];
         int[] b = new int[NUM];

         double x_min = x_min_bottom, x_max = x_max_bottom;
         double y_min = 0, y_max = 0;

         if(left_right == 'l')
         {
              y_min = y_min_left;
              y_max = y_max_left;
         }
         else
         {
              y_min = y_min_right;
              y_max = y_max_right;
         }

         double x_slope = (width-leftmargin-rightmargin)/(x_max-x_min);
         for(int i=0; i<NUM; i++) a[i] = leftmargin + (int)(x_slope*(x[i]-x_min));

         double y_slope =  -(height-topmargin-bottommargin)/(y_max-y_min);
         for(int i=0; i<NUM; i++) b[i] = height-bottommargin + (int)(y_slope*(y[i]-y_min));

         g.drawPolyline(a, b, a.length);

         if(linepoint == '.')
         {
             for(int i=0; i<NUM; i++)
             {
                 if(inBox(a[i], b[i]))
                 {
                     g.drawLine(a[i]-2, b[i], a[i]+2, b[i]);
                     g.drawLine(a[i], b[i]-2, a[i], b[i]+2);
                 }
             }
         }
     }

     private void drawPolyLine(int[] a, int[] b)
     {
         int NUM = a.length;
         if(a == null) return;
         if(NUM == 1) g.drawLine(a[0], b[0], a[0], b[0]);
     }
     private void drawCoordinate()
     {

         if(x_bottom == null) return;

         final int major_xtick = 6;
         final int minor_xtick = 2;
         final int major_ytick = 6;
         final int minor_ytick = 2;

         g.drawRect(rightmargin, topmargin,
               width-rightmargin-leftmargin, height-topmargin-bottommargin);

         Font old_font = g.getFont();
         Font new_font = new Font(null, Font.PLAIN, 10);
         g.setFont(new_font);


         g.setColor(Color.BLACK);

         for(int i=0; i<major_xtick; i++)
         {
             int x_tick = leftmargin + i*(width-leftmargin-rightmargin)/(major_xtick-1);
             g.drawLine(x_tick, height-bottommargin, x_tick, height-bottommargin-5);
             double x_coord;
             x_coord = x_min_bottom + i * ((x_max_bottom - x_min_bottom)/(major_xtick-1));
             String x_str = String.format("%1$5.2e", x_coord);

             g.drawString(x_str, x_tick-20, height-bottommargin+10); 

             if(i<major_xtick-1)
             {
                 for(int j=1; j<minor_xtick+1; j++)
                 {
                     int x_tick_m = x_tick + 
                         j*(width-leftmargin-rightmargin)/((major_xtick-1)*(minor_xtick+1));
                     g.drawLine(x_tick_m, height-bottommargin, x_tick_m, height-bottommargin-2);
                 }
             }
         }
         if(x_min_bottom * x_max_bottom < 0)
         {
             int x_zero = leftmargin-(int)(x_min_bottom/(x_max_bottom-x_min_bottom)*(width-leftmargin-rightmargin));
             g.drawLine(x_zero, topmargin, x_zero, height-bottommargin);
         }


         for(int i=0; i<major_ytick; i++)
         {
             int y_tick = height-bottommargin - i*(height-topmargin-bottommargin)/(major_ytick-1);
             g.setColor(Color.BLACK);
             g.drawLine(rightmargin, y_tick, rightmargin+5, y_tick);
             double y_coord;
             y_coord = y_min_left + i * ((y_max_left-y_min_left)/(major_ytick-1));
             String y_str = String.format("%1$5.2e", y_coord);
             g.setColor(Color.RED);
             g.drawString(y_str, 10, y_tick);

             g.setColor(Color.BLACK);
             if(i<major_ytick-1)
             {
                 for(int j=1; j<minor_ytick+1; j++)
                 {
                     int y_tick_m = y_tick -
                             j*(height-topmargin-bottommargin)/((major_ytick-1)*(minor_ytick+1));
                     g.drawLine(rightmargin, y_tick_m, rightmargin+2, y_tick_m);
                 }
             }
         }
         if(y_min_left * y_max_left < 0)
         {
             int y_zero = height-bottommargin
                         -(int)(y_min_left/(y_max_left-y_min_left)*(topmargin+bottommargin-height));
             g.setColor(Color.RED);
             g.drawLine(leftmargin, y_zero, width-rightmargin, y_zero);
         }


         for(int i=0; i<major_ytick; i++)
         {
              int y_tick = height-bottommargin - i*(height-topmargin-bottommargin)/(major_ytick-1);
              g.setColor(Color.BLACK);
              g.drawLine(width-leftmargin, y_tick, width-leftmargin-5, y_tick);
              double y_coord;
              y_coord = y_min_right + i * ((y_max_right-y_min_right)/(major_ytick-1));
              String y_str = String.format("%1$5.2e", y_coord);
              g.setColor(Color.BLUE);
              g.drawString(y_str, width-leftmargin+2, y_tick);

              g.setColor(Color.BLACK);
              if(i<major_ytick-1)
              {
                  for(int j=1; j<minor_ytick+1; j++)
                  {
                      int y_tick_m = y_tick -
                            j*(height-topmargin-bottommargin)/((major_ytick-1)*(minor_ytick+1));
                      g.drawLine(width-leftmargin, y_tick_m, width-leftmargin-2, y_tick_m);
                  }
             }
         }
         if(y_min_right * y_max_right < 0)
         {
             int y_zero = height-bottommargin
                         -(int)(y_min_right/(y_max_right-y_min_right)*(topmargin+bottommargin-height));
             g.setColor(Color.BLUE);
             g.drawLine(leftmargin, y_zero, width-rightmargin, y_zero);
         }

         g.setFont(old_font);
     }

     public void plot(double[] x, double[][] yleft, double[][] yright)
     {
         if(x == null) return;
         int NUM = x.length;

         this.x_bottom = x;
         this.y_left = yleft;
         this.y_right = yright;

         x_min_bottom = 1.0e99;
         x_max_bottom = -1.0e99;
         for(int i=0; i<x.length; i++)
         {
              if(x[i]>x_max_bottom) x_max_bottom = x[i];
              if(x[i]<x_min_bottom) x_min_bottom = x[i];
         }

         if(yleft != null)
         {
             y_min_left = 1.0e99;
             y_max_left = -1.0e99;
             for(int i=0; i<yleft.length; i++)
             {
                 if(yleft[i] != null)
                 {
                     for(int j=0; j<yleft[i].length; j++)
                     {
                        if(yleft[i][j]>y_max_left) y_max_left = yleft[i][j];
                        if(yleft[i][j]<y_min_left) y_min_left = yleft[i][j];
                     }
                 }
             }
         }

         if(yright != null)
         {
             y_min_right = 1.0e99;
             y_max_right = -1.0e99;
             for(int i=0; i<yright.length; i++)
             {
                 if(yright[i] != null)
                 {
                     for(int j=0; j<yright[i].length; j++)
                     {
                        if(yright[i][j]>y_max_right) y_max_right = yright[i][j];
                        if(yright[i][j]<y_min_right) y_min_right = yright[i][j];
                     }
                 }
             }
         }
         repaint();
     }

     private boolean inBox(int a, int b)
     {
         return a>=leftmargin && a<=width-rightmargin && b>=topmargin && b<=height-bottommargin;
     }
     public void addPlot(double[] x, double[] y, Color lineColor, char right_left)
     {
         if(x == null) return;
         if(right_left == 'l')
         {
             num_add_left++;
             if(num_add_left >= 10) return;
             x_add_left[num_add_left-1] = x;
             y_add_left[num_add_left-1] = y;
         }
         else
         {
             num_add_right++;
             if(num_add_right >= 10) return;
             x_add_right[num_add_right-1] = x;
             y_add_right[num_add_right-1] = y;
         }
         repaint();
     }
     public double[] getRange_x()
     {
         double[] t = new double[2];
         t[0] = x_min_bottom;
         t[1] = x_max_bottom;
         return t;
     }
     public double[] getRange_y_left()
     {
         double[] t = new double[2];
         t[0] = y_min_left;
         t[1] = y_max_left;
         return t;
     }
     public double[] getRange_y_right()
     {
         double[] t = new double[2];
         t[0] = y_min_right;
         t[1] = y_max_right;
         return t;
     }
     public void setRange_x(double x_min, double x_max)
     {
         x_min_bottom = x_min;
         x_max_bottom = x_max;
         repaint();
     }
     public void setRange_y_left(double y_min, double y_max)
     {
         y_min_left = y_min;
         y_max_left = y_max;
         repaint();
     }
     public void setRange_y_right(double y_min, double y_max)
     {
         y_min_right = y_min;
         y_max_right = y_max;
         repaint();
     }
}
