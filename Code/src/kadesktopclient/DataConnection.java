package kadesktopclient;

import java.io.*;
import java.net.*;

class DataConnection implements Runnable
{
    Socket m_sock;
    BufferedReader m_inFromServer;
    DataOutputStream m_outToServer;
    IKAIntervalTester m_tester;
    
    String m_strServerDNS = "localhost";
    int m_serverPort = 80;
    
    DataConnection(
        String strServer,
        int port,
        IKAIntervalTester tester
        ) throws Exception
    {
        m_tester = tester;
        if (!strServer.isEmpty())
        {
            m_strServerDNS = strServer;
        }
        if (port != 0)
        {
            m_serverPort = port;
        }
    }
    
    @Override public void run()
    {
        String strResponse;
        
        while(true)
        {
            try
            {
                if (!IsChannelOpen())
                {
                    OpenChannel();
                }

                long waitCompleted = 0;
                while(true)
                {
                    long lkg = m_tester.GetLKGInterval();
                    if (waitCompleted >= lkg)
                    {
                        break;
                    }
                    
                    long delay = lkg - waitCompleted;
                    
                    //
                    // Wait to complete the KA interval gap
                    //
                    Log("Going to sleep for " + delay + " minutes.\n");
                    Thread.sleep(delay * 60 * 1000);
                    
                    waitCompleted += delay;
                }

                //
                // Now send a ping
                //
                m_outToServer.writeBytes("PING DATA\n");
                Log("sent> PING DATA\n");

                strResponse = m_inFromServer.readLine();
                if (null != strResponse)
                {
                    Log("recv> " + strResponse + "\n");
                }
                else
                {
                    //
                    // end of input stream reached. That means, the other
                    // side has probably closed the connection
                    //
                    Log("Connection reset by peer.\n");
                    CloseChannel();
                }
            }
            catch(Exception e)
            {
                Log(e.toString()+"\n");
                CloseChannel();
            }
        }
    }
    
    private boolean IsChannelOpen()
    {
        return (null != m_sock);
    }
    
    private void OpenChannel() throws Exception
    {
        if (null == m_sock)
        {
            Log("Connecting to " + m_strServerDNS + ":" + m_serverPort + "...\n");      
            m_sock = new Socket(m_strServerDNS, m_serverPort);
            m_outToServer = new DataOutputStream(m_sock.getOutputStream());
            m_inFromServer = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            Log("Done.\n");
        }
    }

    
    private void CloseChannel()
    {
        if (null != m_sock)
        {
            Log("Closing socket @ port " + m_sock.getPort() + "...\n");
            try
            {
                m_inFromServer.close();
                m_inFromServer = null;

                m_outToServer.close();
                m_outToServer = null;

                m_sock.close();
                m_sock = null;

                Log("Done.\n");
            }
            catch(IOException e)
            {
                Log(e.toString() + "\n");
            }
        }
    }
    
    private void Log(String strToLog)
    {
        strToLog = "DataConn - " + strToLog;
        Logger.Log(strToLog); 
    }
}

