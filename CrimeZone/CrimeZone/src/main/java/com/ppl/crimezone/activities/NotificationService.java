package com.ppl.crimezone.activities;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ppl.crimezone.R;
import com.ppl.crimezone.classes.CrimeReport;
import com.ppl.crimezone.classes.MapController;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NotificationService extends Service {
    private NotificationManager mNM;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LatLng location;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.local_service_started;
    private List<CrimeReport> list = null;
    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }

    @Override
    public void onCreate() {
        SharedPreferences settings = getSharedPreferences(EditProfileUI.NOTIF_PREFERENCES,
                Context.MODE_PRIVATE);


        boolean notifSetting = settings.getBoolean(EditProfileUI.NOTIF_SETTING, false);
        if(notifSetting) {
            mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            updateLocationUser();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }else {
            onDestroy();
        }
        // Display a notification about us starting.  We put an icon in the status bar.
    }

    //set the map to point to current user location
    private void updateLocationUser(){

        locationListener = new LocationListener() {
            public void onLocationChanged(Location userLocation) {
                location = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                new GetCrimeReport().execute();
                locationManager.removeUpdates(locationListener);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.notif_stop, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    /**
     *
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.dangerous);

        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, HomeMapUI.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.app_name),
                text, contentIntent);

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

    private class GetCrimeReport extends AsyncTask<Double, Void, Boolean> {

        int threshold = 3;
        @Override
        protected Boolean doInBackground(Double... params) {
            list =  MapController.getReportList(location);
            if(list!=null)return true;
            else return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result.booleanValue()) {
                if(list.size() > threshold ) showNotification();
            }

        }
    }


}