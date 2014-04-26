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


package ru.qrck.liberate.te.ex.inferis;

import ru.qrck.liberate.te.ex.inferis.R;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class SetPasswordActivity extends Activity 
{
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */

	private EditText mPasswordView;
	private EditText mPasswordConfirmView;

	private View mSetPasswordFormView;
	private View mSetPasswordStatusView;
	private TextView mSetPasswordStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_set_password);
		setupActionBar();

		mPasswordView = (EditText) findViewById(R.id.password);

		mPasswordConfirmView = (EditText) findViewById(R.id.passwordConfirmation);

		mSetPasswordFormView = findViewById(R.id.set_password_form);
		mSetPasswordStatusView = findViewById(R.id.set_password_status);
		mSetPasswordStatusMessageView = (TextView) findViewById(R.id.set_password_status_message);

		mSetPasswordStatusMessageView.setText("Please note: not a hash but oriignal password will be stored in configuration");	
		mSetPasswordStatusMessageView.setVisibility(true ? View.VISIBLE : View.GONE);
	}

	public void setPassword(View v)
	{
		final String password1 = mPasswordView.getText().toString();
		final String password2 = mPasswordConfirmView.getText().toString();
		
		if ( password1.equals(password2))
		{
			if ( password1.length() >= 4)
			{
				if (password1.length() < 7 )
				{				
					new AlertDialog.Builder(this)
			    		.setMessage("Entered password is shorter than 7 symbols. Are you sure to use short password? Short password makes it easier to guess your password and wipe your device remotely.")
			    		.setPositiveButton("Use short", 
			    			new DialogInterface.OnClickListener() 
			        		{
			        			public void onClick(DialogInterface dialog, int id)
			        			{ 
			        				commitAndFinish(password1);
			        			}
			        		})
			    		.setNegativeButton("Cancel", 
			    			new DialogInterface.OnClickListener() 
			    			{
			    				public void onClick(DialogInterface dialog, int id)
			    				{
			    					resumeEditing(""); 
			    				}
			    			})
						.create()
						.show();
				}
				else
				{
					commitAndFinish(password1);
				}
			}
			else
			{
				resumeEditing("Password is tooo short! Min 4 chars, recommended at least 7.");	
			}
		}
		else
		{
			resumeEditing("Passwords didn't match!");	
		}
	}

	private void commitAndFinish(String password)
	{
		MainActivity.setPassword(this, password);
		mSetPasswordStatusMessageView.setText("");
		finish();

	}
	
	private void resumeEditing(String reason)
	{
		mSetPasswordStatusMessageView.setText(reason);	
		mPasswordView.requestFocus();
		mSetPasswordStatusMessageView.setVisibility(true ? View.VISIBLE : View.GONE);		
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch (item.getItemId()) 
		{
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			// TODO: If Settings has multiple levels, Up should navigate up
			// that hierarchy.
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.set_password, menu);
		return true;
	}
}
