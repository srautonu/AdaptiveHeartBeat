package notificationserver;

import java.io.*;
import java.util.Scanner;

import static notificationserver.Logger.Log;

/**
 * Created by mrahman on 18-Nov-16.
 */
public class Utilities {

    public static String getClientName(String strToken)
    {
        String strLine;
        String strClientName = "";
        String[] strTokens;
        Scanner scanner = null;

        try {
            scanner = new Scanner(new FileInputStream("DeviceInfo.txt"));
        } catch (FileNotFoundException e)
        {
            Log(e);
            return strClientName;
        }

        while (scanner.hasNextLine()) {
            strLine = scanner.nextLine();
            strTokens = strLine.split(" ");
            if (strTokens[0].equalsIgnoreCase(strToken))
            {
                strClientName = strTokens[0];
                break;
            }
        }

        return strClientName;
    }
}
