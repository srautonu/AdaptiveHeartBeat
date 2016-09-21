package kaserver;

import java.io.*;
import java.net.*;

public class KAServer {
    int m_listenPort = 80;
    
    public void Start(int port)
    {        
        ServerSocket serverSock;
        
        if (port != 0)
        {
            m_listenPort = port;
        }
        
        //
        // Start the server at specified port
        //
        Log("Starting server @ port: " + m_listenPort + " ...\n");
        try
        {
            serverSock = new ServerSocket(m_listenPort);
            Log("Done.\n");
        }
        catch(IOException e)
        {
            Log(e.toString() + "\n");
            return;
        }

        //
        // Listen for KA clients
        //
        while(true) {
            Socket clientSock;
            
            try
            {
                clientSock = serverSock.accept();
                Log("New connection accepted @ port " + clientSock.getPort() + "\n");
            }
            catch(IOException e)
            {
                Log(e.toString() + "\n");
                continue;
            }
            
            try
            {
                (new Thread(new KAClientHandler(clientSock))).start();
            }
            catch(Exception e)
            {
                Log(e.toString() + "\n");
                continue;
            }       
        }
    }

    private void Log(String strToLog) { Logger.Log(strToLog); }
    
    public static void main(String[] args) throws Exception
    {
        int port = 0;
        
        if (args.length > 0)
        {
            try
            {
                port = Integer.parseInt((args[0]));
            }
            catch(NumberFormatException e)
            {
            }
        }
        
        KAServer server = new KAServer();
        server.Start(port);
    }
}
