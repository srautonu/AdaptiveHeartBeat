package kadesktopclient;

interface IKAIntervalTester
{
    void ResetTest();
    void SetCurTestResult(boolean fSucceeded);
    int GetNextIntervalToTest();
    int GetLKGInterval();
    boolean IsCompleted();
}