package com.tamatics.wau;

/**
 * http://www.androidbegin.com/tutorial/android-parse-com-simple-login-and-signup-tutorial/
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseAnonymousUtils;
import com.parse.ParseUser;

public class LauncherActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Determine whether the current user is an anonymous user
        if (ParseAnonymousUtils.isLinked(ParseUser.getCurrentUser())) {
            // If user is anonymous, send the user to LoginActivity.class
            Intent intent = new Intent(LauncherActivity.this,
                    LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // If current user is NOT anonymous user
            // Get current user data from Parse.com
            ParseUser currentUser = ParseUser.getCurrentUser();
            if (currentUser != null) {
                // Send logged in users to Welcome.class
                Intent intent = new Intent(LauncherActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Send user to LoginActivity.class
                Intent intent = new Intent(LauncherActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

    }
}