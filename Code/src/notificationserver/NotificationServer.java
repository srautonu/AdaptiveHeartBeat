/**
 * Created by mrahman on 16-Nov-16.
 */
package notificationserver;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/*
Protocol:

client connects. Then:
CLIENT> TOKN <DEVICE_TOKEN>
SERVER> TOKN OK

For notification generator connections to the server:
CLIENT> DEVN <DEVICE_NAME>
SERVER> DEVN <DEVICE_NAME> OK
CLIENT> NTFN <TYPE> <COUNTER>
SERVER> NTFN <TYPE> <COUNTER> OK

THEN SERVER SENDS NOTIFICATIONS FOLLOWING A MODEL. THE FORMAT OF MESSAGE IS:
SERVER> NTFN <TYPE> <COUNTER>\n

Note that PING messages can be received asynchronously. That is why the
reading happens in a separate thread

*/


public class NotificationServer {
    NotificationManager notifMgr = new NotificationManager();

    public void Start(int port)
    {
        ServerSocket serverSock;

        //
        // Start the server at specified port
        //
        Log("Starting Notification Server @ port: " + port + " ...");
        try
        {
            serverSock = new ServerSocket(port);
            Log("Done.");
        }
        catch(IOException e)
        {
            Log(e);
            return;
        }

        //
        // Listen for notification clients
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
                new ReadHandler(clientSock, notifMgr);
            }
            catch(Exception e)
            {
                Log(e.toString() + "\n");
                continue;
            }
        }
    }

    private void Log(Object objToLog) { Logger.Log(objToLog); }

    public static void main(String[] args) throws Exception
    {
        int port = 8080;

        if (args.length > 0)
        {
            try
            {
                port = Integer.parseInt((args[0]));
            }
            catch(NumberFormatException e)
            {
                System.out.println(e);
            }
        }

        NotificationServer server = new NotificationServer();
        server.Start(port);
    }
}
