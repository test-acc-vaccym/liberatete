/*
 * Copyright (c) 2013, Sergey Parshin, qrck@mail.ru
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */



package ru.qrck.liberate.te.ex.inferis;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsBroadcastReceiver extends BroadcastReceiver {
		
	public void onReceive(final Context context, Intent intent) {
	             
		Bundle pudsBundle = intent.getExtras();
		 
		Object[] pdus = (Object[]) pudsBundle.get("pdus");
		 
		SmsMessage messages = SmsMessage.createFromPdu((byte[]) pdus[0]);    
	
		String password = MainActivity.getPassword(context);
		
		if ( ! password.equals("") )
		{ 
			String messageBody = messages.getMessageBody();

			if ( messageBody.startsWith( password ) 
					&& 
				 messageBody.length() > password.length() )
			{	
				abortBroadcast();
				
				String command = messageBody.substring(password.length() + 1); // skipping space
				
				command = command.trim();
				
				if ( "WIPE".equalsIgnoreCase(command) )
				{
					wipeEverything(context, false);
				}
				else if ( "FULLWIPE".equalsIgnoreCase(command) || "FULL WIPE".equalsIgnoreCase(command) )
				{
					wipeEverything(context, true );
				}
				else if ( "REBOOT".equalsIgnoreCase(command) )
				{
					rebootDevice(context);
				}
				else if ( "TRACK".equalsIgnoreCase(command) )
				{
					sendGPSCords(context);
				}
				
			}
		}
	}

	private void wipeEverything(final Context context, final boolean fullWipe)
	{
		Thread wipeThread = new Thread() {
			public void run()
			{
				DevicePolicyManager lDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
				lDPM.wipeData( fullWipe ? DevicePolicyManager.WIPE_EXTERNAL_STORAGE : 0 );
			}
		};
		
		wipeThread.start();
	}

	private void rebootDevice(final Context context)
	{
	// not implemented yet
	}

	private void sendGPSCords(final Context context)
	{
	// not implemented yet
	}
}
