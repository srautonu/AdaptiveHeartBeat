package kaserver;

import java.io.*;
import java.net.*;

class KAClientHandler implements Runnable
{
    Socket m_sock;
    BufferedReader m_input;
    DataOutputStream m_output;
    
    KAClientHandler(Socket clientSock) throws Exception
    {
        //
        // Save the client socket and bind I/O objects to it
        //
        m_sock = clientSock;
        m_input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
        m_output = new DataOutputStream(clientSock.getOutputStream());
    }
    
    @Override public void run()
    {
        try
        {
            while(true)
            {
                String str = m_input.readLine();
                if (null == str)
                {
                    //
                    // socket has been closed from the other side
                    //
                    break;
                }
                
                Log("recv@" + m_sock.getPort() + "> " + str + "\n");

                str = str + " OK";
                m_output.writeBytes(str + "\n");
                Log("sent@" + m_sock.getPort() + "> " + str + "\n");
            }
        }
        catch(IOException e)
        {
            Log(e.toString()+"\n");
        }
        
        CloseChannel();
    }
    
    private void CloseChannel()
    {
        if (null != m_sock)
        {
            Log("Closing socket @ port " + m_sock.getPort() + "...\n");
            try
            {
                m_input.close();
                m_input = null;

                m_output.close();
                m_output = null;

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
    
    private void Log(String strToLog) { Logger.Log(strToLog); }
}
