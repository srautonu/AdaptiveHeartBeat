import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class NotificationGenerator {
    //
    // Note: As needed, add more notification types below
    //
    NotificationInfo _rgNotification[] = {
        new NotificationInfo("Messenger", "high", 27.5),
        new NotificationInfo("Mail", "normal", 8.7),
        new NotificationInfo("Social", "normal", 4.7),
        new NotificationInfo("Calendar", "high", 4.0) // TODO: Review --> These notifications are mostly locally generated.
    };

    Random _rng;
    double _rateSum;
    String _deviceId;
    int _expDurationS;

    public NotificationGenerator(String deviceToken, int expDurationS) {
        _deviceId = deviceToken;
        _expDurationS = expDurationS;
        _rng = new Random(System.currentTimeMillis());
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

        long curTimeMS = System.currentTimeMillis();
        long endTimeMS = curTimeMS + _expDurationS * 1000;

        while(true) {
            curTimeMS = System.currentTimeMillis();
            long remainingTimeMS = (endTimeMS - curTimeMS);
            if (remainingTimeMS <= 0) {
                break;
            }
       
            Log("Remaining time: " + Math.round(remainingTimeMS / (1000 * 60.0)) + " minutes.");
            //
            // Wait this amount of sec before sending the next
            //
            double waitTimeS = -(3600.0 / _rateSum) * Math.log(_rng.nextDouble());
            if (waitTimeS * 1000 + curTimeMS > endTimeMS) {
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

        for (NotificationInfo info : _rgNotification)
        {
            System.out.println(info._strCategory + " (" + info._sendCompletedCount + ")");
        }
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
        message.put("to", _deviceId);
        message.put("priority", _rgNotification[appId]._strPriority);

        JSONObject data = new JSONObject();
        data.put("Category", _rgNotification[appId]._strCategory);
        data.put("NotificationId", _rgNotification[appId]._sendAttemptedCount++);

        message.put("data", data);
        message.put("time_to_live", 0);
        post.setEntity(new StringEntity(message.toString(), "UTF-8"));

        //
        // Start the HTTP call
        //
        Log("Sending notification> Category: " + _rgNotification[appId]._strCategory + " NotificationId: " + _rgNotification[appId]._sendAttemptedCount);
        HttpResponse response = client.execute(post);
        Log(response.getStatusLine());

        if (200 == response.getStatusLine().getStatusCode())
        {
            _rgNotification[appId]._sendCompletedCount++;
        }
    }

    private void sendNotification2(int appId) {
        _rgNotification[appId]._sendAttemptedCount++;
        boolean fSuccess = Utilities.sendNotification2(
                                "localhost",
                                5229,
                _deviceId,
                                _rgNotification[appId]._strCategory,
                                _rgNotification[appId]._sendAttemptedCount
                                );
        if (fSuccess)
        {
            _rgNotification[appId]._sendCompletedCount++;
            Log("Successful.");
        }
        else
        {
            Log("FAILED.");
        }
    }

    public static void main(String[] args) throws Exception {
        String strDeviceToken = "";
        int expDurationS;
        NotificationGenerator notGen;

        if (args.length < 2)
        {
            System.out.println("Usage: java NotificationGenerator <DeviceName> <Duration_hours>");
            return;
        }

        try {
            strDeviceToken = getDeviceToken(args[0]);
        }
        catch (FileNotFoundException e)
        {
            Log(e);
        }

        expDurationS = Integer.parseInt(args[1]) * 60 * 60;
        if (strDeviceToken.isEmpty() || expDurationS <= 0)
        {
            Log("No device token found and/or incorrect experiment duration specified.");
            return;
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
        Utilities.Log(objToLog);
    }
}

class NotificationInfo
{
    public String _strCategory;
    public String _strPriority;
    public double _dailyRate;
    public double _probability;
    public int _sendAttemptedCount;
    public int _sendCompletedCount;

    NotificationInfo(String strCategory, String strPriority, double dailyRate)
    {
        _strCategory = strCategory;
        _strPriority = strPriority;
        _dailyRate = dailyRate;
        _probability = 0.0;
        _sendAttemptedCount = 0;
        _sendCompletedCount = 0;
    }
}
