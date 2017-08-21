package com.ppl.crimezone.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.DatePickerUI;
import com.ppl.crimezone.classes.GsonParser;
import com.ppl.crimezone.classes.ReportController;
import com.ppl.crimezone.classes.TimePickerUI;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class ReportFormUI extends FragmentActivity {

    //variable for point to the map
    private SupportMapFragment mapFragment;
    private GoogleMap reportMap = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Marker placeMarker;
    private MarkerOptions place;

    //for filter
    private AutoCompleteTextView searchLocation;
    private LocationDownloadTask placesDownloadTask;
    private LocationDownloadTask placeDetailsDownloadTask;
    private LocationAddressTask placesParserTask;
    private LocationAddressTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    private EditText titleEditText, descriptionEditText;
    private Button crimeDate,crimeTime, submitButton;
    private ImageButton type [] = new ImageButton[8], backHome;
    private RelativeLayout mapContainer;
    private ImageView expandCollapseMap;
    private boolean newReportCrimeType [] = new boolean [8];
    private Calendar time;
    private LatLng location;
    private Handler dateHandler;
    private Handler timeHandler;
    //for progress bar
    //ProgressBar pb;

    private void printDate(){
        int timeInt = time.get(Calendar.DAY_OF_MONTH);
        String dayString = timeInt+"";
        if(timeInt<10)dayString = "0"+ dayString;
        timeInt = time.get(Calendar.MONTH)+1;
        String monthString = (timeInt)+"";
        if(timeInt<10)monthString= "0"+ monthString;
        crimeDate.setText(dayString + "/" + monthString + "/" + time.get(Calendar.YEAR));
    }

    private void printTIme(){
        int timeInt = time.get(Calendar.HOUR_OF_DAY);
        String hourString = timeInt+"";
        if(timeInt<10)hourString = "0"+ hourString;
        timeInt = time.get(Calendar.MINUTE);
        String minuteString = timeInt + "";
        if(timeInt < 10)minuteString = "0"+ minuteString;
        crimeTime.setText(hourString + ":" + minuteString);
    }

    private void showDateDialog(){
        time = Calendar.getInstance();
        dateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                time.set(Calendar.YEAR, b.getInt("set_year"));
                time.set(Calendar.MONTH,b.getInt("set_month"));
                time.set(Calendar.DAY_OF_MONTH, b.getInt("set_day"));
                Calendar today = Calendar.getInstance();
                if(time.getTimeInMillis() > today.getTimeInMillis()){
                    showAlertDialog("DATE NOT VALID", "Please Select Date Today Backward",0 );
                    crimeDate.setText("DD/MM/YYYY");
                }else {
                    crimeDate.setError(null);
                    printDate();
                }
            }
        };
        View.OnClickListener dateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle b = new Bundle();
                b.putInt("set_year", time.get(Calendar.YEAR));
                b.putInt("set_month", time.get(Calendar.MONTH));
                b.putInt("set_day", time.get(Calendar.DAY_OF_MONTH));
//                DatePickerUI datePicker = new DatePickerUI(dateHandler);
//                datePicker.setArguments(b);
//                FragmentManager fm = getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.add(datePicker, "date_picker");
//                ft.commit();
            }
        };
        crimeDate.setOnClickListener(dateListener);
    }


    private void showTimeDialog(){
        timeHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                time.set(Calendar.HOUR_OF_DAY, b.getInt("set_hour"));
                time.set(Calendar.MINUTE,  b.getInt("set_minute"));
                Calendar today = Calendar.getInstance();
                if(!crimeDate.getText().toString().equals("DD/MM/YYYY") && time.getTimeInMillis() > today.getTimeInMillis()){
                    showAlertDialog("TIME NOT VALID", "Please Select Tme Before Current Time", 1);
                    crimeTime.setText("HH:MM");
                }else {
                    crimeTime.setError(null);
                    printTIme();
                }
            }
        };

        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//               Bundle b = new Bundle();
