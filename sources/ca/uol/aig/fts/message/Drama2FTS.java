package ca.uol.aig.fts.message;

import au.gov.aao.drama.Arg;
import au.gov.aao.drama.DramaException;
import au.gov.aao.drama.DramaTask;
import au.gov.aao.drama.DramaErs;
import au.gov.aao.drama.DramaStatus;
import au.gov.aao.drama.Sdp;
import au.gov.aao.drama.dul_err;

import ca.uol.aig.fts.drpipeline.DRPipeline;

/**
 * Create a Drama task for the data reduction pipeline, DRPipeline.
 * This Drama task provides three actions: SETPARAMETERS, DATAREDUCTION, EXIT. <br><br>
 * SETPARAMETERS:  set the parameter of data reduction. <br>
 * DATAREDUCTION:  perform data reduction. <br>
 * EXIT:  exit the current Drama task.
 */
public class Drama2FTS 
{
    DramaTask fts2task = null;

    /**
     * Constructor.
     * @param taskName the name of this Drama task
     */
    public Drama2FTS(String taskName)
    {
        try
        {
            initDramaTask(taskName);
        }
        catch(DramaException e)
        {
            e.printStackTrace();
        }
    }
    /* initialize the Drama task */
    void initDramaTask(String taskName) throws DramaException 
    {

        /* Make this JAVA program a DRAMA task named FTS2DRAMA by creating
           a new DramaTask object.
        */
        fts2task = new DramaTask(taskName);

        fts2task.Add("SETPARAMETERS",new SetParametersAction());
        fts2task.Add("DATAREDUCTION",new DataReductionAction());
        fts2task.Add("EXIT",new ExitAction());

        /* enter the DRAMA message receive loop, which will process 
           DRAMA messages received by this task.  All further work is
           done within this call.
        */
        fts2task.RunDrama();
        exitDramaTask();
    }
    /* clean up the Drama task */
    void exitDramaTask()
    {
        /* The message loop has exited (due to task.PutRequestExit() being
           invoked).  Shutdown the DRAMA task down and then set the object
           reference to null
        */
        fts2task.CloseTask(); 
        fts2task = null;
    }
}

/**
 * SetParametersAction is used to create a Drama action dealing with action SETPARAMERTERS. 
 */
class SetParametersAction implements DramaTask.Action 
{
    public void Obey(DramaTask t) throws DramaException  
    {
         Arg arg = t.GetArgument();

         DataReductionParameters.pc_pcfSize_h = arg.IntValue("pcfSize_h");
         DataReductionParameters.pc_dsSize = arg.IntValue("dsSize");
         DataReductionParameters.pc_ssSize = arg.IntValue("ssSize");
         DataReductionParameters.pc_phaseFittingdegree = arg.IntValue("fittingDegree");
         DataReductionParameters.pc_weight_limit = arg.RealValue("weight_limit");
         DataReductionParameters.pc_wn_lBound_percent = arg.RealValue("wn_lBound");
         DataReductionParameters.pc_wn_uBound_percent = arg.RealValue("wn_uBound");
         DataReductionParameters.pc_deglitch = arg.IntValue("deglitch");
         DataReductionParameters.pc_numThread = arg.IntValue("numThread");

         t.PutRequestEnd();
    }
}

/**
 * ExitAction is used to create a Drama action dealing with action EXIT
 */
class ExitAction implements DramaTask.Action
{
    public void Obey(DramaTask t) throws DramaException  
    {
        t.MsgOut("Exit from FTS-2 Pipeline");
        t.PutRequestExit();
    }
}

/**
 * ExitAction is used to create a Drama action dealing with action DATAREDUCTION
 */

class DataReductionAction implements DramaTask.Action 
{
    public void Obey(DramaTask t) throws DramaException 
    {
        Arg arg = t.GetArgument();

        String inPath = arg.StringValue("in");
        String outPath = arg.StringValue("out");

        t.MsgOut(">>> Data Reduction: <IN=" + inPath + ">:<OUT=" + outPath +">");
        Object[] ioParams = new Object[]{inPath, outPath};
        DRPipeline dp = new DRPipeline(ioParams,
                                       DataReductionParameters.pc_pcfSize_h,
                                       DataReductionParameters.pc_dsSize,
                                       DataReductionParameters.pc_ssSize,
                                       DataReductionParameters.pc_phaseFittingdegree,
                                       DataReductionParameters.pc_weight_limit,
                                       DataReductionParameters.pc_wn_lBound_percent,
                                       DataReductionParameters.pc_wn_uBound_percent,
                                       DataReductionParameters.pc_deglitch,
                                       DataReductionParameters.pc_numThread,
                                       "Scuba2NDF"); 
        t.MsgOut("   Phase Correction : dsSize = "  + dp.get_dsSize() 
                                   + ", ssSize = "  + dp.get_ssSize() 
                                   + ", pcfSize = " + dp.get_pcfSize());
        t.MsgOut("End of Data Reduction");
        t.PutRequestEnd();
    }
}

/**
 * DataReductionParameters is used to pass data reduction parameters between different
 * Drama actions.
 */
class DataReductionParameters
{
    /* pathname of I/O files */

    public static String inPath = "";
    public static String outPath = "";

    /* Parameters for Phase Correction */
    public static int pc_pcfSize_h = 80;
    public static int pc_dsSize  = 300;
    public static int pc_ssSize = 300;
    public static int pc_phaseFittingdegree = 2;
    public static double pc_weight_limit = 0.01;
    public static double pc_wn_lBound_percent = 0.0D;
    public static double pc_wn_uBound_percent = 1.0D;
    public static int pc_deglitch = 0;
    public static int pc_numThread = 1;
}
