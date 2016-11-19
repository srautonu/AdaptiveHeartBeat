/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package notificationserver;

import java.text.SimpleDateFormat;
import java.util.Date;

class Logger
{
    public static void Log(Object objToLog)
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
        System.out.println(formattedDate + " - " + objToLog.toString());
    }
}
