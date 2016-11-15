package kadesktopclient;

import java.io.*;
import java.net.*;

//  kopottakha.cs.uiuc.edu:8080
public class KADesktopClient {
    String m_strServerDNS = "kopottakha.cs.uiuc.edu"; //"localhost";
    int m_serverPort = 8080;
    
    Socket m_sock;
    DataOutputStream m_outToServer;
    BufferedReader m_inFromServer;

    public void Start(String strServer, int port, IKAIntervalTester tester) throws Exception
    {
        String strResponse;
        
        if (!strServer.isEmpty())
        {
            m_strServerDNS = strServer;
        }
        if (port != 0)
        {
            m_serverPort = port;
        }
        
        while(false == tester.IsCompleted())
        {
            long delay = tester.GetNextIntervalToTest();
            
            if (!IsChannelOpen())
            {
                OpenChannel();
            }
            
            //
            // Wait for the test KA interval time
            //
            Log("Going to sleep for " + delay + " minutes.\n");
            Thread.sleep(delay * 60 * 1000);
            
            //
            // Now send a ping
            //
            try
            {
                m_outToServer.writeBytes("PING TEST\n");
                Log("sent> PING TEST\n");
                
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
            catch(IOException e)
            {
                Log(e.toString() + "\n");
                CloseChannel();
            }
                        
            tester.SetCurTestResult(IsChannelOpen());
            Log("LKG KA Interval is " + tester.GetLKGInterval() + " minutes.\n");
        }
        
        Log("Optimal KA Interval is " + tester.GetLKGInterval() + " minutes.\n");
                
        //
        // Close the socket when done.
        //
        CloseChannel();
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
            Log("Closing socket ...\n");
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
        strToLog = "TestConn - " + strToLog;
        Logger.Log(strToLog);
    }

    public static void main(String[] args) throws Exception
    {
        int port = 0;
        String strServer = "";
        if (args.length > 1)
        {
            strServer = args[0];
            try
            {
                port = Integer.parseInt((args[1]));
            }
            catch(NumberFormatException e)
            {
            }
        }
        
        IKAIntervalTester tester = new KAHybrid();
        //IKAIntervalTester tester = new KANoTest();
        KADesktopClient client = new KADesktopClient();
            
        //(new Thread(new DataConnection(strServer, port, tester))).start();
        client.Start(strServer, port, tester);
    }    
}