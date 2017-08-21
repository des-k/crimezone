package com.ppl.crimezone.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.GsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A login screen that offers login via email/password.
 */
public class UserController extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView successView;
    private TextView errorView;
    private TextView timeOutView;

    private static final String MYPREFERENCES = "UserAccount";
    private static final String FIRSTNAME = "FirstNameKey";
    private static final String LASTNAME = "LastNameKey";
    private static final String USERNAME = "usernameKey";
    private static final String EMAIL = "emailKey";
    private static final String DATEJOIN = "joinKey";
    private static final String SEX = "sexKey";
    private static final String OCCUPATION = "occuKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_controller);

        if(checkSharedPreferences()){
            Intent i = new Intent(UserController.this, HomeMapUI.class);
            finish();
            startActivity(i);
        }

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button signUpButton = (Button) findViewById(R.id.sign_up_button);
        signUpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(UserController.this, RegisterUI.class);
                finish();
                startActivity(i);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        showSuccessMessage();
    }

    private boolean checkSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("session", false);
    }

    private void showSuccessMessage(){
        boolean regSucceed = getIntent().getBooleanExtra("success", false);
        successView = (TextView) findViewById(R.id.successMessage);
        if(regSucceed){
            successView.setVisibility(View.VISIBLE);
        }
    }

    private void showErrorMessage(){
        errorView = (TextView) findViewById(R.id.wrongInputMessage);
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
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        getIntent().putExtra("success", false);


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

        // Check for an empty password.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check if password contain space
        else if(!isPasswordValid(password)){
            mPasswordView.setError(getString(R.string.error_password_format));
            focusView = mPasswordView;
            cancel = true;
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        final String EMAIL_PATTERN =
                "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }

    private boolean isPasswordValid(String password) {
        final String PASS_PATTERN = "^[a-z0-9_-]{3,15}$";

        Pattern pattern = Pattern.compile(PASS_PATTERN);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    /**
//     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
//     * the email text field with results on the main UI thread.
//     */
//    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {
//
//        @Override
//        protected List<String> doInBackground(Void... voids) {
//            ArrayList<String> emailAddressCollection = new ArrayList<String>();
//
//            // Get all emails from the user's contacts and copy them to a list.
//            ContentResolver cr = getContentResolver();
//            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
//                    null, null, null);
//            while (emailCur.moveToNext()) {
//                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
//                        .CommonDataKinds.Email.DATA));
//                emailAddressCollection.add(email);
//            }
//            emailCur.close();
//
//            return emailAddressCollection;
//        }
//
//        @Override
//        protected void onPostExecute(List<String> emailAddressCollection) {
//            addEmailsToAutoComplete(emailAddressCollection);
//        }
//    }

//    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<String>(UserController.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mEmailView.setAdapter(adapter);
//    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUser;
        private final String mPassword;
        private final String LOGIN_URL = "http://crimezone.besaba.com/webservice/login.php";

        private static final String TAG_SUCCESS = "success";

        private boolean timeOut = false;

        GsonParser parser = new GsonParser();

        UserLoginTask(String user, String password) {
            mUser = user;
            mPassword = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            // TODO: attempt authentication against a network service.

            int success;

            try {
                //Building parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", mUser));
                params.add(new BasicNameValuePair("password", mPassword));

                Log.d("Request", "Starting");

                JSONObject json = parser.makeHTTPRequest(LOGIN_URL, "POST", params);
                Log.d("MESSAGE: ", json.getString("message"));
                //Handle request time out
                if(json.getString("message").equals("timeout")){
                    Log.d("Login failed", "Request time out");
                    timeOut = !timeOut;
                    return false;
                }

                Log.d("Login attempt", json.toString());

                success = json.getInt(TAG_SUCCESS);
                Log.d("STATUS LOGIN: ", ""+json.getInt(TAG_SUCCESS));
                //Login success
                if(success == 2){
                    Log.d("Login successful", json.toString());

                    //set shared preferences
                    setSharedPreference(json);

                    //start activity
                    Intent i = new Intent(UserController.this, HomeMapUI.class);
                    finish();
                    startActivity(i);

                    return true;
                }

                //Login failed
                else{
                    Log.d("Login failure", json.toString());
                    return false;
                }
            } catch (Exception e) {
                Log.d("ERROR", e.toString());
                return false;
            }
        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if(timeOut){
                //TODO notif time out
                showTimeOutMessage();
                return;
            }

            if (success) {
                finish();
            }
            else {
                showErrorMessage();
                mPasswordView.requestFocus();
                mPasswordView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        protected void setSharedPreference(JSONObject obj){

            SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            try{
                editor.putString(FIRSTNAME, obj.getString("firstname"));
                editor.putString(LASTNAME, obj.getString("lastname"));
                editor.putString(EMAIL, obj.getString("email"));
                editor.putString(USERNAME, obj.getString("username"));
                editor.putString(DATEJOIN, obj.getString("join_date"));
                editor.putString(SEX, obj.getString("sex"));
                editor.putString(OCCUPATION, obj.getString("occupation"));

                editor.putBoolean("session", true);

                editor.commit();
            }catch(JSONException e){
                Log.e("Json Parsing Error", e.toString());
            }
        }
    }
}
