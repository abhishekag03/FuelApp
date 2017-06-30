package com.example.android.fuelapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.PlacesResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.vision.text.Text;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_LOCATION_PERMISSION = 122;


    public String TAG = "database";

    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    private String CURRENT_LOCATION;
    private int CURRENT_COST;
    private double CURRENT_RATE;
    private String CURRENT_FUEL_TYPE;
    private double CURRENT_LITRES;
    private String CURRENT_FAVOURITE;

    private static boolean preferencesUpdated=false;


    private EditText currentFuelTextview;
    private TextView currentLitresTextView;
    private TextView currentRateTextView;
    private TextView favouriteFuelTextView;
    private TextView frequentFuelTextView;
    private TextView lastUsedFuelTextView;


    private TextView priceHolder;
    private TextView litresHolder;
    private TextView rateHolder;
    private TextView favouriteHolder;
    private TextView frequentHolder;
    private TextView lastUsedHolder;

    private FloatingActionButton fillButton;

    private void askForPermission(final String permission, final Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Location Permission is required for this app to work.");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
                    }
                });
                alertDialog.setNegativeButton("Cancel", null);
                AlertDialog a = alertDialog.create();
                a.show();
                Window window = a.getWindow();
                window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_secondary1);
        //getSupportActionBar().hide();



        priceHolder=(TextView) findViewById(R.id.price_text_view);
        litresHolder=(TextView) findViewById(R.id.litres_text_view);
        rateHolder=(TextView) findViewById(R.id.rate_text_view);
        favouriteHolder=(TextView) findViewById(R.id.favourite_text_view);
        frequentHolder=(TextView) findViewById(R.id.frequent_text_view);
        lastUsedHolder=(TextView) findViewById(R.id.last_used_text_view);

        Typeface oratorSTD=Typeface.createFromAsset(getAssets(), "fonts/OratorStd.otf");
        Typeface segment7=Typeface.createFromAsset(getAssets(), "fonts/Segment7Standard.otf");

        priceHolder.setTypeface(oratorSTD);
        litresHolder.setTypeface(oratorSTD);
        rateHolder.setTypeface(oratorSTD);
        favouriteHolder.setTypeface(oratorSTD);
        frequentHolder.setTypeface(oratorSTD);
        lastUsedHolder.setTypeface(oratorSTD);


        currentFuelTextview=(EditText) findViewById(R.id.current_fuel_cost);
        currentLitresTextView=(TextView) findViewById(R.id.current_fuel_litres);
        currentRateTextView=(TextView) findViewById(R.id.current_fuel_rate);
        favouriteFuelTextView=(TextView) findViewById(R.id.favourite_fuel_cost);
        frequentFuelTextView=(TextView) findViewById(R.id.most_used_fuel_cost);
        lastUsedFuelTextView=(TextView) findViewById(R.id.last_fuel_cost);


        currentFuelTextview.setTypeface(segment7);
        currentRateTextView.setTypeface(segment7);
        currentLitresTextView.setTypeface(segment7);
        favouriteFuelTextView.setTypeface(segment7);
        frequentFuelTextView.setTypeface(segment7);
        lastUsedFuelTextView.setTypeface(segment7);



        getCurrentRate();


        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        CURRENT_FUEL_TYPE=sharedPreferences.getString("FuelType", "Petrol");
        CURRENT_FAVOURITE=sharedPreferences.getString("favourite", "100");

        favouriteFuelTextView.setText(CURRENT_FAVOURITE);

        currentFuelTextview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length()>0){
                    CURRENT_COST=Integer.parseInt(stripNonDigits(s.toString().trim()));
                    CURRENT_LITRES= CURRENT_COST/CURRENT_RATE;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLitresTextView.setText(String.valueOf(String.format("%.2f", CURRENT_LITRES)));
                if (s.length() > 0) {
                    CURRENT_COST = Integer.parseInt(stripNonDigits(s.toString().trim()));
                    CURRENT_LITRES = CURRENT_COST / CURRENT_RATE;
                    Log.d(TAG, CURRENT_COST + " " + CURRENT_LITRES);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
             if(s.length()>0){
                currentLitresTextView.setText(String.valueOf(String.format("%.2f",CURRENT_LITRES)));
                CURRENT_COST=Integer.parseInt(stripNonDigits(s.toString().trim()));
                CURRENT_LITRES= CURRENT_COST/CURRENT_RATE;
                Log.d(TAG, CURRENT_COST+" "+CURRENT_LITRES);
            }

            }
        });

        askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);



        fillButton = (FloatingActionButton) findViewById(R.id.fill_fuel_button);

        if (!GooglePlayServicesAvailable()) {
            finish();
        }

        createLocationRequest();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Awareness.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        fillButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }



    private void sendNotification()
    {
        android.support.v4.app.NotificationCompat.Builder mBuilder= new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("First Notification")
                .setContentText("hello world");

        Intent resultIntent= new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId= 001;

        NotificationManager mNotifymgr= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifymgr.notify(mNotificationId, mBuilder.build());

    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            currentFuelTextview.setText(CURRENT_LOCATION+" "+ mLastUpdateTime);
        } else {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Location Permission is required for this app to work. Please turn on location");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog.setNegativeButton("Cancel", null);
            AlertDialog a = alertDialog.create();
            a.show();
            Window window = a.getWindow();
            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            Toast.makeText(getApplicationContext(), "mCurrentLocation is null", Toast.LENGTH_SHORT).show();
        }
    }

    /*  Returns a dialog to address the provided errorCode. The returned dialog displays a localized
     message about the error and upon user confirmation (by tapping on dialog) will direct them to
     the Play Store if Google Play services is out of date or missing, or to system settings if
     Google Play services is disabled on the device.
     */
    private boolean GooglePlayServicesAvailable() {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int status = googleApi.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            googleApi.getErrorDialog(this, status, 0).show();
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        if(preferencesUpdated)
        {
            preferencesUpdated=false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences= android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {

                case REQUEST_LOCATION_PERMISSION:
                    Toast.makeText(getApplicationContext(), "Permission allowed", Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        //updateUI();
        getPlace();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        Log.d(TAG, String.valueOf(this instanceof LocationListener));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("You are not connected to the internet right now. Please try again later.");
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.setNegativeButton("Cancel", null);
        AlertDialog a = alertDialog.create();
        a.show();
        Window window = a.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


        Toast.makeText(getApplicationContext(), "You are not connected to the internet right now. Please try again.", Toast.LENGTH_SHORT).show();
    }


    protected void getPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
            return;
        }


        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                if (!placesResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Could not get places");
                    return;
                }
                List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                if (placeLikelihoodList != null) {

                    CURRENT_LOCATION=placeLikelihoodList.get(0).getPlace().getName().toString();

                    for (int i = 0; i < placeLikelihoodList.size(); i++) {
                        PlaceLikelihood p = placeLikelihoodList.get(i);
                        Log.d(TAG, p.getPlace().getName().toString() +",place type: "+p.getPlace().getPlaceTypes().contains(41));



                        if(p.getPlace().getPlaceTypes().contains(41)){
                            Toast.makeText(getApplicationContext(), "Fuel station found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.d(TAG, "Place is null");
                }

            }
        });

    }

    public static String stripNonDigits(
            final CharSequence input ){
        final StringBuilder sb = new StringBuilder(
                input.length() );
        for(int i = 0; i < input.length(); i++){
            final char c = input.charAt(i);
            if(c > 47 && c < 58){
                sb.append(c);
            }
        }
        return sb.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();

        if(id==R.id.action_settings)
        {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCurrentRate()
    {
            String Url="http://fuelpriceindia.herokuapp.com/price?city=hyderabad";
            RequestQueue queue= Volley.newRequestQueue(this);
            JsonObjectRequest getRequest= new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    try {

                        if(CURRENT_FUEL_TYPE.equals("Petrol"))
                            CURRENT_RATE=Double.parseDouble(response.getString("petrol"));
                        else
                            CURRENT_RATE=Double.parseDouble(response.getString("diesel"));
                        currentRateTextView.setText((String.valueOf(CURRENT_RATE)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, e.toString());
                    }

                }
            },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, error.toString());
                        }
                    }


            );
            queue.add(getRequest);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        preferencesUpdated=true;
        CURRENT_FUEL_TYPE=sharedPreferences.getString("FuelType", "thug");
        Log.d(TAG, CURRENT_FUEL_TYPE);
        CURRENT_FAVOURITE=sharedPreferences.getString("favourite", "100");
        favouriteFuelTextView.setText(CURRENT_FAVOURITE);

        getCurrentRate();
    }
}
