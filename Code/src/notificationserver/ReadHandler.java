package notificationserver;

import java.io.*;
import java.net.*;

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
        //NOTG <DEVICE_NAME> <Category> <NotificationId>

        String[] strTokens = strMessage.split(" ");
        String strRecipient = strTokens[1];
        String strResponse = "";

        if (strRecipient.isEmpty())
        {
            strResponse = "NOTG ERROR";
        }
        else
        {
            int index = strTokens[0].length() + strTokens[1].length() + 1;
            _notifMgr.queueMessage(strTokens[1], "NTFN " + strMessage.substring(index));
            strResponse = "NOTG OK";
        }

        DataOutputStream out = new DataOutputStream(_sock.getOutputStream());
        out.writeBytes(strResponse + "\n");
        Log("sent@" + _sock.getPort() + "> " + strResponse);
    }

    private void processCLNT(String strMessage)
    {
        // CLNT <DEVICE_TOKEN>

        String[] strTokens = strMessage.split(" ");

        _strClient = Utilities.getClientName(strTokens[1]);
        if (!_strClient.isEmpty()) {
            Log("Found client@" + _sock.getPort() + ": " + _strClient);
            _notifMgr.addConnection(_strClient, _sock);
            _notifMgr.queueMessage(_strClient, "CLNT OK");
        }
        else
        {
            //
            // If we find an unknown client, we close the socket.
            //
            Log("Unknown client@" + _sock.getPort() + ": Closing connection...");
            CloseChannel();
        }
    }

    private void CloseChannel()
    {
        if (null != _sock)
        {
            Log("Closing socket @" + _sock.getPort() + "...");
            try
            {
                _sock.close();
                _sock = null;
                _input = null;

                if (!_strClient.isEmpty())
                    _notifMgr.removeConnection(_strClient);

                Log("Socket @" + _sock.getPort() + " closed.");
            }
            catch(IOException e)
            {
                Log(e);
            }
        }
    }
    
    private void Log(Object objToLog)
    {
        Logger.Log(objToLog);
    }
}