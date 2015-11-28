/*
 * Copyright (c) 2014, Sergey Parshin, quarck@gmail.com
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
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


package ru.qrck.liberate.te.ex.inferis

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.telephony.SmsMessage
import kotlin.concurrent.thread

class SmsBroadcastReceiver : BroadcastReceiver()
{
	override fun onReceive(context: Context, intent: Intent)
	{
		val pudsBundle = intent.extras

		val pdus = pudsBundle.get("pdus") as Array<Any>?

		if (pdus != null)
		{
			val messages = SmsMessage.createFromPdu(pdus[0] as ByteArray)

			val password = MainActivity.getPassword(context)

			if (password != "")
			{
				val messageBody = messages.messageBody

				if (messageBody.length > password.length
						&& messageBody.startsWith(password))
				{
					abortBroadcast()

					var command =
						messageBody
							.substring(password.length + 1)
							.trim({ it <= ' ' })
							.toUpperCase()

					handleCommand(context, command, messages);
				}
			}
		}
	}

	private fun handleCommand(context: Context, command: String, messages: SmsMessage)
	{
		when (command)
		{
			"WIPE" ->
				wipeEverything(context, messages, false)
			"FULLWIPE", "FULL WIPE" ->
				wipeEverything(context, messages, true)
			"REBOOT" ->
				rebootDevice(context, messages)
			"LOCATE" ->
				sendGPSCords(context, messages)
 			"PING" ->
				handlePingCommand(context, messages)
		}
	}

	private fun handlePingCommand(context: Context, message: SmsMessage)
	{
		SmsUtil.reply(message, "PONG")
	}

	private fun wipeEverything(context: Context, message: SmsMessage, fullWipe: Boolean)
	{
		val wipeThread =
			thread(false) {
				val lDPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
				lDPM.wipeData(if (fullWipe) DevicePolicyManager.WIPE_EXTERNAL_STORAGE else 0)
			}

		SmsUtil.reply(message, (if (fullWipe) "FULL " else "") + "WIPE started, device would be rebooted.", 3000)
		wipeThread.start()
	}

	private fun rebootDevice(context: Context, message: SmsMessage)
	{
		SmsUtil.reply(message, "Rebooting", 2000)

		val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager

		pm?.reboot(null)
	}

	private fun sendGPSCords(context: Context, message: SmsMessage)
	{
		SmsLocationReporter(message.originatingAddress).Start(context)
	}
}
