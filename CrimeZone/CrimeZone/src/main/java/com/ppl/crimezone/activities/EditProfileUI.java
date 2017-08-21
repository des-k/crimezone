package com.ppl.crimezone.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.GsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class EditProfileUI extends Activity {
    private SaveSettingsTask saveTask = null;

    private String username;
    private String email;
    private String firstname;
    private String lastname;

    private EditText firstNameView;
    private EditText lastNameView;
    private EditText usernameView;
    private EditText emailView;
    private EditText joinDateView;

    private Button saveButton;
    private Button logoutButton;

    private View savingProgressView;
    private View editProfileView;

    private static final String MYPREFERENCES = "UserAccount";
    private static final String FIRSTNAME = "FirstNameKey";
    private static final String LASTNAME = "LastNameKey";
    private static final String USERNAME = "usernameKey";
    private static final String EMAIL = "emailKey";

    static final String NOTIF_PREFERENCES  = "NotifPreferences";

    static final String NOTIF_SETTING  = "NotifSetting";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);
        final ToggleButton notifButton = (ToggleButton)findViewById(R.id.notif);
        // Get data from shared preferences
        SharedPreferences settings = getSharedPreferences(NOTIF_PREFERENCES, 0);
        boolean notifSetting = settings.getBoolean(NOTIF_SETTING, false);
        Log.d("notifSetting", notifSetting+"");
        notifButton.setChecked(notifSetting);
        notifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("notif", notifButton.isChecked()+"");
                if(!notifButton.isChecked()){
                    SharedPreferences settings = getSharedPreferences(NOTIF_PREFERENCES, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(NOTIF_SETTING, false);
                    editor.commit();
                    //stop service
                    Context ctx = getApplicationContext();
                    Intent intent = new Intent(ctx, NotificationService.class);
                    PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, intent, 0);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                    alarmManager.cancel(sender);
                    stopService(intent);
                }else{
                    SharedPreferences settings = getSharedPreferences(NOTIF_PREFERENCES, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(NOTIF_SETTING, true);
                    editor.commit();
                    //start service

                    Calendar cal = Calendar.getInstance();
                    Intent intent = new Intent(EditProfileUI.this, NotificationService.class);
                    PendingIntent pintent = PendingIntent.getService(EditProfileUI.this, 0, intent, 0);
                    AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Log.d("Main",String.valueOf( cal.getTimeInMillis()));
                    //make the alarm goes off every 10 sec (not exact help to save battery life)
                    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 300000, pintent);
                }
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(USERNAME)){
            username = sharedPreferences.getString(USERNAME,"");
        }
        if(sharedPreferences.contains(FIRSTNAME)){
            firstname = sharedPreferences.getString(FIRSTNAME,"");
        } else firstname = "-";
        if(sharedPreferences.contains(LASTNAME)){
            lastname = sharedPreferences.getString(LASTNAME,"");
        } else lastname = "-";
        if(sharedPreferences.contains(EMAIL)){
            email = sharedPreferences.getString(EMAIL,"");
        }

        firstNameView = (EditText) findViewById(R.id.firstNameEdit);
        firstNameView.setHint(firstname);

        lastNameView = (EditText) findViewById(R.id.lastNameEdit);
        lastNameView.setHint(lastname);

        usernameView = (EditText) findViewById(R.id.usernameEdit);
        usernameView.setHint(username);

        saveButton = (Button) findViewById(R.id.savesettings);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSaveSettings();
            }
        });

        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogout();
            }
        });

        savingProgressView = findViewById(R.id.saving_progress);
        editProfileView = findViewById(R.id.editProfileForm);
    }

    public void attemptLogout(){
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("session", false);
        editor.commit();

        Intent i = new Intent(EditProfileUI.this, UserController.class);
        finish();
        startActivity(i);
    }

    private void attemptSaveSettings() {
        if(saveTask != null){
            return;
        }

        //reset errors
        firstNameView.setError(null);
        lastNameView.setError(null);
        usernameView.setError(null);

        String newUsername = usernameView.getText().toString();
        String newFirstname = firstNameView.getText().toString();
        String newLastname = lastNameView.getText().toString();

        boolean cancel = false;

        // Check for a valid username.
        if (TextUtils.isEmpty(newUsername)) {
            usernameView.setError(getString(R.string.error_field_required));
            usernameView.requestFocus();
            cancel = true;
        } else if(!isUsernameValid(newUsername)){
            usernameView.setError(getString(R.string.error_username_format));
            cancel = true;
        }
        // Check for a valid firstname.
        if (TextUtils.isEmpty(newFirstname)) {
            firstNameView.setError(getString(R.string.error_field_required));
            firstNameView.requestFocus();
            cancel = true;
        }
        // Check for a valid lastname.
        if (TextUtils.isEmpty(newLastname)) {
            lastNameView.setError(getString(R.string.error_field_required));
            lastNameView.requestFocus();
            cancel = true;
        }

        if(!cancel){
            Log.d("Save settings begin", "BISMILLAH");
            showProgress(true);

            saveTask = new SaveSettingsTask(username, newUsername, newFirstname, newLastname);
            saveTask.execute((Void) null);
        }
    }

    public boolean isUsernameValid(String username){
        return !username.contains(" ");
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

            editProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
            editProfileView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    editProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            savingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            savingProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    savingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            savingProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            editProfileView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class SaveSettingsTask extends AsyncTask<Void, Void, Boolean>{

        private final String mUserOld;
        private final String mUser;
        private final String mFirstname;
        private final String mLastname;

        private boolean timeOut = false;
        private int successStatus;

        private final String REGISTER_URL = "http://crimezone.besaba.com/webservice/updateProfile.php";
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";
        GsonParser parser = new GsonParser();

        public SaveSettingsTask(String oldUsername, String username, String firstname, String lastname) {
            mUserOld = oldUsername;
            mUser = username;
            mFirstname = firstname;
            mLastname = lastname;
        }



        @Override
        protected Boolean doInBackground(Void... args) {
            Log.d("Do", "Background");

            try{
                //Building parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("username", mUserOld));
                params.add(new BasicNameValuePair("newUsername", mUser));
                params.add(new BasicNameValuePair("firstname", mFirstname));
                params.add(new BasicNameValuePair("lastname", mLastname));

                JSONObject json = parser.makeHTTPRequest(REGISTER_URL, "POST", params);

                //Handle request time out
                if(json.getString("message").equals("timeout")){
                    Log.d("Register failed", "Request time out");
                    timeOut = !timeOut;
                    return false;
                }

                //Request status
                successStatus = json.getInt(TAG_SUCCESS);

                if(successStatus == 2){
                    Log.d("Success", "Berhasil di update");
                    Intent i = new Intent(EditProfileUI.this, ProfileUI.class);
                    i.putExtra("success", true);
                    i.putExtra("username", mUser);
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
            saveTask = null;
            showProgress(false);

            if(timeOut){
                //TODO show time out message
                return;
            }
            if(success){
                finish();
            } else {
                //TODO show username error message
                usernameView.setError("Username is already used");
                usernameView.requestFocus();
                usernameView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            saveTask = null;
            showProgress(false);
        }
    }
}
