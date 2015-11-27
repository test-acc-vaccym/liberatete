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

import ru.qrck.liberate.te.ex.inferis.R
import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class MainActivity : Activity()
{


	private var mDPM: DevicePolicyManager? = null
	private var mDeviceAdmin: ComponentName? = null

	private var displayingDisclamer = false

	private fun displayDisclamer(ctx: Context)
	{
		if (getIsDisclamerAgreed(ctx))
			return

		if (displayingDisclamer)
		// already displaying, launched from another location
			return

		displayingDisclamer = true

		val activity = this


		// Use the Builder class for convenient dialog construction
		AlertDialog
				.Builder(this)
				.setMessage(
						"DISCLAMER\nBy using this application you agree that you understand that main purpose of this application is to perform device factory reset, this would erase everything on your device!\nBeware of accidental triggering this funciton and make sure noone knows your password, so noone could remotely wipe your device. \nAuthor of this application takes no responsibility for accidental data loss.\nSoftware cames with NO WARRANTY.\nBefore using this application you must agree with this disclamer.")
				.setCancelable(false)
				.setPositiveButton("Agree", object : DialogInterface.OnClickListener
				{
					override fun onClick(dialog: DialogInterface?, which: Int)
					{
						setDisclamerAgreed(ctx)
						displayingDisclamer = false
					}
				})
				.setNegativeButton("Disagree", object : DialogInterface.OnClickListener
				{
					override fun onClick(dialog: DialogInterface?, which: Int)
					{
						displayingDisclamer = false
						activity.finish()
					}
				})
				.create()
				.show()
	}

	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
		mDeviceAdmin = ComponentName(this, MyDeviceAdminReceiver::class.java)

		updateControls()
	}

	override fun onResume()
	{
		super.onResume()
		updateControls()
	}

	private fun updateControls()
	{

		val isPassword = isPasswordConfigured
		val isAdmin = isActiveAdmin

		displayDisclamer(this)

		val step1 = findViewById(R.id.buttonSetPassword) as Button
		val step2 = findViewById(R.id.buttonEnableDeviceAdmin) as Button
		val step3 = findViewById(R.id.buttonDisableLauncherIcon) as Button

		if (isPassword)
		{
			step2.isEnabled = true
			step1.setText(R.string.step1done)
		}
		else
		{
			step2.isEnabled = false
			step1.setText(R.string.step1)
		}

		if (isAdmin)
		{
			step3.isEnabled = true
			step2.setText(R.string.step2done)
		}
		else
		{
			step3.isEnabled = false
			step2.setText(R.string.step2)
		}

		if (getLauncherDisabled(this))
		{
			step3.setText(R.string.step3done)
		}
		else
		{
			step3.setText(R.string.step3)
		}
	}

	fun step1(v: View)
	{
		val intent = Intent(this, SetPasswordActivity::class.java)
		startActivity(intent)
	}

	fun step2(v: View)
	{
		enableAdmin()
	}

	fun step3(v: View)
	{
		// hide launcher icon
		val p = packageManager

		p.setComponentEnabledSetting(
				componentName,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP)

		setLauncherDisabled(this)

		updateControls()

		Toast.makeText(applicationContext, "Lancher icon disabled. Reboot the device.", Toast.LENGTH_LONG).show()
	}

	private val isPasswordConfigured: Boolean
		get() = "" != getPassword(this)


	private val isActiveAdmin: Boolean
		get() = mDPM!!.isAdminActive(mDeviceAdmin)


	protected fun enableAdmin()
	{
		// Launch the activity to have the user enable our admin.

		val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin)
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
				"Enable Liberate.Me as admin")

		startActivityForResult(intent, 1)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.main, menu)
		return true
	}


	class MyDeviceAdminReceiver : DeviceAdminReceiver()

	companion object
	{

		val SHARED_PREF = "ru.qrck.liberate.te.inferis.PREF"
		val PASSWORD_KEY = "pwd"
		val LAUNCHER_DISABLED_KEY = "ldis"
		val DISCLAMER_AGREED = "agreed_with_terms"


		fun setDisclamerAgreed(ctx: Context)
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.putBoolean(DISCLAMER_AGREED, true)
			editor.commit()
		}

		fun getIsDisclamerAgreed(ctx: Context): Boolean
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			return prefs.getBoolean(DISCLAMER_AGREED, false)
		}


		fun setPassword(ctx: Context, strPassword: String)
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.putString(PASSWORD_KEY, strPassword)
			editor.commit()
		}

		fun getPassword(ctx: Context): String
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			return prefs.getString(PASSWORD_KEY, "")
		}

		protected fun setLauncherDisabled(ctx: Context)
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

			val editor = prefs.edit()

			editor.putBoolean(LAUNCHER_DISABLED_KEY, true)

			editor.commit()
		}

		fun getLauncherDisabled(ctx: Context): Boolean
		{
			val prefs = ctx.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

			return prefs.getBoolean(LAUNCHER_DISABLED_KEY, false)
		}
	}
}
