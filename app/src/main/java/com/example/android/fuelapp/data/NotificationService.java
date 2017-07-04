package com.example.android.fuelapp.data;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

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


    private static final String ACTION_SHOW_NOTIFICATION = "my.app.service.action.show";
    private static final String ACTION_HIDE_NOTIFICATION = "my.app.service.action.hide";
    private static final int REQUEST_LOCATION_PERMISSION = 122;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    public static boolean notificationIsSent=false;


    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void askForPermission(final String permission, final Integer requestCode) {
        if (ContextCompat.checkSelfPermission(new MainActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(new MainActivity(), permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Location Permission is required for this app to work.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(new MainActivity(), new String[]{permission}, requestCode);
                    }
                });
                alertDialog.setNegativeButton("Cancel", null);
                AlertDialog a = alertDialog.create();
                a.show();
                Window window = a.getWindow();
                window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


            } else {

                ActivityCompat.requestPermissions(new MainActivity(), new String[]{permission}, requestCode);
            }
        } else {
            // Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
            Log.d("databaseService", ""+permission+" is already granted. ");
        }
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

        notificationIsSent=true;

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
        }
        catch (IllegalStateException e){
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
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
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        getPlace();
    }

    protected void getPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
            return;
        }


//        if(mCurrentLocation==null) {
//
//
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setMessage("Location Permission is required for this app to work. Please turn on location");
//            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//                }
//            });
//            alertDialog.setNegativeButton("Cancel", null);
//            AlertDialog a = alertDialog.create();
//            a.show();
//            Window window = a.getWindow();
//            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
//
//            // Toast.makeText(getApplicationContext(), "mCurrentLocation is null", Toast.LENGTH_SHORT).show();
//        }

        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                if (!placesResult.getStatus().isSuccess()) {
                    Log.e("databaseService", "Could not get places");
                    //AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
                    //alertDialog.setMessage("You are not connected to the internet right now. Please try again later.");
                    //alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      //  @Override
                       // public void onClick(DialogInterface dialog, int which) {
                        //}
                    //});
                    //alertDialog.setNegativeButton("Cancel", null);
                    //AlertDialog a = alertDialog.create();
                    //a.show();
                    //Window window = a.getWindow();
                    //window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    //return;
                }
                List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                if (placeLikelihoodList != null) {

                    MainActivity.CURRENT_LOCATION=placeLikelihoodList.get(0).getPlace().getName().toString();


                    if(placeLikelihoodList.get(0).getPlace().getPlaceTypes().contains(41) && !notificationIsSent && !MainActivity.isRunning && !TimelineActivity.isRunning && !SettingsActivity.isRunning){
                     //   Toast.makeText(getApplicationContext(), "Fuel station found", Toast.LENGTH_SHORT).show();
                        notificationIsSent=true;
                        sendNotification();
                    }

                    for (int i = 0; i < placeLikelihoodList.size(); i++) {
                        PlaceLikelihood p = placeLikelihoodList.get(i);
                        Log.d("databaseService", p.getPlace().getName().toString() +",place type: "+p.getPlace().getPlaceTypes().contains(41));


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
        Intent restartService= new Intent("RestartService");
        sendBroadcast(restartService);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
