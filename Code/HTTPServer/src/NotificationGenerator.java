
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

public class NotificationGenerator {
    //
    // Note: As needed, add more notification types below
    //
    NotificationInfo _rgNotification[] = {
        new NotificationInfo("WhatsApp", "high", 19.9),
        new NotificationInfo("Gmail", "normal", 7.0),
        new NotificationInfo("SMS/MMS", "normal", 4.2),
        new NotificationInfo("Facebook", "high", 3.7),
        new NotificationInfo("Facebook Messanger", "high", 3.4),
        new NotificationInfo("Email", "normal", 1.7),
        new NotificationInfo("Google Calendar", "high", 1.6),
        new NotificationInfo("To-Do List", "high", 1.5),
        new NotificationInfo("Google Plus", "high", 1.0),
        new NotificationInfo("Calendar", "high", 0.9)
    };

    Random _rng;
    double _rateSum;
    String _deviceToken;
    int _expDurationS;

    public NotificationGenerator(String deviceToken, int expDurationS) {
        _deviceToken = deviceToken;
        _expDurationS = expDurationS;
        _rng = new Random(50);
        _rateSum = 0.0;

        for (int i = 0; i < _rgNotification.length; i++) {
            _rateSum += _rgNotification[i]._dailyRate;
        }

        for(int i = 0; i < _rgNotification.length; i++){
            _rgNotification[i]._probability = _rgNotification[i]._dailyRate / _rateSum;
        }

        _rateSum /= 24; // Convert to hourly rate.
    }

    public void run() {
        Log("Experiment Started.");

        long endTimeMS = System.currentTimeMillis() + _expDurationS * 1000;

        while(true) {
            long remainingTimeMS = (endTimeMS - System.currentTimeMillis());
            if (remainingTimeMS <= 0) {
                break;
            }
       
            Log("Remaining time: " + Math.round(remainingTimeMS / (1000 * 60.0)) + " minutes.");
            //
            // Wait this amount of sec before sending the next
            //
            double waitTimeS = -(3600.0 / _rateSum) * Math.log(_rng.nextDouble());
            if (waitTimeS * 1000 + System.currentTimeMillis() > endTimeMS) {
                Log("Next waiting time (" + Math.round(waitTimeS/60.0) + " minute(s)) is longer than remaining time.");
                break;
            }

            Log("Waiting " + Math.round(waitTimeS/60.0) + " minute(s) before sending next notificatoin.");
            try {
                Thread.sleep((int) waitTimeS * 1000);
            } catch (InterruptedException e) {
                Log(e);
            }

            // Choose the notification type and send
            int app = whichone();
            try {
                sendNotification(app);
            } catch (JSONException e) {
                Log(e);
            } catch (IOException e) {
                Log(e);
            }
        }

        Log("Experiment Ended.");
    }

    private int whichone() {
        double prob = _rng.nextDouble();
        double probSum = 0;
        for(int i = 0; i < _rgNotification.length; i++) {
            probSum = probSum + _rgNotification[i]._probability;
            if (probSum >= prob)
                return i;
        }
        return -1; // Invalid ! This should never happen
    }

    private void sendNotification(int appId) throws JSONException, IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("https://fcm.googleapis.com/fcm/send");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Authorization", "key=AIzaSyCDLHCWASScdkcz9s_29UJyW6GQ4YQgVMQ");

        JSONObject message = new JSONObject();
        message.put("to", _deviceToken);
        message.put("priority", _rgNotification[appId]._strPriority);

        JSONObject data = new JSONObject();
        data.put("AppName", _rgNotification[appId]._strAppName);
        data.put("NotificationId", _rgNotification[appId]._sendAttemptedCount);

        message.put("data", data);
        message.put("time_to_live", 60);
        post.setEntity(new StringEntity(message.toString(), "UTF-8"));

        //
        // Start the HTTP call
        //
        _rgNotification[appId]._sendAttemptedCount++;

        Log("Sending notification> AppName: " + _rgNotification[appId]._strAppName + " NotificationId: " + _rgNotification[appId]._sendAttemptedCount);
        HttpResponse response = client.execute(post);
        Log(response.getStatusLine());

        if (200 == response.getStatusLine().getStatusCode())
        {
            _rgNotification[appId]._sendCompletedCount++;
        }
    }

    public static void main(String[] args) throws Exception {
        String strDeviceToken = "";
        int expDurationS = 24 * 60 * 60;

        if (args.length == 0)
        {
            System.out.println("Usage: java NotificationGenerator <DeviceName>.");
            return;
        }

        try {
            strDeviceToken = getDeviceToken(args[0]);
        }
        catch (FileNotFoundException e)
        {
            Log(e);
        }

        new NotificationGenerator(strDeviceToken, expDurationS).run();
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

    private static void Log(Object objToLog)
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

class NotificationInfo
{
    public String _strAppName;
    public String _strPriority;
    public double _dailyRate;
    public double _probability;
    public int _sendAttemptedCount;
    public int _sendCompletedCount;

    NotificationInfo(String strAppName, String strPriority, double dailyRate)
    {
        _strAppName = strAppName;
        _strPriority = strPriority;
        _dailyRate = dailyRate;
        _probability = 0.0;
        _sendAttemptedCount = 0;
        _sendCompletedCount = 0;
    }
}
