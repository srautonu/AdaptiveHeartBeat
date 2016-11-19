import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by mrahman on 18-Nov-16.
 */
public class Utilities {
    private boolean sendNotification(
        String strRecipientToken,
        String strCategory,
        String strPriority,
        int notificationId
        ) throws JSONException, IOException
    {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Authorization", "key=AIzaSyCDLHCWASScdkcz9s_29UJyW6GQ4YQgVMQ");

        JSONObject message = new JSONObject();
        message.put("to", strRecipientToken);
        message.put("priority", strPriority);

        JSONObject data = new JSONObject();
        data.put("Category", strCategory);
        data.put("NotificationId", notificationId);

        message.put("data", data);
        message.put("time_to_live", 0);
        post.setEntity(new StringEntity(message.toString(), "UTF-8"));

        //
        // Start the HTTP call
        //
        Log("Sending notification> Category: " + strCategory + " NotificationId: " + notificationId);
        HttpResponse response = client.execute(post);
        Log(response.getStatusLine());

        return (200 == response.getStatusLine().getStatusCode());
    }

    //
    // Return true means notification was sent to server successfully
    //
    static boolean sendNotification2(
        String strServer,
        int port,
        String strRecipientToken,
        String strCategory,
        int notificationId
        )
    {
        Socket sock = null;
        boolean fRet = false;

        String strMessage = "NOTG " + strRecipientToken + " " + strCategory + " " + notificationId;

        //
        // Start the TCP call
        //
        try {
            sock = new Socket(strServer, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());

            Log("Sending notification> Category: " + strCategory + " NotificationId: " + notificationId);
            out.writeBytes(strMessage + "\n");

            sock.setSoTimeout(30 * 1000);
            strMessage = in.readLine();

            fRet = strMessage.equalsIgnoreCase("NOTG OK");
        }
        catch (IOException e)
        {
            Log(e);
        }
        finally {
            try {
                if (null != sock)
                    sock.close();
            }
            catch (IOException e)
            {
                Log(e);
            }
        }

        return fRet;
    }

    private static String getDeviceToken(String strDevice) throws FileNotFoundException
    {
        String strLine;
        String strDeviceToken = "";
        String[] strTokens;

        Scanner scanner = new Scanner(new FileInputStream("DeviceInfo.txt"));
        while (scanner.hasNextLine()) {
            strLine = scanner.nextLine();
            strTokens = strLine.split(" ");
            if (strTokens[0].equalsIgnoreCase(strDevice))
            {
                strDeviceToken = strTokens[1];
                break;
            }
        }

        return strDeviceToken;
    }

    static void Log(Object objToLog)
    {
        //
        // Prepare the timestamp.
        //
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
        String formattedDate = sdf.format(date);

        //
        // Log the time-stamped spew
        //
        System.out.println(formattedDate + " - " + objToLog);
    }

}
