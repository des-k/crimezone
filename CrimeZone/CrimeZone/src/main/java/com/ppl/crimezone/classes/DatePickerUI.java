package com.ppl.crimezone.classes;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.HashMap;

/**
 * Created by adesudiman on 4/10/2014.
 */


public class DatePickerUI extends DialogFragment {
    Handler mHandler ;
    int year;
    int month;
    int day;

    public DatePickerUI(){}


//    public static DatePickerUI newInstance(Handler h) {
//        CheckoutFragment cf = new CheckoutFragment();
//        Bundle bundle = new Bundle();
//        bundle.putAll(h);
//        bundle.putSerializable("orders", orders);
//        cf.setArguments(bundle);
//
//        return cf;
//    }
//    public DatePickerUI(Handler h){
//        /** Getting the reference to the message handler instantiated in MainActivity class */
//        mHandler = h;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        /** Creating a bundle object to pass currently set time to the fragment */
        Bundle b = getArguments();

        /** Getting the hour of day from bundle */
        year = b.getInt("set_year");

        /** Getting the minute of hour from bundle */
        month = b.getInt("set_month");

        day = b.getInt("set_day");
        
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {


            @Override
            public void onDateSet(DatePicker view, int cYear, int cMonth, int cDay) {


                    year = cYear;
                    month = cMonth;
                    day = cDay;
                    /** Creating a bundle object to pass currently set time to the fragment */
                    Bundle b = new Bundle();

                    /** Adding currently set hour to bundle object */
                    b.putInt("set_year", year);

                    /** Adding currently set minute to bundle object */
                    b.putInt("set_month", month);

                    b.putInt("set_day", day);
                    /** Creating an instance of Message */
                    Message m = new Message();

                    /** Setting bundle object on the message object m */
                    m.setData(b);

                    /** Message m is sending using the message handler instantiated in MainActivity class */
                    mHandler.sendMessage(m);
                }
        };



        /** Opening the TimePickerDialog window */
        return  new DatePickerDialog(getActivity(), listener, year, month, day);

    }
}