//                b.putInt("set_hour", time.get(Calendar.HOUR_OF_DAY));
//                b.putInt("set_minute", time.get(Calendar.MINUTE));
//                TimePickerUI timePicker = new TimePickerUI(timeHandler);
//                timePicker.setArguments(b);
//                FragmentManager fm = getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.add(timePicker, "time_picker");
//                ft.commit();
            }
        };
        crimeTime.setOnClickListener(timeListener);
    }

    private void expandMap(double expandSize, int nextStatee){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screen_height = metrics.heightPixels;
        screen_height = (int) (expandSize * screen_height);
        ViewGroup.LayoutParams params2 = mapFragment.getView().getLayoutParams();
        params2.height = screen_height;
        mapFragment.getView().setLayoutParams(params2);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mapContainer.getLayoutParams();
        params.height = screen_height;
        mapContainer.setLayoutParams(params);
        onExpand = nextStatee;
    }

    int onExpand = 1;
    private void autoCollapsExpandMap(){
        expandCollapseMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onExpand==1) {
                    expandMap(0.72, 2);
                }else if(onExpand == 2){
                    expandMap(0, 0);
                }else {
                    expandMap(0.35, 1);
                }
            }
        });
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    titleEditText.setError(null);
                }else{
                    showTextError("Title Still Empty     ", 0);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        descriptionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals("")){
                    descriptionEditText.setError(null);
                }else{
                    showTextError("Description Still Empty     ", 1);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        descriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    expandMap(0, 0);
                }
            }
        });
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    expandMap(0, 0);
                 }
            }
        });

        searchLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (onExpand == 0) {
                    expandMap(0.35, 1);
                }
            }
        });
    }


    private void setUpButtonListener(){

        backHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backHome.setImageResource(R.drawable.back_pressed);
                finish();
            }
        });

        for(int ii=0; ii< 8; ++ii){
            newReportCrimeType[ii] = false;
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                submitButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                                submitForm();
                                            }
                                        }
        );

        for(int ii=0; ii<8; ++ii){
            type[ii].setImageResource(R.drawable.ic_nocrime);
            final int finalIi = ii;
            type[ii].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (newReportCrimeType[finalIi]) {
                                                    type[finalIi].setImageResource(R.drawable.ic_nocrime);
                                                    newReportCrimeType[finalIi] = false;
                                                }else{
                                                    type[finalIi].setImageDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
                                                    newReportCrimeType[finalIi] = true;
                                                }
                                            }
                                        }
            );
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_form_ui);
        mapFragment = (SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.new_report_map));
        reportMap = mapFragment.getMap();
        crimeDate = (Button)findViewById(R.id.crime_date);
        searchLocation = (AutoCompleteTextView) findViewById(R.id.crime_location);
        crimeTime = (Button) findViewById(R.id.crime_time);
        expandCollapseMap = (ImageView) findViewById(R.id.expand_collapse_button);
        titleEditText = (EditText) findViewById(R.id.headline);
        descriptionEditText = (EditText) findViewById(R.id.description);
        mapContainer = (RelativeLayout)findViewById(R.id.map_frame);
        backHome = (ImageButton)findViewById(R.id.back_new_report);
        submitButton = (Button) findViewById(R.id.submit);
        type[0] = (ImageButton) findViewById(R.id.drugs);
        type[1] = (ImageButton) findViewById(R.id.burglary);
        type[2] = (ImageButton) findViewById(R.id.homicide);
        type[3] = (ImageButton) findViewById(R.id.kidnap);
        type[4] = (ImageButton) findViewById(R.id.sxassault);
        type[5] = (ImageButton) findViewById(R.id.theft);
        type[6] = (ImageButton) findViewById(R.id.vehicletheft);
        type[7] = (ImageButton) findViewById(R.id.violence);

        showMap();
        showDateDialog();
        showTimeDialog();
        autoCollapsExpandMap();
        setUpButtonListener();
    }

    public void setUpMarkerListener(){
        reportMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                changeMarkerPosition(latLng.latitude, latLng.longitude);
            }
        });
    }

    public void showMap(){
        reportMap.setMyLocationEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                changeMarkerPosition(userLocation.getLatitude(), userLocation.getLongitude());
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        setUpSearchLocation();
        setUpMarkerListener();
    }


    //initialize autocomplete view
    private void setUpSearchLocation(){

        searchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new LocationDownloadTask(PLACES);
                String url = GsonParser.getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                placesDownloadTask = new LocationDownloadTask(PLACES);
                String url = GsonParser.getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
            }
        });
        // Setting an item click listener for the AutoCompleteTextView dropdown list
        searchLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new LocationDownloadTask(PLACES_DETAILS);
                // Getting url to the Google Places details api
                String url = GsonParser.getPlaceDetailsUrl(hm.get("reference"));
                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
            }
        });
        searchLocation.setThreshold(0);
    }

    // Fetches data from url passed
    private class LocationDownloadTask extends AsyncTask<String, Void, String> {
        private int downloadType=0;

        // Constructor
        public LocationDownloadTask(int type){
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                data = GsonParser.downloadUrl(url[0]);
            }catch(Exception e){
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch(downloadType){
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new LocationAddressTask(PLACES);
                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);
                    break;

                case PLACES_DETAILS :
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new LocationAddressTask(PLACES_DETAILS);
                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    private class LocationAddressTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
        int parserType = 0;

        public LocationAddressTask(int type){
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                switch(parserType){
                    case PLACES :
                        // Getting the parsed data as a List construct
                        list = GsonParser.parsePlace(jObject);
                        break;
                    case PLACES_DETAILS :
                        // Getting the parsed data as a List construct
                        list = GsonParser.parseDetailPlace(jObject);
                }
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text2 };
                    // Creating a SimpleAdapter for the AutoCompleteTextView
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, R.layout.autocomplete, from, to);
                    // Setting the adapter
                    searchLocation.setAdapter(adapter);
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);
                    changeMarkerPosition(Double.parseDouble(hm.get("lat")), Double.parseDouble(hm.get("lng")));
                    break;
            }
        }
    }

    private void changeMarkerPosition(double latitude, double longitude){
        location = new LatLng(latitude, longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(location)      // Sets the center of the map to Mountain View
                .zoom(15)                   // Sets the zoom level
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        reportMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        if(placeMarker != null)placeMarker.remove();
        place = new MarkerOptions();
        place.position(location);
        place.title("Crime Location");
        place.snippet("Drag and Drop");
        place.draggable(true);
        placeMarker = reportMap.addMarker(place);
    }

    private void showTextError(String message, int type){
        int ecolor = Color.RED; // whatever color you want
        ForegroundColorSpan fgcspan = new ForegroundColorSpan(ecolor);
        SpannableStringBuilder ssbuilder = new SpannableStringBuilder(message);
        ssbuilder.setSpan(fgcspan, 0, message.length(), 10);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(type == 0){
            titleEditText.setError(ssbuilder);
            imm.showSoftInput(titleEditText, InputMethodManager.SHOW_IMPLICIT);
            titleEditText.requestFocus();
        }else {
            descriptionEditText.setError(ssbuilder);
            imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);
            descriptionEditText.requestFocus();
        }
        submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
    }

    private void submitForm(){
        Calendar submitTime = Calendar.getInstance();
        if (titleEditText.getText().toString().equals("")) {
            showTextError("Title Still Empty     ", 0);
        }else if(descriptionEditText.getText().toString().equalsIgnoreCase("")) {
            showTextError("Description Still Empty     ", 1);
        }
        else if(crimeDate.getText().toString().equals("DD/MM/YYYY")){
            showAlertDialog("DATE NOT SET", "Please Select Valid Date First", 0);
        }else if(time.get(Calendar.YEAR) > submitTime.get(Calendar.YEAR) || time.get(Calendar.MONTH) > submitTime.get(Calendar.MONTH) || time.get(Calendar.DAY_OF_MONTH) > submitTime.get(Calendar.DAY_OF_MONTH)) {
            showAlertDialog("DATE NOT VALID", "Please Select Date Today Backward", 0);
        }else if(crimeTime.getText().toString().equals("HH:MM")) {
            showAlertDialog("TIME NOT SET", "Please Select Valid Time First", 1);
        }else if(time.getTimeInMillis() > submitTime.getTimeInMillis()) {
            showAlertDialog("TIME NOT VALID", "Please Select Tme Before Current Time", 1);
        }else {
            boolean valid = false;
            for (int ii = 0; ii < 8; ++ii) {
                if (newReportCrimeType[ii]) {
                    valid = true;
                    break;
                }
            }
            if (valid) {
                String MYPREFERENCES = "UserAccount";
                SharedPreferences sharedPreferences = getSharedPreferences(MYPREFERENCES, Context.MODE_PRIVATE);
                String username = sharedPreferences.getString("usernameKey", "");
                new SendReport().execute(ReportController.prepareNewReportData(time, submitTime, username, titleEditText.getText().toString(), descriptionEditText.getText().toString(), location, newReportCrimeType));
            } else {
                showAlertDialog("CRIME TYPE EMPTY", "Please Select At Least One Crime Type", 3);
            }
        }
    }

    private void showAlertDialog(String title, String message, int typeButton){
        switch(typeButton){
            case 0:
                crimeDate.setError("");
                break;
            case 1:
                crimeTime.setError("");
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportFormUI.this);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNegativeButton("Close",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        submitButton.setBackgroundColor(getResources().getColor(R.color.color4));
    }

    private class SendReport extends AsyncTask<String, Integer, String>{

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            return ReportController.postData(params);
        }

        protected void onPostExecute(String result){
            //pb.setVisibility(View.GONE);
            String PREFS_NAME = "ReportLocation";
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("NotHome", true);
            editor.putString("latitude", location.latitude + "");
            editor.putString("longitude", location.longitude+"");
            editor.commit();
            finish();
        }
        protected void onProgressUpdate(Integer... progress){
            //pb.setProgress(progress[0]);
        }
    }
}