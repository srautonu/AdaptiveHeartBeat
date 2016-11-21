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
                    Log("Notification dropped!! No connection available to " + msg._strRecipientToken);
                }
            }
            catch (InterruptedException e) {
                System.out.println(e);
            }
            catch (IOException e)
            {
                closeSocket(sock);
            }
        }
    }

    public void addConnection(String strRecipientToken, Socket sock)
    {
        if (!strRecipientToken.isEmpty() && sock != null && !sock.isClosed())
            _connectionTable.put(strRecipientToken, sock);
    }

    public void removeConnection(String strRecipientToken)
    {
        if (strRecipientToken.isEmpty())
            return;
        _connectionTable.remove(strRecipientToken);
    }

    public void queueMessage(String strRecipientToken, String strBody)
    {
        try {
            _msgQueue.put(new Message(strRecipientToken, strBody));
        } catch (InterruptedException e)
        {
            System.out.println(e);
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
            System.out.println(e);
        }

    }

    private void Log(Object objToLog) { Logger.Log(objToLog); }
}
