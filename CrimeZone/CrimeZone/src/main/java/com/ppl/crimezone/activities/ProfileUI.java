package com.ppl.crimezone.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.MapController;

public class ProfileUI extends Activity {
    private String username;
    private String email;
    private String fullname;
    private String joinDate;

    private TextView fullNameView;
    private TextView usernameView;
    private TextView emailView;
    private TextView joinDateView;

    private TextView successView;

    private ImageButton backButton;
    private ImageButton settingButton;

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
        setContentView(R.layout.activity_profile_ui);


        // Get data from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
        if(sharedPreferences.contains(USERNAME)){
            username = sharedPreferences.getString(USERNAME,"");
        }
        if(sharedPreferences.contains(FIRSTNAME) && sharedPreferences.contains(LASTNAME)){
            fullname = sharedPreferences.getString(FIRSTNAME,"")+" "
                    +sharedPreferences.getString(LASTNAME,"");
        } else fullname = "-";
        if(sharedPreferences.contains(EMAIL)){
            email = sharedPreferences.getString(EMAIL,"");
        }
        if(sharedPreferences.contains(DATEJOIN)){
            joinDate = sharedPreferences.getString(DATEJOIN,"");
        }

        fullNameView = (TextView) findViewById(R.id.fullName);
        fullNameView.setText(fullname);

        usernameView = (TextView) findViewById(R.id.usernamevalue);
        usernameView.setText(username);

        emailView = (TextView) findViewById(R.id.emailvalue);
        emailView.setText(email);

        joinDateView = (TextView) findViewById(R.id.profjoined);
        joinDateView.setText(joinDate);

        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProfileUI.this, MapController.class);
                finish();
                startActivity(i);
            }
        });

        settingButton = (ImageButton) findViewById(R.id.settings);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ProfileUI.this, EditProfileUI.class);
                finish();
                startActivity(i);
            }
        });

        showSuccessMessage();
    }

    private void showSuccessMessage(){
        boolean regSucceed = getIntent().getBooleanExtra("success", false);
        successView = (TextView) findViewById(R.id.successMessage);
        if(regSucceed){
            successView.setVisibility(View.VISIBLE);
            usernameView.setText(getIntent().getStringExtra("username"));
        }
    }
}
