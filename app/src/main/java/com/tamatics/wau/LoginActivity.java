package com.tamatics.wau;

/**
 * http://www.androidbegin.com/tutorial/android-parse-com-simple-login-and-signup-tutorial/
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends Activity {
    // Declare Variables
    protected Button mLoginButton;
    protected Button mSignupButton;
    protected EditText mPassword;
    protected EditText mUsername;
    protected String mUsernameStr;
    protected String mPasswordStr;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Get the view from activity_login.xml
        setContentView(R.layout.activity_login);

        // Locate EditTexts in activity_login.xml
        mUsername = (EditText) findViewById(R.id.login_username);
        mPassword = (EditText) findViewById(R.id.login_password);

        // Locate Buttons in activity_login.xml
        mLoginButton = (Button) findViewById(R.id.login_login);
        mSignupButton = (Button) findViewById(R.id.login_signup);

        // Login Button Click Listener
        mLoginButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                setProgressBarIndeterminateVisibility(true);
                // Retrieve the text entered from the EditText
                mUsernameStr = mUsername.getText().toString();
                mPasswordStr = mPassword.getText().toString();
                // Send data to Parse.com for verification
                ParseUser.logInInBackground(mUsernameStr, mPasswordStr,
                        new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                setProgressBarIndeterminateVisibility(false);

                                if (user != null) {
                                    // If user exist and authenticated, send user to Welcome.class
                                    Intent intent = new Intent(LoginActivity.this,
                                            HomeActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(),
                                            getResources().getString(R.string.alert_welcome),
                                            Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            getResources().getString(R.string.alert_invalid_credentials),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        // Sign up Button Click Listener
        mSignupButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}