/********************************************************************************
 * Copyright (c) 2011, Scott Ferguson
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awful;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ferg.awful.ForumsIndexActivity.AwfulForumAdapter;
import com.ferg.awful.constants.Constants;
import com.ferg.awful.network.NetworkUtils;
import com.ferg.awful.thread.AwfulForum;

public class AwfulLoginActivity extends Activity {
    private static final String TAG = "LoginActivity";

    private LoginTask mLoginTask;

    private Button mLogin;
    private EditText mUsername;
    private EditText mPassword;

    private ProgressDialog mDialog;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mLogin = (Button) findViewById(R.id.login);
        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        mLogin.setOnClickListener(onLoginClick);
        
        final ImageView image = (ImageView) findViewById(R.id.dealwithit); 
        image.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((AnimationDrawable) image.getDrawable()).start();
			}
		});
    }
    
    @Override
    public void onPause() {
        super.onPause();

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
    }
        
    @Override
    public void onStop() {
        super.onStop();

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mLoginTask != null) {
            mLoginTask.cancel(true);
        }
    }
    

    private View.OnClickListener onLoginClick = new View.OnClickListener() {
        public void onClick(View aView) {
            String username = mUsername.getText().toString();
            String password = mPassword.getText().toString();
            
            mLoginTask = new LoginTask();
            mLoginTask.execute(new String[] {username, password});
        }
    };
    
    private class LoginTask extends AsyncTask<String, Void, Boolean> {
        public void onPreExecute() {
            mDialog = ProgressDialog.show(AwfulLoginActivity.this, "Logging In", 
                "Hold on...", true);
        }

        public Boolean doInBackground(String... aParams) {
        	boolean result = false;
        	
            if (!isCancelled()) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(Constants.PARAM_USERNAME, aParams[0]);
                params.put(Constants.PARAM_PASSWORD, aParams[1]);
                params.put(Constants.PARAM_ACTION, "login");

                try {
                    NetworkUtils.post(Constants.FUNCTION_LOGIN, params);
                    result = NetworkUtils.saveLoginCookies(AwfulLoginActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                }
            }

            return result;
        }

        public void onPostExecute(Boolean aResult) {
            if (!isCancelled()) {
                boolean succeeded = aResult;

                mDialog.dismiss();

                int loginStatusResource = succeeded ? R.string.login_succeeded : R.string.login_failed;
                Toast.makeText(AwfulLoginActivity.this, loginStatusResource, Toast.LENGTH_SHORT).show();

                if(succeeded) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    setResult(Activity.RESULT_CANCELED);
                    // Not finishing - give the user another chance to log in
                }
            }
        }
    }
}
