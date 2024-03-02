package com.tamatics.wau;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends Activity {
    // Declare Variables
    protected Button mSignupButton;
    protected EditText mName;
    protected EditText mPhone;
    protected EditText mPassword;
    protected EditText mUsername;
    protected String mNameStr;
    protected String mPhoneStr;
    protected String mUsernameStr;
    protected String mPasswordStr;
    protected String mPhoneStrFormatted;

    private static final String TAG = SignupActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // Get the view from activity_signup.xml
        setContentView(R.layout.activity_signup);

        // Locate EditTexts in activity_signup.xml
        mName = (EditText) findViewById(R.id.signup_name);
        mPhone = (EditText) findViewById(R.id.signup_phone);
        mUsername = (EditText) findViewById(R.id.signup_username);
        mPassword = (EditText) findViewById(R.id.signup_password);

        // Locate Button in activity_signup.xml
        mSignupButton = (Button) findViewById(R.id.signup_signup);

        // Sign up Button Click Listener
        mSignupButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Retrieve the text entered from the EditText
                mNameStr = mName.getText().toString();
                mPhoneStr = mPhone.getText().toString();
                mUsernameStr = mUsername.getText().toString();
                mPasswordStr = mPassword.getText().toString();

                // Normalize the phone number and then convert to US format
                mPhoneStrFormatted = PhoneNumberUtils.stripSeparators(mPhoneStr);

                // Force user to fill up the form
                if (mNameStr.equals("") || mUsernameStr.equals("") || mPasswordStr.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please complete all fields", Toast.LENGTH_LONG).show();
                } else if (mPhoneStrFormatted.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "Please provide a valid mobile number", Toast.LENGTH_LONG).show();
                } else {
                    saveToParse();
                }
            }
        });
    }

    // Save new user data into Parse.com Data Storage
    private void saveToParse() {
        setProgressBarIndeterminateVisibility(true);
        // Create new ParseUser
        ParseUser user = new ParseUser();
        user.setUsername(mUsernameStr);
        user.setPassword(mPasswordStr);
        user.put("name", mNameStr);
        user.put("mobileNumber", mPhoneStrFormatted);
        user.signUpInBackground(new SignUpCallback() {

            public void done(ParseException e) {
                setProgressBarIndeterminateVisibility(false);

                if (e == null) {
                    // Signup success!
                    Toast.makeText(getApplicationContext(),
                            "Registration successful",
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Signup failed
                    switch (e.getCode()) {
                        case ParseException.USERNAME_TAKEN:
                            Toast.makeText(getApplicationContext(),
                                    "Sorry, the username has already been taken",
                                        Toast.LENGTH_LONG).show();
                            break;
                        case ParseException.CONNECTION_FAILED:
                            Toast.makeText(getApplicationContext(), "Internet connection failed",
                                    Toast.LENGTH_LONG).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "Woops! Signup failed...",
                                    Toast.LENGTH_LONG).show();
                            Log.e(TAG, e.getMessage());
                            break;
                    }
                }
            }
        });
    }
}
