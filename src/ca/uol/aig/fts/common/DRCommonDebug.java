package ca.uol.aig.fts.common;

/**
 * Data exchange among different FTS classes
 * @author Baoshe Zhang
 * @author Astronomial Instrument Group of UoL
 */

public class DRCommonDebug
{
     public static int pc_dsSize;
     public static int pc_ssSize;
     public static int pc_pcfSize;
     public static int deglitch_flag;
    
     public static double[] ifgm = null;
     public static double[] ifgm_interp = null;
     public static double[] mirror_pos = null;
     public static double[] mirror_pos_interp = null;
     public static double newInterval_ifgm = 0;
     public static double[] wn_Bounds = new double[2];
     public static double   phaseFittingStdErr = 0;
     public static double[] phase_orig = null;
     public static double[] phase_fitting = null;
     public static double[] intensity_square = null;
     public static double[] pcf = null;
     public static double[] spectrum = null;
}
