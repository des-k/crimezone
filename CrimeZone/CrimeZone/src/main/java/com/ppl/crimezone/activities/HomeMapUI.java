package com.ppl.crimezone.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.CrimeReport;
import com.ppl.crimezone.classes.DatePickerUI;
import com.ppl.crimezone.classes.GsonParser;;
import com.ppl.crimezone.classes.MapController;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is controller for HomeMap UI
 */
public class HomeMapUI extends ActionBarActivity {

    //the map variable
    private GoogleMap map = null;
    //private Location location;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private ArrayList<Marker> placeMarkers;
    private ArrayList<MarkerOptions> places;

    //for filter
    private AutoCompleteTextView searchLocation;
    private DownloadTask placesDownloadTask;
    private DownloadTask placeDetailsDownloadTask;
    private ParserTask placesParserTask;
    private ParserTask placeDetailsParserTask;

    final int PLACES=0;
    final int PLACES_DETAILS=1;

    private SlidingUpPanelLayout slider;
    private ImageView dragArea;
    private ImageButton type [];
    private Button startDate ;
    private Button endDate;


    private Handler startDateHandler;
    private Handler endDateHandler;

    MapController mapController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_map_ui);
        mapController = new MapController();
        slider = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        dragArea = (ImageView) findViewById(R.id.show_filter);
        type = new ImageButton[8];
        type[0] = (ImageButton) findViewById(R.id.drugs2);
        type[1] = (ImageButton) findViewById(R.id.burglary2);
        type[2] = (ImageButton) findViewById(R.id.homicide2);
        type[3] = (ImageButton) findViewById(R.id.kidnap2);
        type[4] = (ImageButton) findViewById(R.id.sxassault2);
        type[5] = (ImageButton) findViewById(R.id.theft2);
        type[6] = (ImageButton) findViewById(R.id.vehicletheft2);
        type[7] = (ImageButton) findViewById(R.id.violence2);
        searchLocation = (AutoCompleteTextView) findViewById(R.id.atv_places);
        startDate = (Button) findViewById(R.id.start_date);
        endDate = (Button) findViewById(R.id.end_date);
        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

        //set up for the first time report location to false\
        String PREFS_NAME = "ReportLocation";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("NotHome", false);
        editor.commit();

        setUpMap();
        setUpFilter();
        setUpSearchLocation();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        updateLocationUser();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    //initialize map
    private void setUpMap(){
        map.setMyLocationEnabled(true);
        map.setPadding(0,0,0,30);
        map.setOnCameraChangeListener
                (new GoogleMap.OnCameraChangeListener() {
                     @Override
                     public void onCameraChange(CameraPosition cameraPosition) {
                         changeLocation(cameraPosition);
                     }
                 }
                );
        map.setOnInfoWindowClickListener
                (
                        new GoogleMap.OnInfoWindowClickListener()
                        {

                            @Override
                            public void onInfoWindowClick(Marker marker) {
                                String PREFS_NAME = "ReportIdentifier";
                                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                SharedPreferences.Editor editor = settings.edit();
                                editor.putString("reportId", mapController.getIdReport(marker)+"");
                                editor.commit();
                                Intent intent = new Intent(HomeMapUI.this, DetailReportUI.class);
                                startActivity(intent);
                            }
                        }
                );
    }


    private void changeLocation(CameraPosition cameraPosition){
        mapController.setLocation(cameraPosition.target.latitude, cameraPosition.target.longitude);
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        LatLng far = map.getProjection().getVisibleRegion().farLeft;
        double distance = SphericalUtil.computeDistanceBetween(far, mapController.getLocation());
        new GetCrimeReport().execute(distance);
    }
    private void changeLocation(double latitude, double longitude){
        mapController.setLocation(latitude, longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mapController.getLocation())      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom level
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        LatLng far = map.getProjection().getVisibleRegion().farLeft;
        double distance = SphericalUtil.computeDistanceBetween(far, mapController.getLocation());
        new GetCrimeReport().execute(distance);
    }

    private void showAlertDialog(String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(HomeMapUI.this);
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
    }

    private View.OnClickListener getDateListener(final int day, final int month, final int year, final Handler handler){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Creating a bundle object to pass currently set time to the fragment */
                Bundle b = new Bundle();

                b.putInt("set_day", day);
                b.putInt("set_month", month);
                b.putInt("set_year", year);

                /** Instantiating TimePickerDialogFragment */
                DatePickerUI datePicker = new DatePickerUI(handler);

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
    }

    private void setUpFilter(){
        slider.setDragView(dragArea);
        for(byte ii=0; ii<8; ++ii){
            final byte finalIi = ii;
            type[ii].setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Log.d("onclick", mapController.getFilterType(finalIi)+"");
                                                if (mapController.getFilterType(finalIi)) {
                                                    type[finalIi].setImageResource(android.R.color.transparent);
                                                    mapController.setFilterType(finalIi, false);
                                                }else{
                                                    type[finalIi].setImageResource(R.drawable.ic_nocrime);
                                                    mapController.setFilterType(finalIi, true);
                                                }
                                                drawReportList();
                                            }
                                        }
            );
        }

        //set up date listener
        startDateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                if(mapController.setStartDate(b.getInt("set_day"), b.getInt("set_year"),b.getInt("set_month"))){
                    drawReportList();
                    startDate.setText(mapController.printStartDate());
                }else{
                    if(mapController.resetStartDate())drawReportList();
                    showAlertDialog("START DATE NOT VALID", "Please Select Valid Date Before Current Date");
                    startDate.setText("Start");
                }
            }
        };

        startDate.setOnClickListener(getDateListener(mapController.getStartDate(0), mapController.getStartDate(1), mapController.getStartDate(2), startDateHandler));
        startDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mapController.resetStartDate())drawReportList();
                startDate.setText("Start");
                return true;
            }
        });

        endDateHandler= new Handler(){
            @Override
            public void handleMessage(Message m){
                Bundle b = m.getData();
                if(mapController.setEndDate(b.getInt("set_day"), b.getInt("set_year"), b.getInt("set_month"))){
                    drawReportList();
                    endDate.setText(mapController.printEndDate());
                }else{
                    if(mapController.resetEndDate())drawReportList();
                    showAlertDialog("END DATE NOT VALID", "Please Select Date Before Current Date");
                    startDate.setText("End");
                }
            }
        };

        endDate.setOnClickListener(getDateListener(mapController.getEndDate(0), mapController.getEndDate(1), mapController.getEndDate(2), endDateHandler));
        endDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mapController.resetEndDate())drawReportList();
                endDate.setText("Start");
                return true;
            }
        });
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_report:
                intent = new Intent(this, ReportFormUI.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, ProfileUI.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String PREFS_NAME = "ReportLocation";
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean backHome = settings.getBoolean("NotHome", false);
        if(backHome) {
            double latitude = Double.parseDouble(settings.getString("latitude", 100000 + ""));
            double longitude = Double.parseDouble(settings.getString("longitude", 100000 + ""));

            if ((int) latitude != 100000 || (int) longitude != 100000) {
                changeLocation(latitude, longitude);
            }
        }
    }


    //set the map to point to current user location
    private void updateLocationUser(){

        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                changeLocation(userLocation.getLatitude(), userLocation.getLongitude());
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    //initialize autocomplete view
    private void setUpSearchLocation(){
        searchLocation.setThreshold(0);
        // Adding textchange listener
        searchLocation.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = GsonParser.getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                placesDownloadTask = new DownloadTask(PLACES);
                String url = GsonParser.getAutoCompleteUrl(s.toString());
                placesDownloadTask.execute(url);
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

    private void drawReportList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (placeMarkers != null) {
                    for (int pm = 0; pm < placeMarkers.size(); pm++) {
                        if (placeMarkers.get(pm) != null)
                            placeMarkers.get(pm).remove();
                    }
                }
                places = new ArrayList<MarkerOptions>();
                placeMarkers = new ArrayList<Marker>();
                int ii=0;
                for (Integer idReport : mapController.getFilterList().keySet()) {
                    int currIcon;
                    CrimeReport report = mapController.getCrimeReport(idReport);
                    switch (report.getCategories().size()) {
                        case 1:
                            switch(report.getCategories().get(0)) {
                                case 0:
                                    currIcon = R.drawable.mk_drugs;
                                    break;
                                case 1:
                                    currIcon = R.drawable.mk_burglary;
                                    break;
                                case 2:
                                    currIcon = R.drawable.mk_homicide;
                                    break;
                                case 3:
                                    currIcon = R.drawable.mk_kidnap;
                                    break;
                                case 4:
                                    currIcon = R.drawable.mk_sxassault;
                                    break;
                                case 5:
                                    currIcon = R.drawable.mk_theft;
                                    break;
                                case 6:
                                    currIcon = R.drawable.mk_vehicletheft;
                                    break;
                                default:
                                    currIcon = R.drawable.mk_violence;
                                    break;
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
                            currIcon = R.drawable.mk_5;
                            break;
                        case 6:
                            currIcon = R.drawable.mk_6;
                            break;
                        case 7:
                            currIcon = R.drawable.mk_7;
                            break;
                        default:
                            currIcon = R.drawable.mk_8;
                            break;
                    }

                    places.add(new MarkerOptions()
                            .position(new LatLng(report.getLatitude(), report.getLongitude()))
                            .title(report.getTitle())
                            .icon(BitmapDescriptorFactory.fromResource(currIcon)));
                    Marker mark = map.addMarker(places.get(ii));
                    placeMarkers.add(mark);
                    mapController.addMarkerToCrimeReport(mark, report);
                    ii++;
                }
            }
        });
    }

    //fetch and parse crime report data
    private class GetCrimeReport extends AsyncTask<Double, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Double... params) {
            if(mapController.getReportList(params[0]))drawReportList();
            else {
                return false;
                //notification about connection
            }
            return true;
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
                    changeLocation( Double.parseDouble(hm.get("lat")),  Double.parseDouble(hm.get("lng")) );
                   break;
            }
        }
    }
}