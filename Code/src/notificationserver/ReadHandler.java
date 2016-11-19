package notificationserver;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

/*
Protocol:

client connects. Then:
CLIENT> DEVICE_NAME DURATION_HOURS
SERVER> DEVICE_NAME DURATION_HOURS OK

THEN SERVER SENDS NOTIFICATIONS FOLLOWING A MODEL. THE FORMAT OF MESSAGE IS:
SERVER> NTFN <TYPE> <COUNTER>\n

*/

class ReadHandler implements Runnable
{
    NotificationManager _notifMgr;
    Thread _thread;

    Socket _sock;
    BufferedReader _input;
    String _strClient = "";

    ReadHandler(Socket clientSock, NotificationManager notifMgr) throws IOException
    {
        //
        // Save the client socket and bind I/O objects to it
        //
        _sock = clientSock;
        _notifMgr = notifMgr;
        _input = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

        _thread = new Thread(this);
        _thread.start();
    }
    
    @Override public void run()
    {
        String strMessage;

        try
        {
            while(true)
            {
                strMessage = _input.readLine();
                if (strMessage == null)
                {
                    break;
                }
                Log("recv@" + _sock.getPort() + "> " + strMessage);

                if (strMessage.startsWith("NOTG "))
                {
                    processNOTG(strMessage);
                    break;
                }
                else if (strMessage.startsWith("CLNT "))
                {
                    processCLNT(strMessage);
                }
                else
                {
                    strMessage += " OK";
                    _notifMgr.queueMessage(_strClient, strMessage);
                }
            }
        }
        catch(IOException e)
        {
            Log(e);
        }
        finally {
            CloseChannel();
        }
    }

    private void processNOTG(String strMessage) throws IOException
    {
        //NOTG <DEVICE_TOKEN> <Category> <NotificationId>

        String[] strTokens = strMessage.split(" ");

        int index = strTokens[0].length() + strTokens[1].length() + 1;
        _notifMgr.queueMessage(strTokens[1], "NTFN " + strMessage.substring(index));
 
        DataOutputStream out = new DataOutputStream(_sock.getOutputStream());
        out.writeBytes("NOTG OK\n");
        Log("sent@" + _sock.getPort() + "> NOTG OK");
    }

    private void processCLNT(String strMessage)
    {
        // CLNT <DEVICE_TOKEN>

        String[] strTokens = strMessage.split(" ");

        _strClient = strTokens[1];
        _notifMgr.addConnection(_strClient, _sock);
        _notifMgr.queueMessage(_strClient, "CLNT OK");
    }

    private void CloseChannel()
    {
        if (null != _sock)
        {
            Log("Closing socket @ port " + _sock.getPort() + "...");
            try
            {
                _sock.close();
                _sock = null;
                _input = null;

                if (!_strClient.isEmpty())
                    _notifMgr.removeConnection(_strClient);

                Log("Done.");
            }
            catch(IOException e)
            {
                Log(e);
            }
        }
    }
    
    private void Log(Object objToLog) { Logger.Log(objToLog); }
}