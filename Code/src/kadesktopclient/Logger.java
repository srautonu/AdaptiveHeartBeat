package kadesktopclient;

import java.text.SimpleDateFormat;
import java.util.Date;

class Logger
{
    public static void Log(String strToLog)
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
        System.out.print(formattedDate + " - " + strToLog); 
    }
}
