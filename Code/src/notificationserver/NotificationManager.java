package notificationserver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by mrahman on 18-Nov-16.
 */
public class NotificationManager implements Runnable {
    class Message
    {
        String _strRecipientToken;
        String _strBody;

        Message(String strRecipientToken, String strBody)
        {
            _strRecipientToken = strRecipientToken;
            _strBody = strBody;
        }
    };

    Thread _thread;
    ArrayBlockingQueue<Message> _msgQueue;
    Hashtable<String, Socket> _connectionTable;

    NotificationManager()
    {
        _msgQueue = new ArrayBlockingQueue<Message>(100);
        _connectionTable = new Hashtable<String, Socket>();

        _thread = new Thread(this);
        _thread.start();
    }

    @Override
    public void run() {
        Message msg;
        Socket sock = null;
        while (true)
        {
            try {
                msg = _msgQueue.take();
                sock = _connectionTable.get(msg._strRecipientToken);
                if (sock != null && !sock.isClosed())
                {
                    DataOutputStream out = new DataOutputStream(sock.getOutputStream());
                    out.writeBytes(msg._strBody + "\n");
                    Log("sent@" + sock.getPort() + "> " + msg._strBody);
                }
                else
                {
                    Log("Queued message dropped!! No connection available to " + msg._strRecipientToken);
                }
            }
            catch (InterruptedException e) {
                Log(e);
            }
            catch (IOException e)
            {
                closeSocket(sock);
            }
        }
    }

    public void addConnection(String strRecipientToken, Socket sock)
    {
        if (!strRecipientToken.isEmpty() && sock != null && !sock.isClosed()) {
            // Remove existing connection (if any) for the recipient.
            // This will ensure a close() call on the socket that is now
            // being discarded.
            removeConnection(strRecipientToken);

            _connectionTable.put(strRecipientToken, sock);
        }
    }

    public void removeConnection(String strRecipientToken, Socket sock)
    {
//
//         Remove connection only if the mapped/stored socket
//         matches the passed in socket. Otherwise, in a race case
//         we may remove a valid socket. Here is an example scenario: Suppose
//         one socket from the client is broken. Now the client starts up another
//         socket. However, on the server side, the read exception on the former
//         socket happens after the newer socket connection has been established.
//         Now the new/valid socket is in the map, but the read exception in the
//         read handler causes us to remove connection, which will remove the newer
//         valid socket. To prevent this from happening, the caller of removeConnection
//         must pass in a socket that he wants to be removed. This is checked against
//         the stored socket. Only if a match happens, the socket is closed and removed.
//

        if (strRecipientToken.isEmpty())
            return;

        if (_connectionTable.get(strRecipientToken) == sock) {
            removeConnection(strRecipientToken);
        }
    }

    public void queueMessage(String strRecipientToken, String strBody)
    {
        try {
            _msgQueue.put(new Message(strRecipientToken, strBody));
        } catch (InterruptedException e)
        {
            Log(e);
        }
    }

    private void removeConnection(String strRecipientToken)
    {
        assert !strRecipientToken.isEmpty();

        Socket sock = _connectionTable.remove(strRecipientToken);
        try {
            sock.close();
        } catch (IOException e) {
            Log(e);
        }
    }

    private void closeSocket(Socket sock) {
        try
        {
            if (null != sock)
                sock.close();
        }
        catch (IOException e)
        {
            Log(e);
        }
    }

    private void Log(Object objToLog) { Logger.Log(objToLog); }
}
