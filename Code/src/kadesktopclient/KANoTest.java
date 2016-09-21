package kadesktopclient;

public class KANoTest implements IKAIntervalTester
{
    
    @Override public void ResetTest()
    {
    }
    
    @Override public int GetLKGInterval()
    {
        //
        // default/conservative interval is 1 minute
        //
        return 1;
    }
    
    @Override public void SetCurTestResult(boolean fSucceeded)
    {
    }
    
    @Override public int GetNextIntervalToTest()
    {
        return 0;
    }
    
    @Override public boolean IsCompleted()
    {
        return true;
    }
}