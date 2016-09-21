package kadesktopclient;

public class KAHybrid implements IKAIntervalTester
{
    private int m_low;
    private int m_high;
    private int m_inc;
    private int m_lkg;
    private int m_test;
    private boolean m_fBinaryPhase;
    
    KAHybrid()
    {
        ResetTest();
    }
    
    @Override public void ResetTest()
    {
        synchronized(this)
        {
            m_inc = 1;
            m_low = 1;
            m_lkg = 1;
            m_high = 512; 
            m_fBinaryPhase = false;
        }
    }
    
    @Override public int GetLKGInterval()
    {
        int retLkg;
        
        synchronized(this)
        {
            retLkg = m_lkg;
        }
        
        return retLkg;       
    }
    
    @Override public void SetCurTestResult(boolean fSucceeded)
    {
        synchronized(this)
        {
            if (true == fSucceeded)
            {
                m_inc *= 2;
                m_low = m_test;
                m_lkg = m_test;
            }
            else
            {
                m_fBinaryPhase = true;
                m_low = m_lkg;
                m_high = m_test-1;
            }
        }
    }
    
    @Override public int GetNextIntervalToTest()
    {
        int retTest = 0;
        
        synchronized(this)
        {
            m_test = (m_fBinaryPhase ? (m_low + m_high + 1)/2 : (m_lkg + m_inc));
            retTest = m_test;
        }    
        return retTest;
    }
    
    @Override public boolean IsCompleted()
    {
        boolean fCompleted = false;
        
        synchronized(this)
        {
            fCompleted = (m_high-m_low <= 0);
        }
        
        return fCompleted;
    }
}