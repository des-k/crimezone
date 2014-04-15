package com.ppl.crimezone.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ppl.crimezone.R;
import com.ppl.crimezone.fragments.DatePickerDialogFragment;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.util.Log;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.maps.android.SphericalUtil;
import com.ppl.crimezone.model.MiniCrimeReport;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 com.google.maps.android:android-maps-utils
 * This is controller for HomeMap UI
 */
public class MapController extends ActionBarActivity {


    //the map variable
    private GoogleMap map = null;
    //private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayList<Marker> placeMarkers;
    private ArrayList<MarkerOptions> places;
    private LatLng location;


    //for filter
    private ArrayList<MiniCrimeReport> filterList = new ArrayList<MiniCrimeReport>();
    private boolean filterReportCrimeType[] = new boolean [8];

    AutoCompleteTextView searchLocation;
    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    boolean dateFilter;
    boolean crimeTypeFilter;

    int year_start;
    int month_start;
    int day_start;

    int year_end;
    int month_end;
    int day_end;

    List<MiniCrimeReport> reports;

    boolean crimeCategories[] = new boolean[8];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_map_ui);
        setUpFilter();
        setUpMap();

        setUpSearchLocation();

    }

    private void setUpFilter(){
        SlidingUpPanelLayout slider = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        ImageView dragArea = (ImageView) findViewById(R.id.show_filter);
        slider.setDragView(dragArea);

        setUpFilterListener();

    }

    //set up the action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_controller_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //set up the response for action bar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_report:
                openReport();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateFilterCrimeMarker(){
        LatLng far = map.getProjection().getVisibleRegion().farLeft;
        double distance = SphericalUtil.computeDistanceBetween(far, location);
       updateCrimeMarker(location.latitude, location.longitude, distance);
    }


    private void setUpFilterListener(){

        //button listener
            for(int ii=0; ii< 8; ++ii){
                filterReportCrimeType[ii] = true;
            }

            year_start = 0;
            year_end =0;
            month_start = 0;
            month_end = 0;
            day_start= 0;
            day_end = 0;

            final ImageButton type [] = new ImageButton[8];
            type[0] = (ImageButton) findViewById(R.id.drugs2);
            type[1] = (ImageButton) findViewById(R.id.burglary2);
            type[2] = (ImageButton) findViewById(R.id.homicide2);
            type[3] = (ImageButton) findViewById(R.id.kidnap2);
            type[4] = (ImageButton) findViewById(R.id.sxassault2);
            type[5] = (ImageButton) findViewById(R.id.theft2);
            type[6] = (ImageButton) findViewById(R.id.vehicletheft2);
            type[7] = (ImageButton) findViewById(R.id.violence2);

            type[0].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[0]) {
                                                   type[0].setImageResource(R.drawable.ic_drugs);
                                                   filterReportCrimeType[0] = false;
                                               }else{
                                                   type[0].setImageResource(R.drawable.nc_drugs);
                                                   filterReportCrimeType[0] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[1].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[1]) {
                                                   type[1].setImageResource(R.drawable.ic_burglary);
                                                   filterReportCrimeType[1] = false;
                                               }else{
                                                   type[1].setImageResource(R.drawable.nc_burglary);
                                                   filterReportCrimeType[1] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );

            type[2].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[2]) {
                                                   type[2].setImageResource(R.drawable.ic_homicide);
                                                   filterReportCrimeType[2] = false;
                                               }else{
                                                   type[2].setImageResource(R.drawable.nc_homicide);
                                                   filterReportCrimeType[2] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[3].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[3]) {
                                                   type[3].setImageResource(R.drawable.ic_kidnap);
                                                   filterReportCrimeType[3] = false;
                                               }else{
                                                   type[3].setImageResource(R.drawable.nc_kidnap);
                                                   filterReportCrimeType[3] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[4].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[4]) {
                                                   type[4].setImageResource(R.drawable.ic_sxassault);
                                                   filterReportCrimeType[4] = false;
                                               }else{
                                                   type[4].setImageResource(R.drawable.nc_sxassault);
                                                   filterReportCrimeType[4] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[5].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[5]) {
                                                   type[5].setImageResource(R.drawable.ic_theft);
                                                   filterReportCrimeType[5] = false;
                                               } else {
                                                   type[5].setImageResource(R.drawable.nc_theft);
                                                   filterReportCrimeType[5] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[6].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[6]) {
                                                   type[6].setImageResource(R.drawable.ic_vehicletheft);
                                                   filterReportCrimeType[6] = false;
                                               } else {
                                                   type[6].setImageResource(R.drawable.nc_vehicletheft);
                                                   filterReportCrimeType[6] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );
            type[7].setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               if (filterReportCrimeType[7]) {
                                                   type[7].setImageResource(R.drawable.ic_violence);
                                                   filterReportCrimeType[7] = false;
                                               }else{
                                                   type[7].setImageResource(R.drawable.nc_violence);
                                                   filterReportCrimeType[7] = true;
                                               }
                                               updateFilterCrimeMarker();
                                           }
                                       }
            );


        //set up date listener

        final Button startDate = (Button) findViewById(R.id.start_date);
        final Button endDate = (Button) findViewById(R.id.end_date);

        Calendar cal = Calendar.getInstance();
        year_start = cal.get(Calendar.YEAR);
        month_start = cal.get(Calendar.MONTH);
        day_start = cal.get(Calendar.DATE);
        final Handler startDateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                /** Creating a bundle object to pass currently set Time to the fragment */
                Bundle b = m.getData();

                /** Getting the year from bundle */
                int temp_year_start = b.getInt("set_year");

                /** Getting the month from bundle */
                int temp_month_start = b.getInt("set_month");

                /** Getting the day from bundle */
                int temp_day_start = b.getInt("set_day");

                /** Displaying a short time message containing time set by Time picker dialog fragment */
                boolean valid = true;

                Calendar cal = Calendar.getInstance();
                if(temp_year_start > cal.get(Calendar.YEAR)) {
                    valid = false;
                }else if(temp_year_start == cal.get(Calendar.YEAR) && temp_month_start > cal.get(Calendar.MONTH)){
                    valid = false;
                }else if(temp_year_start == cal.get(Calendar.YEAR) && temp_month_start == cal.get(Calendar.MONTH) && temp_day_start > cal.get(Calendar.DATE)){
                    valid = false;
                }


                if(valid){
                    if(!endDate.getText().toString().equals("End")){
                        if (year_end > cal.get(Calendar.YEAR)) {
                            valid = false;
                        } else if (year_end == cal.get(Calendar.YEAR) && month_end > cal.get(Calendar.MONTH)) {
                            valid = false;
                        } else if (year_end == cal.get(Calendar.YEAR) && month_end == cal.get(Calendar.MONTH) && day_end > cal.get(Calendar.DATE)) {
                            valid = false;
                        }

                        if(valid){
                            if(temp_year_start > year_end)valid = false;
                            else if(temp_year_start == year_end && temp_month_start > month_end)valid = false;
                            else if(temp_year_start == year_end && temp_month_start == month_end && temp_day_start > day_end)valid = false;

                            if(valid){
                                year_start = temp_year_start;
                                month_start = temp_month_start;
                                day_start = temp_day_start;
                                startDate.setText(day_start+"/"+(month_start+1)+"/"+year_start);
                                updateFilterCrimeMarker();
                            }else{
                                startDate.performClick();
                            }
                        }else {
                            endDate.performClick();
                        }
                    }else{
                        year_start = temp_year_start;
                        month_start = temp_month_start;
                        day_start = temp_day_start;
                        startDate.setText(day_start+"/"+(month_start+1)+"/"+year_start);
                    }
                }else {
                    startDate.performClick();
                }
            }
        };

        /** Click Event Handler for button */
        View.OnClickListener startDateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                /** Adding currently set hour to bundle object */
                b.putInt("set_year", year_start);

                /** Adding currently set minute to bundle object */
                b.putInt("set_month", month_start);

                b.putInt("set_day", day_start);
                /** Instantiating TimePickerDialogFragment */
                DatePickerDialogFragment datePicker = new DatePickerDialogFragment(startDateHandler);

                /** Setting the bundle object on timepicker fragment */
                datePicker.setArguments(b);

                /** Getting fragment manger for this activity */
                FragmentManager fm = getSupportFragmentManager();

                /** Starting a fragment transaction */
                FragmentTransaction ft = fm.beginTransaction();

                /** Adding the fragment object to the fragment transaction */
                ft.add(datePicker, "date_picker");

                /** Opening the TimePicker fragment */
                ft.commit();
            }
        };
        startDate.setOnClickListener(startDateListener);

        year_end = cal.get(Calendar.YEAR);
        month_end = cal.get(Calendar.MONTH);
        day_end = cal.get(Calendar.DATE);
        final Handler endDateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();

                /** Getting the year from bundle */
                int temp_year_end = b.getInt("set_year");

                /** Getting the month from bundle */
                int temp_month_end = b.getInt("set_month");

                /** Getting the day from bundle */
                int temp_day_end = b.getInt("set_day");

                /** Displaying a short time message containing time set by Time picker dialog fragment */
                boolean valid = true;

                Calendar cal = Calendar.getInstance();
                if(temp_year_end > cal.get(Calendar.YEAR)) {
                    valid = false;
                }else if(temp_year_end == cal.get(Calendar.YEAR) && temp_month_end > cal.get(Calendar.MONTH)){
                    valid = false;
                }else if(temp_year_end == cal.get(Calendar.YEAR) && temp_month_end == cal.get(Calendar.MONTH) && temp_day_end > cal.get(Calendar.DATE)){
                    valid = false;
                }


                if(valid){
                    if(!startDate.getText().toString().equals("Start")){
                        if (year_start > cal.get(Calendar.YEAR)) {
                            valid = false;
                        } else if (year_start == cal.get(Calendar.YEAR) && month_start > cal.get(Calendar.MONTH)) {
                            valid = false;
                        } else if (year_start == cal.get(Calendar.YEAR) && month_start == cal.get(Calendar.MONTH) && day_start > cal.get(Calendar.DATE)) {
                            valid = false;
                        }

                        if(valid){
                            if(temp_year_end < year_start)valid = false;
                            else if(temp_year_end == year_start && temp_month_end < month_start)valid = false;
                            else if(temp_year_end == year_start && temp_month_end == month_start && temp_day_end < day_start)valid = false;

                            if(valid){
                                year_end = temp_year_end;
                                month_end = temp_month_end;
                                day_end = temp_day_end;
                                endDate.setText(day_end+"/"+(month_end+1)+"/"+year_end);
                                updateFilterCrimeMarker();
                            }else{
                                endDate.performClick();
                            }
                        }else {
                            startDate.performClick();
                        }
                    }else{
                        year_end = temp_year_end;
                        month_end = temp_month_end;
                        day_end = temp_day_end;
                        endDate.setText(day_end + "/" + (month_end + 1) + "/" + year_end);
                    }
                }else {
                    endDate.performClick();
                }
            }
        };

        /** Click Event Handler for button */
        View.OnClickListener endDateListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                /** Adding currently set hour to bundle object */
                b.putInt("set_year", year_end);

                /** Adding currently set minute to bundle object */
                b.putInt("set_month", month_end);

                b.putInt("set_day", day_end);
                /** Instantiating TimePickerDialogFragment */
                DatePickerDialogFragment datePicker = new DatePickerDialogFragment(endDateHandler);

                /** Setting the bundle object on timepicker fragment */
                datePicker.setArguments(b);

                /** Getting fragment manger for this activity */
                FragmentManager fm = getSupportFragmentManager();

                /** Starting a fragment transaction */
                FragmentTransaction ft = fm.beginTransaction();

                /** Adding the fragment object to the fragment transaction */
                ft.add(datePicker, "date_picker");

                /** Opening the TimePicker fragment */
                ft.commit();
            }
        };
        endDate.setOnClickListener(endDateListener);

    }




    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart(){
        super.onStart();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateLocationUser();

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }


    @Override
    protected void onResume() {
        super.onResume();
        final String PREFS_NAME = "NewReportLocation";


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        double latitude =  Double.parseDouble(settings.getString("latitude", 100000+""));
        double longitude =  Double.parseDouble(settings.getString("longitude", 100000+""));

        if((int)latitude != 100000 || (int) longitude!=100000 ){
            location = new LatLng(latitude, longitude);

            LatLng target = new LatLng(location.latitude, location.longitude);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(target)      // Sets the center of the map to Mountain View
                    .zoom(13)                   // Sets the zoom level
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            LatLng far = map.getProjection().getVisibleRegion().farLeft;
            double distance = SphericalUtil.computeDistanceBetween(far, target);
            updateCrimeMarker(location.latitude, location.longitude, distance );
        }



    }


    @Override
    public void onPause() {
        super.onPause();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    //method for action bar moving to other activities
    public void openReport()
    {
        String PREFS_NAME = "ReporControllerMode";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("NewReportMode", true);

        // Commit the edits!
        editor.commit();

        Intent intent = new Intent(this, ReportController.class);
        startActivity(intent);
    }

    public void openSettings()
    {

    }

    //initialize map
    private void setUpMap(){
        if(map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.setMyLocationEnabled(true);
            map.setPadding(0,0,0,20);
            setUpMapListener();
        }
    }
    //add listener if user swap or zoom the map
    private void setUpMapListener(){
        map.setOnCameraChangeListener
                (   new GoogleMap.OnCameraChangeListener()
                    {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition)
                        {
                            LatLng far = map.getProjection().getVisibleRegion().farLeft;
                            double distance = SphericalUtil.computeDistanceBetween(far, cameraPosition.target);
                            updateCrimeMarker(location.latitude, location.longitude, distance);
                        }
                    }
                );
    }

    //set the map to point to current user location
    private void updateLocationUser(){
        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                // Called when a new location is found by the network location provider.
                //location
                location = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(location)      // Sets the center of the map to Mountain View
                        .zoom(13)                   // Sets the zoom level
                        .bearing(0)                // Sets the orientation of the camera to east
                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                LatLng far = map.getProjection().getVisibleRegion().farLeft;
                double distance = SphericalUtil.computeDistanceBetween(far, location);
                updateCrimeMarker(location.latitude, location.longitude, distance );
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    /*
        Using http  post
    private void updateCrimeMarker(String latitude, String longitude, double distance){
        String url;
        try {
            url = "http:/http://crimezone.besaba.com/webservice/crimeLocation.php
            new GetCrimeReport().execute(url);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }u
     */
    private void updateCrimeMarker(double latitude, double longitude, double distance){
        String url;

        url = "http://crimezone.besaba.com/webservice/crimeLocation.php?distance="+distance
                +"&latitude="+ latitude+
                "&longitude=" +longitude;
        Log.d("String url" , url);
        new GetCrimeReport().execute(url);
    }

    //initialize autocomplete view
    private void setUpSearchLocation(){
        if(searchLocation == null) {
            searchLocation = (AutoCompleteTextView) findViewById(R.id.atv_places);
            searchLocation.setThreshold(1);
            setUpSearchLocationListener();
        }
    }

    private void setUpSearchLocationListener(){
        // Adding textchange listener
        searchLocation.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

                // Getting url to the Google Places Autocomplete api
                String url = GsonParser.getAutoCompleteUrl(s.toString());

                // Start downloading Google Places
                // This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        // Setting an item click listener for the AutoCompleteTextView dropdown list
        searchLocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

                // Creating a DownloadTask to download Places details of the selected place
                placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

                // Getting url to the Google Places details api
                String url = GsonParser.getPlaceDetailsUrl(hm.get("reference"));

                // Start downloading Google Place Details
                // This causes to execute doInBackground() of DownloadTask class
                placeDetailsDownloadTask.execute(url);
            }
        });
    }


    private void setFilter(){
        filterList = new ArrayList<MiniCrimeReport>();
        Log.d("report size", reports.size()+"");
        for(int ii=0; ii< reports.size(); ++ii){
            MiniCrimeReport temp = new MiniCrimeReport(reports.get(ii).getTitle(), reports.get(ii).getCrimeTimeStart(), reports.get(ii).getCategories(), reports.get(ii).getLatitude(), reports.get(ii).getLongitude());
            if(!filterReportCrimeType[0]){
                temp.remove("0");
            }
            if(!filterReportCrimeType[1]){
                temp.remove("1");
            }
            if(!filterReportCrimeType[2]){
                temp.remove("2");
            }
            if(!filterReportCrimeType[3]){
                temp.remove("3");
            }
            if(!filterReportCrimeType[4]){
                temp.remove("4");
            }
            if(!filterReportCrimeType[5]){
                temp.remove("5");
            }
            if(!filterReportCrimeType[6]){
                temp.remove("6");
            }
            if(!filterReportCrimeType[7]){
                temp.remove("7");
            }
            boolean valid = true;
            final Button startDate = (Button) findViewById(R.id.start_date);
            final Button endDate = (Button) findViewById(R.id.end_date);
            if(!(startDate.getText().toString().equals("Start") ||  endDate.getText().toString().equals("End"))){
                Log.d("year check start", temp.getCrimeTimeStart().getYear()+" "+ year_start);
                Log.d("year check end", temp.getCrimeTimeStart().getYear()+" "+ year_end);
                Log.d("month check start", temp.getCrimeTimeStart().getMonth()+" "+ month_start);
                Log.d("month check end", temp.getCrimeTimeStart().getMonth()+" "+ month_end);
                Log.d("date check start", temp.getCrimeTimeStart().getDate()+" "+ day_start);
                Log.d("date check end", temp.getCrimeTimeStart().getDate()+" "+ day_end);

                if (temp.getCrimeTimeStart().getYear()+1900 < year_start) valid = false;
                else if (temp.getCrimeTimeStart().getYear()+1900 > year_end) valid = false;
                else if (temp.getCrimeTimeStart().getYear()+1900 == year_start && temp.getCrimeTimeStart().getMonth() < (month_start))
                {
                    valid = false;
                    Log.d("date check", "1");
                }
                else if (temp.getCrimeTimeStart().getYear()+1900 == year_start && temp.getCrimeTimeStart().getMonth() == (month_start) && temp.getCrimeTimeStart().getDate() < day_start)
                {
                    valid = false;
                    Log.d("date check", "2");
                }
                else if (temp.getCrimeTimeStart().getYear()+1900 == year_end && temp.getCrimeTimeStart().getMonth() >(month_end))
                {
                    valid = false;
                    Log.d("date check", "3");
                }
                else if (temp.getCrimeTimeStart().getYear()+1900 == year_end && temp.getCrimeTimeStart().getMonth() == (month_end) && temp.getCrimeTimeStart().getDate() > day_end)
                {
                    valid = false;
                    Log.d("date check", "4");
                }
            }
            Log.d("valid", valid+"");
            Log.d("loop "+ii, temp.printCategories());
            if(valid && temp.getCategories().size()>0){
                filterList.add(temp);
                Log.d("filterlist size", filterList.size()+"");
            }
        }
    }
    private void handleReportList(List<MiniCrimeReport> updateReports){
        reports = updateReports;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setFilter();
                if (placeMarkers != null) {
                    for (int pm = 0; pm < placeMarkers.size(); pm++) {
                        if (placeMarkers.get(pm) != null)
                            placeMarkers.get(pm).remove();
                    }
                }
                placeMarkers = new ArrayList<Marker>();
                places = new ArrayList<MarkerOptions>();
                int currIcon = R.drawable.mk;


                for (int ii = 0; ii < filterList.size(); ++ii)
                {
                    //Log.d("loop "+ ii+" ", filterList.get(ii).printCategories());
                    switch (filterList.get(ii).getCategories().size()) {
                        case 1:
                            if (filterList.get(ii).getCategories().get(0).equals("0")) {
                                currIcon = R.drawable.mk_drugs;
                            } else if (filterList.get(ii).getCategories().get(0).equals("1")) {
                                currIcon = R.drawable.mk_burglary;
                            } else if (filterList.get(ii).getCategories().get(0).equals("2")) {
                                currIcon = R.drawable.mk_homicide;
                            } else if (filterList.get(ii).getCategories().get(0).equals("3")) {
                                currIcon = R.drawable.mk_kidnap;
                            } else if (filterList.get(ii).getCategories().get(0).equals("4")) {
                                currIcon = R.drawable.mk_sxassault;
                            } else if (filterList.get(ii).getCategories().get(0).equals("5")) {
                                currIcon = R.drawable.mk_theft;
                            } else if (filterList.get(ii).getCategories().get(0).equals("6")) {
                                currIcon = R.drawable.mk_vehicletheft;
                            }else if (filterList.get(ii).getCategories().get(0).equals("7")) {
                                currIcon = R.drawable.mk_violence;
                            }
                            break;
                        case 2:
                            currIcon = R.drawable.mk_2;
                            break;
                        case 3:
                            currIcon = R.drawable.mk_3;
                            break;
                        case 4:
                            currIcon = R.drawable.mk_4;
                            break;
                        case 5:
                            currIcon = R.drawable.mk;
                            break;
                        case 6:
                            currIcon = R.drawable.mk;
                            break;
                        case 7:
                            currIcon = R.drawable.mk;
                            break;
                        case 8:
                            currIcon = R.drawable.mk;
                            break;
                        default:
                            currIcon = R.drawable.mk;

                    }


                    places.add(new MarkerOptions()
                            .position(new LatLng(filterList.get(ii).getLatitude(), filterList.get(ii).getLongitude()))
                            .title(filterList.get(ii).getTitle())
                            .icon(BitmapDescriptorFactory.fromResource(currIcon)));
                    placeMarkers.add(map.addMarker(places.get(ii)));
                    Log.d("place size at "+ii, places.size()+"");
                    Log.d("place marker size at "+ii, placeMarkers.size()+"");
                }
            }
        });
    }

    //fetch and parse crime report data
    private class GetCrimeReport extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... placesURL) {
            StringBuilder placesBuilder = new StringBuilder();
            //process search parameter string(s)
            Log.d("background", "get in");

            for (String placeSearchURL : placesURL) {
                //execute search
                Log.d("url place search", placeSearchURL);
                HttpClient placesClient = new DefaultHttpClient();
                try {
                    //try to fetch the data
                    HttpGet placesGet = new HttpGet(placeSearchURL);
                    HttpResponse placesResponse = placesClient.execute(placesGet);
                    StatusLine placeSearchStatus = placesResponse.getStatusLine();
                    Log.d("Status Code Connection ", placeSearchStatus.getStatusCode()+"");
                    if (placeSearchStatus.getStatusCode() == 200) {
                        //we have an OK response
                        HttpEntity entity = placesResponse.getEntity();
                        InputStream content = entity.getContent();

                        try {
                            //Read the server response and attempt to parse it as JSON
                            Reader reader = new InputStreamReader(content);
                            GsonBuilder gsonBuilder = new GsonBuilder();
                            gsonBuilder.setDateFormat("d/MM/yyyy HH:mm");
                            Gson gson = gsonBuilder.create();
                            List<MiniCrimeReport> miniReports = new ArrayList<MiniCrimeReport>();
                            Log.d("gson : ", "sebelum gson from json");


                            miniReports = Arrays.asList(gson.fromJson(reader, MiniCrimeReport[].class));
                            for (MiniCrimeReport p : miniReports) {
                                //Log.d("Report ", p.getTitle()+", "+p.getLatitude()+", "+ p.getLongitude()+ ","+p.getCrimeTimeStart().toString() + p);
                                Log.d("Report", p.toString());
                            }
                            content.close();
                            handleReportList(miniReports);
                        } catch (Exception ex) {
                            Log.e("Exception", "Error " +ex);
                            //failedLoadingPosts();
                        }
                    } else {
                        //Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                        //failedLoadingPosts();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            return placesBuilder.toString();
        }



        @Override
        protected void onPostExecute(String result) {
            Log.d("Result on Post: ", result);
            //parse place data returned from Google Places
        }
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        private int downloadType=0;

        // Constructor
        public DownloadTask(int type){
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
                Log.d("Background Task",e.toString());
            }
            Log.d("Hasil Data ", data);
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            switch(downloadType){
                case PLACES:
                    // Creating ParserTask for parsing Google Places
                    placesParserTask = new ParserTask(PLACES);

                    // Start parsing google places json data
                    // This causes to execute doInBackground() of ParserTask class
                    placesParserTask.execute(result);
                    break;

                case PLACES_DETAILS :
                    // Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

                    // Starting Parsing the JSON string
                    // This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        int parserType = 0;

        public ParserTask(int type){
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

                    // Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

                    // Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

                    // Getting reference to the SupportMapFragment of the activity_main.xml
                    SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

                    // Getting GoogleMap from SupportMapFragment

                    LatLng point = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

                    // Showing the user input location in the Google Map
                    map.moveCamera(cameraPosition);
                    map.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

                    // Adding the marker in the Google Map
                    map.addMarker(options);

                    break;
            }
        }
    }
}