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

import ru.qrck.liberate.te.ex.inferis.R;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity 
{

	public static final String SHARED_PREF = "ru.qrck.liberate.te.inferis.PREF";
	public static final String PASSWORD_KEY = "pwd";
	public static final String LAUNCHER_DISABLED_KEY = "ldis";
	public static final String DISCLAMER_AGREED = "agreed_with_terms";
	
	
	private DevicePolicyManager mDPM = null;
	private ComponentName mDeviceAdmin = null;
	
	private boolean displayingDisclamer = false;
    
	private void displayDisclamer(final Context ctx)
	{
		if (getIsDisclamerAgreed(ctx))
			return;
		
		if (displayingDisclamer) // already displaying, launched from another location
			return; 

		displayingDisclamer = true;

		final Activity activity = this;
		
		
        // Use the Builder class for convenient dialog construction
        new AlertDialog.Builder(this)
    		.setMessage(
    					"DISCLAMER\n"+
    					"By using this application you agree that you understand that "+
    					"main purpose of this application is to perform device factory "+
    					"reset, this would erase everything on your device!\n"+
    					"Beware of accidental triggering this funciton and make sure "+
    					"noone knows your password, so noone could remotely wipe "+
    					"your device. \n"+
    					"Author of this application takes no responsibility for "+
    					"accidental data loss.\n"+
    					"Software cames with NO WARRANTY.\n"+
    					"Before using this application you must agree with this disclamer.")
    		.setPositiveButton("Agree", 
    			new DialogInterface.OnClickListener() 
        		{
        			public void onClick(DialogInterface dialog, int id)
        			{ 
        				setDisclamerAgreed(ctx); 
        				displayingDisclamer = false;
        			}
        		})
    		.setNegativeButton("Disagree", 
    			new DialogInterface.OnClickListener() 
    			{
    				public void onClick(DialogInterface dialog, int id)
    				{
    					displayingDisclamer = false;
    					activity.finish(); 
    				}
    			})
			.create()
			.show();
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(this, MyDeviceAdminReceiver.class);

		updateControls();
	}
	
	protected void onResume()
	{
		super.onResume();
		updateControls();
	}
	
	private void updateControls()
	{

		boolean isPassword = isPasswordConfigured();
		boolean isAdmin = isActiveAdmin();
		
		displayDisclamer(this);

		Button step1 = (Button) findViewById( R.id.buttonSetPassword );
		Button step2 = (Button) findViewById( R.id.buttonEnableDeviceAdmin );
		Button step3 = (Button) findViewById( R.id.buttonDisableLauncherIcon );

		if ( isPassword )
		{
			step2.setEnabled( true );
			step1.setText(R.string.step1done);
		}
		else
		{
			step2.setEnabled( false );        	
			step1.setText(R.string.step1);
		}

		if ( isAdmin )
		{
			step3.setEnabled( true );
			step2.setText(R.string.step2done);
		}
		else
		{
			step3.setEnabled( false );
			step2.setText(R.string.step2);
		}

		if ( getLauncherDisabled(this) )
		{
			step3.setText(R.string.step3done);
		}
		else
		{
			step3.setText(R.string.step3);
		}
	}

	public void step1(View v)
	{
		Intent intent = new Intent(this, SetPasswordActivity.class);
		startActivity(intent);
	}

	public void step2(View v)
	{
		enableAdmin();
	}

	public void step3(View v)
	{
		// hide launcher icon
		PackageManager p = getPackageManager();
		
		p.setComponentEnabledSetting(
				getComponentName(), 
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
				PackageManager.DONT_KILL_APP
			);	
		
		setLauncherDisabled(this);
		
		updateControls();
		
		Toast.makeText(getApplicationContext(), "Lancher icon disabled. Reboot the device.", Toast.LENGTH_LONG).show();
	}

	
	public static void setDisclamerAgreed(Context ctx)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(DISCLAMER_AGREED, true);
		editor.commit();
	}
	
	public static Boolean getIsDisclamerAgreed(Context ctx)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		return prefs.getBoolean(DISCLAMER_AGREED, false);
	}

	
	protected static void setPassword(Context ctx, String strPassword)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PASSWORD_KEY, strPassword);
		editor.commit();
	}
	
	public static String getPassword(Context ctx)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		return prefs.getString(PASSWORD_KEY, "");
	}
	
	private boolean isPasswordConfigured()
	{
		return ! "".equals( getPassword(this) );
	}

	protected static void setLauncherDisabled(Context ctx)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		
		SharedPreferences.Editor editor = prefs.edit();
		
		editor.putBoolean(LAUNCHER_DISABLED_KEY, true);
		
		editor.commit();
	}
	
	public static boolean getLauncherDisabled(Context ctx)
	{
		SharedPreferences prefs = ctx.getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
		
		return prefs.getBoolean(LAUNCHER_DISABLED_KEY, false);
	}

	
    private boolean isActiveAdmin() 
    {
        return mDPM.isAdminActive(mDeviceAdmin);
    }


	protected void enableAdmin()
	{
        // Launch the activity to have the user enable our admin.
    
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
		intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
		intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
			"Enable Liberate.Me as admin");

		startActivityForResult(intent, 1);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	public static class MyDeviceAdminReceiver extends DeviceAdminReceiver 
	{
	}
}
