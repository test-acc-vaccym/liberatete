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
import kotlinx.android.synthetic.activity_main.*;

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
			.setPositiveButton("Agree", {
				dialog: DialogInterface?, which: Int ->
				setDisclamerAgreed(ctx)
				displayingDisclamer = false
			})
			.setNegativeButton("Disagree", {
				dialog: DialogInterface?, which: Int ->
				displayingDisclamer = false
				activity.finish()
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
		displayDisclamer(this)

		val passwordConfigured = isPasswordConfigured;

		buttonEnableDeviceAdmin.isEnabled = passwordConfigured;
		buttonSetPassword.setText(if (passwordConfigured) R.string.step1done else R.string.step1)

		val adminActive = isActiveAdmin;

		buttonDisableLauncherIcon.isEnabled = adminActive
		buttonEnableDeviceAdmin.setText(if (adminActive) R.string.step2done else R.string.step2)

		buttonDisableLauncherIcon.setText(if (getLauncherDisabled(this)) R.string.step3done else R.string.step3)
	}

	fun stepOneSetPassword(v: View)
	{
		val intent = Intent(this, SetPasswordActivity::class.java)
		startActivity(intent)
	}

	fun steTwoEnableAdmin(v: View)
	{
		enableAdmin()
	}

	fun stepThreeDisableIcon(v: View)
	{
		// hide launcher icon

		packageManager.setComponentEnabledSetting(
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

		fun Context.setPrefs(fn: SharedPreferences.Editor.() -> Unit): Unit
		{
			val prefs = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			val editor = prefs.edit()
			editor.fn();
			editor.commit()
		}

		fun <T> Context.getPrefs(fn: SharedPreferences.() -> T): T
		{
			val prefs = this.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)
			return prefs.fn()
		}

		fun setDisclamerAgreed(ctx: Context)
			= ctx.setPrefs { putBoolean(DISCLAMER_AGREED, true) }

		fun getIsDisclamerAgreed(ctx: Context): Boolean
			= ctx.getPrefs { getBoolean(DISCLAMER_AGREED, false) }

		fun setPassword(ctx: Context, strPassword: String)
			= ctx.setPrefs { putString(PASSWORD_KEY, strPassword) }

		fun getPassword(ctx: Context): String
			= ctx.getPrefs { getString(PASSWORD_KEY, "") }

		fun setLauncherDisabled(ctx: Context)
			= ctx.setPrefs { putBoolean(LAUNCHER_DISABLED_KEY, true) }

		fun getLauncherDisabled(ctx: Context): Boolean
			= ctx.getPrefs { getBoolean(LAUNCHER_DISABLED_KEY, false) }
	}
}
