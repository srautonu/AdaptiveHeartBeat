/**
 * Created by mrahman on 16-Nov-16.
 */
package notificationserver;

import java.io.*;
import java.net.*;

/*
Protocol:

client connects. Then:
CLIENT> CLNT <DEVICE_TOKEN>
SERVER> CLNT OK

For notification generator connections to the server:
CLINET> NOTG <DEVICE_TOKEN> <Category> <NotificationId>
SERVER> NOTG OK

NOTIFICATION SENT FROM SERVER TO CLIENT:
SERVER> NTFN <TYPE> <COUNTER>

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
