import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;

public class JavaApplication1 {
    private static void Log(Object objToLog)
    {
        Utilities.Log(objToLog);
    }

    public static void main(String[] args) throws JSONException, IOException, FileNotFoundException {
        String strDevice = "SAIFUR";
        String strType = "Messenger";
        String strPriority = "high";
        String strServer = "www.ekngine.com";
        boolean fSuccess = false;

        if (args.length >= 1)
            strDevice = args[0];
        if (args.length >= 2)
            strType = args[1];
        if (args.length >= 3)
            strServer = args[2];

        //System.out.println("Server: " + strServer + " Device: " + strDevice + " AppCategory: " + strType);
        Log("Device: " + strDevice + " AppCategory: " + strType);

        Log("Sending notification...");
        //fSuccess = Utilities.sendNotificationViaCustomServer(strServer, 5229, strDevice, strType, 0);
        fSuccess = Utilities.sendNotificationViaFCM(strPriority, strDevice, strType, 0);
        if (fSuccess)
        {
            Log("Notification sent successfully.");
        }
        else
        {
            Log("FAILED.");
        }
    }
}