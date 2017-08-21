package com.ppl.crimezone.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.GsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUI extends Activity{
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mRegTask = null;

    // UI references.
    private EditText mUsernameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPassReenterView;

    private View mProgressView;
    private View mRegisterFormView;

    private TextView errorView;
    private TextView timeOutView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ui);

        // Set up the register form.
        mUsernameView = (EditText) findViewById(R.id.usernameField);
        mEmailView = (EditText) findViewById(R.id.emailField);

        mPasswordView = (EditText) findViewById(R.id.pass1);

        mPassReenterView = (EditText) findViewById(R.id.pass2);

        Button signUpButton = (Button) findViewById(R.id.signUp);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mRegisterFormView =  findViewById(R.id.sign_up_form);
        mProgressView = findViewById(R.id.sign_up_progress);
        if(mProgressView == null){
            Log.d("ERROR", "FormView null");
        }
    }

    private void showErrorMessage(){
        errorView = (TextView) findViewById(R.id.alreadyUsed);
        errorView.setVisibility(View.VISIBLE);
    }

    private void showTimeOutMessage(){
        timeOutView = (TextView) findViewById(R.id.timeOut);
        timeOutView.setVisibility(View.VISIBLE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptRegister() {
        if (mRegTask != null) {
            return;
        }

        // Reset errors.
        mUsernameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPassReenterView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passRe = mPassReenterView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_username_format));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if (!isPasswordValid(password)){
            mPasswordView.setError("Password must only contains letter and number");
            focusView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(passRe)){
            mPassReenterView.setError(getString(R.string.error_field_required));
            focusView = mPassReenterView;
            cancel = true;
        }
        else if (!isPasswordMatches(password, passRe)) {
            mPasswordView.setError(getString(R.string.error_password_unmatched));
            focusView = mPassReenterView;
            cancel = true;
        }



        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            Log.d("BEGIN", "Register Task");
            showProgress(true);
            mRegTask = new UserRegisterTask(username, email, password);
            mRegTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username){
        final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);

        return matcher.matches();
    }

    private boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private boolean isPasswordMatches(String password, String re) {
        return password.equals(re);
    }

    private boolean isPasswordValid(String password){
        final String PASS_PATTERN = "^[a-z0-9_-]{3,15}$";

        Pattern pattern = Pattern.compile(PASS_PATTERN);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }

    /**
     * Shows the progress UI and hides the Register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(RegisterUI.this, UserController.class);
        finish();
        startActivity(i);
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mEmail;
        private final String mPassword;

        private boolean timeOut = false;
        private int successStatus;

        private final String REGISTER_URL = "http://crimezone.besaba.com/webservice/register.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        GsonParser parser = new GsonParser();

        UserRegisterTask(String username, String email, String password) {
            mUser = username;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            // TODO: attempt authentication against a network service.

            Log.d("Do", "Background");
            try{
                //Building parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", mUser));
                params.add(new BasicNameValuePair("email", mEmail));
                params.add(new BasicNameValuePair("password", mPassword));

                JSONObject json = parser.makeHTTPRequest(REGISTER_URL, "POST", params);

                //Handle request time out
                if(json.getString("message").equals("timeout")){
                    Log.d("Register failed", "Request time out");
                    timeOut = !timeOut;
                    return false;
                }

                //Request status
                successStatus = json.getInt(TAG_SUCCESS);
                Log.d("Status", mUser + " " + mEmail + " " + mPassword);
                Log.d("Status", ""+json.getString(TAG_MESSAGE));
                if(successStatus == 3){
                    Log.d("Success", "Masuk Login");
                    Intent i = new Intent(RegisterUI.this, UserController.class);
                    i.putExtra("success", true);
                    finish();
                    startActivity(i);
                    return true;
                } else {
                    return false;
                }
            }catch(Exception e){
                Log.e("Error", e.toString());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mRegTask = null;
            showProgress(false);

            if(timeOut){
                showTimeOutMessage();
                return;
            }

            if (success) {
                finish();
            } else {
                showErrorMessage();
                if(successStatus == 1){
                    mUsernameView.setError("Username already used");
                    mUsernameView.requestFocus();
                    mUsernameView.setText("");
                }
                else if(successStatus == 2){
                    mEmailView.setError("Email already used");
                    mEmailView.requestFocus();
                    mEmailView.setText("");
                }
            }
        }

        @Override
        protected void onCancelled() {
            mRegTask = null;
            showProgress(false);
        }
    }
}



