package com.ppl.crimezone.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.GiveRatingUI;
import com.ppl.crimezone.classes.ReportController;

public class DetailReportUI extends Activity  {

    TextView title, time, author, description, locationDescription;
    LinearLayout starContainer, crimeTypeContainer;
    ImageButton back, giveRate;
    GiveRatingUI rateUI;

    ReportController reportController;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail_ui);
        title = (TextView) findViewById(R.id.headline_detail);
        time = (TextView) findViewById(R.id.time_detail);
        author = (TextView) findViewById(R.id.author);
        description = (TextView) findViewById(R.id.description_detail);
        locationDescription = (TextView) findViewById(R.id.latlang);
        crimeTypeContainer = (LinearLayout) findViewById(R.id.type_container);
        starContainer = (LinearLayout) findViewById(R.id.star_container);
        back = (ImageButton) findViewById(R.id.back_detail);
        giveRate = (ImageButton) findViewById(R.id.rate_detail);
        reportController = new ReportController();

        View.OnClickListener backListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.setImageResource(R.drawable.back_pressed);
                String PREFS_NAME = "ReportLocation";
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("NotHome", true);
                editor.putString("latitude", reportController.getDetailReport().getLatitude() + "");
                editor.putString("longitude", reportController.getDetailReport().getLongitude() + "");
                editor.commit();
                finish();
            }
        };

        View.OnClickListener rateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveRate.setImageResource(R.drawable.r_yesstar);
                rateUI = new GiveRatingUI(DetailReportUI.this);
                rateUI.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
                rateUI.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (rateUI.isSubmit()) {
                            new FetchRatingTask().execute();

                        }
                        giveRate.setImageResource(R.drawable.r_star);
                    }
                });
                rateUI.show();
            }
        };

        back.setOnClickListener(backListener);

        giveRate.setOnClickListener(rateListener);

        new FetchDetailTask().execute();
    }



    private class FetchRatingTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL){
            String MYPREFERENCES = "UserAccount";
            SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);

            String username = sharedPreferences.getString("usernameKey", "");

            if(reportController.updateRating(username)){
                drawStarThread();
            }else {
                //failed
            }
            return "";
        }
    }
    private class FetchDetailTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL){
            final String PREFS_NAME = "ReportIdentifier";

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            String reportId = settings.getString("reportId", "-1");

            if(reportController.fetchReportDetail(reportId))
                showDetailReport();
            else {
                finish();
            }
            return "";
        }
    }

    public void showDetailReport(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //init UI
                title = (TextView) findViewById(R.id.headline_detail);

                title.setText(reportController.getDetailReport().getTitle());

                time.setText(reportController.getDetailReport().printDate());
                author.setText(reportController.getDetailReport().getUsername());
                description.setText(reportController.getDetailReport().getDescription());

                for(Byte x: reportController.getDetailReport().getCategories()){
                    ImageView typeCrime = new ImageView(DetailReportUI.this);
                    byte idType = x;
                    switch (idType){
                        case 0:
                            typeCrime.setImageResource(R.drawable.ic_drugs);
                            break;
                        case 1:
                            typeCrime.setImageResource(R.drawable.ic_burglary);
                            break;
                        case 2:
                            typeCrime.setImageResource(R.drawable.ic_homicide);
                            break;
                        case 3:
                            typeCrime.setImageResource(R.drawable.ic_kidnap);
                            break;
                        case 4:
                            typeCrime.setImageResource(R.drawable.ic_sxassault);
                            break;
                        case 5:
                            typeCrime.setImageResource(R.drawable.ic_theft);
                            break;
                        case 6:
                            typeCrime.setImageResource(R.drawable.ic_vehicletheft);
                            break;
                        case 7:
                            typeCrime.setImageResource(R.drawable.ic_violence);
                            break;
                    }
                    crimeTypeContainer.addView(typeCrime);
                }
                locationDescription.setText("lat: "+reportController.getDetailReport().getLatitude()+ ", long "+ reportController.getDetailReport().getLongitude());
                drawStar();//Your code to run in GUI thread here
            }//public void run() {
        });
    }

    public void drawStarThread(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawStar();
            }
        });
    }

    private void drawStar()
    {
        starContainer.removeAllViewsInLayout();
        int x = (int)reportController.getDetailReport().getAvgScore();
        for(int ii=0; ii< x; ++ii){
            ImageView starii = new ImageView(this);
            starii.setImageResource(R.drawable.r_yesstar);
            if(ii==0){
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(90, 0, 0, 0);
                starii.setLayoutParams(lp);
            }
            starContainer.addView(starii);

        }
        if(Math.abs(reportController.getDetailReport().getAvgScore()-(x+0.5))< Math.abs(Math.round(reportController.getDetailReport().getAvgScore()) - reportController.getDetailReport().getAvgScore())){
            ImageView halfStar = new ImageView(this);
            halfStar.setImageResource(R.drawable.r_halfstar);
            starContainer.addView(halfStar);
        }else {
            if(Math.round(reportController.getDetailReport().getAvgScore()) == x+1){
                ImageView fullStar = new ImageView(this);
                fullStar.setImageResource(R.drawable.r_yesstar);
                starContainer.addView(fullStar);
            }
        }
    }
}