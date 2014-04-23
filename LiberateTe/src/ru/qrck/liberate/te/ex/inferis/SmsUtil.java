package ru.qrck.liberate.te.ex.inferis;

import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsUtil 
{
	public static void send(String dst, String msg, long sleepMillis) 
	{
		try 
		{
			if (dst != null)
			{
				SmsManager smsMgr = SmsManager.getDefault();
				
				if (smsMgr != null)
					smsMgr.sendTextMessage( dst, null, msg, null, null);

				if (sleepMillis > 0)
					Thread.sleep(sleepMillis); // sleep for 3 seconds to give SMS chance to get delivered
			}
		}
		catch (Exception ex)
		{
			// catch everything, since we must simply try sending, if it fails - it is not the biggest deal
		}
	}
	public static void send(String dst, String msg) 
	{
		send(dst, msg, 0);
	}
	
	public static void reply(SmsMessage originalMessage, String replyText, long sleepMillis)
	{
		send(originalMessage.getOriginatingAddress(), replyText, sleepMillis);
	}

	public static void reply(SmsMessage originalMessage, String replyText)
	{
		reply(originalMessage, replyText, 0);
	}
}
