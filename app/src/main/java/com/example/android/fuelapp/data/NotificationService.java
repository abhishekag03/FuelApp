package com.example.android.fuelapp.data;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.android.fuelapp.MainActivity;
import com.example.android.fuelapp.R;
import com.example.android.fuelapp.SettingsActivity;
import com.example.android.fuelapp.TimelineActivity;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by vishaal on 2/7/17.
 */

public class NotificationService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION_PERMISSION = 122;
    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;
    public static boolean notificationIsSent = false;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void sendNotification() {
        android.support.v4.app.NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Track your fuel expenses")
                .setContentText("Filling up Fuel? Add it to your timeline.")
                .setAutoCancel(true);

        Intent resultIntent = new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.build().flags |= Notification.FLAG_AUTO_CANCEL;
        mBuilder.setContentIntent(resultPendingIntent);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        int mNotificationId = 001;

        notificationIsSent = true;

        NotificationManager mNotifymgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifymgr.notify(mNotificationId, mBuilder.build());

    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        //onHandleIntent(intent);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
        //onHandleIntent(intent);

        return START_STICKY;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            startLocationUpdates();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.d("network", e.toString());
        }
    }

    protected void startLocationUpdates() {
        Log.d("databaseService", String.valueOf(this instanceof LocationListener));
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
//            return;
//        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d("databaseService", "Location update started ..............: ");
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.d("databaseService", "onConnectionFailed in service");

    }

    @Override
    public void onLocationChanged(Location location) {

        // notificationIsSent=false;
        MainActivity.mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        getPlace();
        // The string "new_location_intent_filter" will be used to filer the intent
        Intent intent = new Intent("location_update_intent_filter");
        intent.putExtra("location_changed", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    protected void getPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                if (!placesResult.getStatus().isSuccess()) {
                    Log.e("databaseService", "Could not get places");
                }
                List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                if (placeLikelihoodList != null) {

                    MainActivity.CURRENT_LOCATION = placeLikelihoodList.get(0).getPlace().getName().toString();


                    if (placeLikelihoodList.get(0).getPlace().getPlaceTypes().contains(41) && !notificationIsSent && !MainActivity.isRunning && !TimelineActivity.isRunning && !SettingsActivity.isRunning) {
                        //   Toast.makeText(getApplicationContext(), "Fuel station found", Toast.LENGTH_SHORT).show();
                        notificationIsSent = true;
                        sendNotification();
                    }

                    for (int i = 0; i < placeLikelihoodList.size(); i++) {
                        PlaceLikelihood p = placeLikelihoodList.get(i);
                        Log.d("databaseService", p.getPlace().getName().toString() + ",place type: " + p.getPlace().getPlaceTypes().contains(41));


                    }
                } else {
                    Log.d("databaseService", "Place is null");
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("databaseService", "came to on destroy of notif service");
        Intent restartService = new Intent("RestartService");
        sendBroadcast(restartService);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
