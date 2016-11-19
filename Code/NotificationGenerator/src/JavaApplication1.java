import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaApplication1 {
    public static boolean notificationsender(String key, String deviceToken, String type,String priority, int notificationId) throws JSONException, IOException{
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Authorization", "key=" + key);
        JSONObject message = new JSONObject();

        //
        // My device Token
        //
        if (deviceToken.isEmpty())
             deviceToken = "d7MqluRWjG0:APA91bE5nDr2RlLWIn_PISSu8ouocOVaM8dFQX-9Y6tUI10-A6iRnZtkxi5m53WBHfDwzstfjdF4ljQI4STsKQ8Iso346Dshp9BiJgFZx8CdJFC1ijFqKqoRbqA2tD5jqMx3rSd-Cw8j";
        message.put("to", deviceToken);
        message.put("priority", priority);

        JSONObject data = new JSONObject();
        data.put("Category", type);
        data.put("NotificationId", notificationId);

        message.put("data", data);
        message.put("time_to_live", 0);
        post.setEntity(new StringEntity(message.toString(), "UTF-8"));

        Log("Sending notification...");
        HttpResponse response = client.execute(post);
        if (200 == response.getStatusLine().getStatusCode())
        {
            Log("Notification sent successfully.");
        }
        else
        {
            Log("FAILED - " + response.getStatusLine().toString());
        }
        
        return false;
    }

    public static void notificationsender2(
        String deviceToken,
        String category,
        String server
        )
    {
        Log("Sending notification...");

        boolean fSuccess = Utilities.sendNotification2(
                            server,
                            5229,
                            deviceToken,
                            category,
                            0
                            );
        if (fSuccess)
        {
            Log("Notification sent successfully.");
        }
        else
        {
            Log("FAILED.");
        }
    }

    private static void Log(Object objToLog)
    {
        Utilities.Log(objToLog);
    }

    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException {
        String strDevice = "SAIFUR";
        String strType = "Messenger";
        String strPriority = "normal";
        String strServer = "www.ekngine.com";

        //int notificationId = 0;

        if (args.length >= 1)
            strDevice = args[0];
        if (args.length >= 2)
            strType = args[1];
        if (args.length >= 3)
            strServer = args[2];

        System.out.println("Server: " + strServer + "Device: " + strDevice + " AppCategory: " + strType);

        String strDeviceToken = Utilities.getDeviceToken(strDevice);
        //notificationsender("AIzaSyCDLHCWASScdkcz9s_29UJyW6GQ4YQgVMQ", strDeviceToken, strType, strPriority, notificationId);
        notificationsender2(strDeviceToken, strType, strServer);
    }
}