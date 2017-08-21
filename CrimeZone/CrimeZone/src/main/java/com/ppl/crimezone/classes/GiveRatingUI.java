package com.ppl.crimezone.classes;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ppl.crimezone.R;

/**
 * Created by adesudiman on 4/15/2014.
 */
public class GiveRatingUI extends Dialog implements
        View.OnClickListener{
    Activity detail;
    ImageButton star1, star2,star3,star4,star5;
    Button submitRating, cancelRating;
    double ratingScore;
    boolean submit= false;
    public GiveRatingUI(Activity a){
        super(a);
        this.detail = a;
        ratingScore = 0;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()) {
            case R.id.star1:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_nostar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 1;
                break;
            case R.id.star2:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_nostar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore =2;
                break;
            case R.id.star3:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_nostar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore= 3;
                break;
            case R.id.star4:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_nostar);
                ratingScore = 4;
                break;
            case R.id.star5:
                star1.setImageResource(R.drawable.r_yesstar);
                star2.setImageResource(R.drawable.r_yesstar);
                star3.setImageResource(R.drawable.r_yesstar);
                star4.setImageResource(R.drawable.r_yesstar);
                star5.setImageResource(R.drawable.r_yesstar);
                ratingScore = 5;
                break;
            case R.id.submitrate:
                submit = true;
                dismiss();
                break;
            case R.id.cancelrate:
                submit= false;
                dismiss();
            default:
                break;
        }
    }

    public boolean isSubmit(){
        return submit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_give_rating);
        setTitle("Give Rating");
        star1 = (ImageButton) findViewById(R.id.star1);
        star2 = (ImageButton) findViewById(R.id.star2);
        star3 = (ImageButton) findViewById(R.id.star3);
        star4 = (ImageButton) findViewById(R.id.star4);
        star5 = (ImageButton) findViewById(R.id.star5);
        submitRating = (Button) findViewById(R.id.submitrate);
        cancelRating = (Button) findViewById(R.id.cancelrate);
        submitRating.setOnClickListener(this);
        cancelRating.setOnClickListener(this);
        star1.setOnClickListener(this);
        star2.setOnClickListener(this);
        star3.setOnClickListener(this);
        star4.setOnClickListener(this);
        star5.setOnClickListener(this);
    }

    public double getNewRating(){
        return ratingScore;
    }
}