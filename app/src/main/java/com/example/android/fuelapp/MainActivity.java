package com.example.android.fuelapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
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
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.fuelapp.data.FuelContract;
import com.example.android.fuelapp.data.FuelDbHelper;
import com.example.android.fuelapp.data.NotificationService;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int REQUEST_LOCATION_PERMISSION = 122;
    ;


    public static List<String> arrayForTimelineFuelType=new ArrayList<>();
    public static List<String> arrayForTimelineDate=new ArrayList<>();
    public static List<String> arrayForTimelineLocation=new ArrayList<>();

    public String TAG = "database";


    public static boolean isRunning=false;
    private static boolean CURRENT_LOCATION_IS_FUEL_STATION = false;

    public final static String EXTRA_ORIENTATION = "EXTRA_ORIENTATION";
    public final static String EXTRA_WITH_LINE_PADDING = "EXTRA_WITH_LINE_PADDING";


    private static final long INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 5000;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;


    private float x1, x2;
    private float y1,y2;

    private AlertDialog a;


    public static String CURRENT_LOCATION;
    private double CURRENT_COST;
    private long CURRENT_LATITUDE;
    private long CURRENT_LONGITUDE;
    private double CURRENT_PETROL_RATE;
    private double CURRENT_DIESEL_RATE;
    private double CURRENT_RATE;
    private String CURRENT_FUEL_TYPE;
    private double CURRENT_LITRES;
    private String CURRENT_FAVOURITE;
    private String CURRENT_CITY="";

    private static boolean preferencesUpdated=false;
    SQLiteDatabase database;


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
                a = alertDialog.create();
                if(!a.isShowing())
                    a.show();
                Window window = a.getWindow();
                window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
           // Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, ""+permission+" is already granted. ");
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

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_main);

        NotificationService.notificationIsSent=false;

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


        CURRENT_COST=Double.parseDouble(currentFuelTextview.getText().toString());
        CURRENT_LITRES=Double.parseDouble(currentLitresTextView.getText().toString());
        CURRENT_RATE=Double.parseDouble(currentRateTextView.getText().toString());

        //getCurrentRate();
        getAllData();

        database= (new FuelDbHelper(getApplicationContext())).getWritableDatabase();

        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        CURRENT_FUEL_TYPE=sharedPreferences.getString("FuelType", "Petrol");
        CURRENT_FAVOURITE=sharedPreferences.getString("favourite", "100");


        SharedPreferences sharedPreferences1= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(!sharedPreferences1.contains("petrolRate"))
        {
            sharedPreferences1.edit().putFloat("petrolRate", 70).apply();
        }

        if(!sharedPreferences1.contains("dieselRate"))
        {
            sharedPreferences1.edit().putFloat("dieselRate", 60).apply();
        }



        CURRENT_PETROL_RATE=sharedPreferences1.getFloat("petrolRate", 70);
        CURRENT_DIESEL_RATE=sharedPreferences1.getFloat("dieselRate", 60);



        favouriteFuelTextView.setText(CURRENT_FAVOURITE);
        Cursor mC = database.rawQuery("SELECT money FROM(SELECT COUNT(money) AS c, money FROM fuel GROUP BY money order by c DESC LIMIT 1)", null);

        if(!mC.moveToFirst() || mC.getCount()==0)
            frequentFuelTextView.setText(String.valueOf("₹100"));
        else
            frequentFuelTextView.setText("₹"+String.valueOf( mC.getInt(0)));



        startService(new Intent(this, NotificationService.class));


//        currentFuelTextview.setText(removeRupeeSymbol(CURRENT_FAVOURITE);
//        CURRENT_LITRES=Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
//        currentLitresTextView.setText(( String.valueOf(String.format("%.2f", CURRENT_LITRES))));
        currentFuelTextview.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(s.length()>1){
                    CURRENT_COST=Integer.parseInt(stripNonDigits(s.toString().trim()));
                    CURRENT_LITRES= CURRENT_COST / CURRENT_RATE;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentLitresTextView.setText(String.valueOf(String.format("%.2f", CURRENT_LITRES)));
                if (s.length() > 1) {
                    CURRENT_COST = Integer.parseInt(stripNonDigits(s.toString().trim()));
                    CURRENT_LITRES = CURRENT_COST / CURRENT_RATE;
                    Log.d(TAG, CURRENT_COST + " " + CURRENT_LITRES);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
             if(s.length()>1){
                currentLitresTextView.setText(String.valueOf(String.format("%.2f",CURRENT_LITRES)));
                CURRENT_COST=Integer.parseInt(stripNonDigits(s.toString().trim()));
                CURRENT_LITRES= CURRENT_COST / CURRENT_RATE;
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

                ContentValues cv=new ContentValues();

                DateFormat dateFormat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date=new Date();

                cv.put(FuelContract.FuelEntry.COLUMN_TIME_FILLED, dateFormat.format(date).toString());
                cv.put(FuelContract.FuelEntry.COLUMN_MONEY, CURRENT_COST);
                cv.put(FuelContract.FuelEntry.COLUMN_FUEL_TYPE, CURRENT_FUEL_TYPE);
                cv.put(FuelContract.FuelEntry.COLUMN_LITRES, String.valueOf(String.format("%.2f",CURRENT_LITRES)));



                cv.put(FuelContract.FuelEntry.COLUMN_LOCATION, CURRENT_LOCATION);


                arrayForTimelineDate.add(dateFormat.format(date).toString());
                arrayForTimelineFuelType.add(CURRENT_FUEL_TYPE);
                arrayForTimelineLocation.add(CURRENT_LOCATION);
                Log.d(TAG, "ContentValues inserted");

                if(mCurrentLocation!=null) {
                    cv.put(FuelContract.FuelEntry.COLUMN_LATITUDE, mCurrentLocation.getLatitude());
                    cv.put(FuelContract.FuelEntry.COLUMN_LONGITUDE, mCurrentLocation.getLongitude());
                }
                else
                {


                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
                    alertDialog.setMessage("Location Permission is required for this app to work. Please turn on location");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                      //Toast.makeText(getApplicationContext(), "You need to switch on Location.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", null);
                    a = alertDialog.create();
                    if(!a.isShowing())
                        a.show();
                    Window window = a.getWindow();
                    window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

                    //Toast.makeText(getApplicationContext(), "mCurrentLocation is null", Toast.LENGTH_SHORT).show();
                }
                long id= database.insert(FuelContract.FuelEntry.TABLE_NAME, null, cv);
                getAllData();
                if(id<=0){
                    Toast.makeText(getApplicationContext(), "Could not add data.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Succesfully added data" , Toast.LENGTH_SHORT).show();
                }

                
            }
        });



        favouriteFuelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFuelTextview.setText(removeRupeeSymbol(favouriteFuelTextView.getText().toString()));
            }
        });

        lastUsedFuelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFuelTextview.setText(removeRupeeSymbol(lastUsedFuelTextView.getText().toString()));
            }
        });

        frequentFuelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFuelTextview.setText(removeRupeeSymbol(frequentFuelTextView.getText().toString()));
            }
        });


    }



    private void sendNotification()
    {
        android.support.v4.app.NotificationCompat.Builder mBuilder= new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Track your fuel expenses")
                .setContentText("Filling up Fuel? Add it to your timeline.")
                .setAutoCancel(true);

        Intent resultIntent= new Intent(this, MainActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.build().flags|=Notification.FLAG_AUTO_CANCEL;
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId= 001;

        
        
        NotificationManager mNotifymgr= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifymgr.notify(mNotificationId, mBuilder.build());

    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            currentFuelTextview.setText(removeRupeeSymbol(CURRENT_LOCATION+" "+ mLastUpdateTime));
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
            a = alertDialog.create();
            if(!a.isShowing())
                a.show();
            Window window = a.getWindow();
            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

           // Toast.makeText(getApplicationContext(), "mCurrentLocation is null", Toast.LENGTH_SHORT).show();
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
        isRunning=true;
        NotificationService.notificationIsSent=false;
        mGoogleApiClient.connect();
        if(preferencesUpdated)
        {
            preferencesUpdated=false;
        }


        database=(new FuelDbHelper(getApplicationContext())).getReadableDatabase();

        Cursor cursor= database.rawQuery("select * from "+ FuelContract.FuelEntry.TABLE_NAME, null );

        String lastItemCost="0";
        while(!cursor.isAfterLast()) {
            cursor.moveToLast();
            lastItemCost = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));
            cursor.moveToNext();
        }
        lastUsedFuelTextView.setText("₹"+removeAfterDecimalPoint(lastItemCost));


        favouriteFuelTextView.setText("₹"+CURRENT_FAVOURITE);

        currentFuelTextview.setText(removeRupeeSymbol("₹"+CURRENT_FAVOURITE));

        CURRENT_LITRES=Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
        currentLitresTextView.setText(( String.valueOf(String.format("%.2f", CURRENT_LITRES))));


        getCity();

        getCurrentRate();

        

    }
    
    
    String removeRupeeSymbol(String string)
    {
        String s="";
        
        for(int i=0;i<string.length();i++){
            if(!(string.charAt(i)=='₹')){
                s+=string.charAt(i);
            }
        }
        
        return s;
    }


    private String removeAfterDecimalPoint(String x)
    {
        String out="";
        int i=0;
        while(i<x.length())
        {
            if(x.charAt(i)!='.')
                out+=x.charAt(i);
            else
                break;
            i++;
        }
        return  out;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences sharedPreferences= android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        database.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning=false;
        //mGoogleApiClient.disconnect();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);



        if (permissions.length>0 && ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            switch (requestCode) {

                case REQUEST_LOCATION_PERMISSION:
                   // Toast.makeText(getApplicationContext(), "Permission allowed", Toast.LENGTH_SHORT).show();
                    break;

            }

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if(CURRENT_LOCATION_IS_FUEL_STATION){
            fillButton.setEnabled(true);
            if((TextView) findViewById(R.id.app_bar_main_view)!=null){
                TextView actionBar= (TextView) findViewById(R.id.app_bar_main_view);
                actionBar.setText(CURRENT_LOCATION);
                actionBar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                actionBar.setFocusable(true);
//                actionBar.setFocusableInTouchMode(true);
//                actionBar.requestFocus();
                actionBar.setSingleLine(true);
                actionBar.setSelected(true);
                actionBar.setMarqueeRepeatLimit(-1);
            }
            //fillButton.setBackgroundTintColor(getResources().getColor(R.color.themeYellow));
            fillButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.themeYellow)));
        }
        else{
            fillButton.setEnabled(false);
            if((TextView) findViewById(R.id.app_bar_main_view)!=null){


                TextView actionBar= (TextView) findViewById(R.id.app_bar_main_view);
                actionBar.setText("FuelApp");
                actionBar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                //actionBar.setFocusable(true);
                //actionBar.setFocusableInTouchMode(true);
                //actionBar.requestFocus();
                actionBar.setSingleLine(true);
                actionBar.setSelected(true);
                actionBar.setMarqueeRepeatLimit(-1);

            }
            //fillButton.setBackgroundColor(getResources().getColor(R.color.mainBlock));
            fillButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.mainBlock)));
        }

        mCurrentLocation = location;
        getCity();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
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
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
        database=new FuelDbHelper(getApplicationContext()).getReadableDatabase();



        Cursor mC = database.rawQuery("SELECT money FROM(SELECT COUNT(money) AS c, money FROM fuel GROUP BY money order by c DESC LIMIT 1)", null);

        if(!mC.moveToFirst() || mC.getCount()==0)
            frequentFuelTextView.setText(String.valueOf("₹100"));
        else
            frequentFuelTextView.setText("₹"+String.valueOf( mC.getInt(0)));


    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (NotificationService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        Log.d("network", "onConnectionFailed");
        alertDialog.setNegativeButton("Cancel", null);
        a = alertDialog.create();
        if(!a.isShowing())
            a.show();
        Window window = a.getWindow();
        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


       // Toast.makeText(getApplicationContext(), "You are not connected to the internet right now. Please try again.", Toast.LENGTH_SHORT).show();
    }


    protected void getPlace() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);
            return;
        }


        if(mCurrentLocation==null) {


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Location Permission is required for this app to work. Please turn on location");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            alertDialog.setNegativeButton("Cancel", null);
            a = alertDialog.create();
            if(!a.isShowing())
                a.show();
            Window window = a.getWindow();
            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

           // Toast.makeText(getApplicationContext(), "mCurrentLocation is null", Toast.LENGTH_SHORT).show();
        }

        Awareness.SnapshotApi.getPlaces(mGoogleApiClient).setResultCallback(new ResultCallback<PlacesResult>() {
            @Override
            public void onResult(@NonNull PlacesResult placesResult) {
                if (!placesResult.getStatus().isSuccess()) {
                    Log.e(TAG, "Could not get places");
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
                    alertDialog.setMessage("You are not connected to the internet right now. Please try again later.");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.setNegativeButton("Cancel", null);
                    Log.d("network", "getPlace");
                    a = alertDialog.create();
                    if(!a.isShowing())
                        a.show();
                    Window window = a.getWindow();
                    window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                    return;
                }
                List<PlaceLikelihood> placeLikelihoodList = placesResult.getPlaceLikelihoods();
                if (placeLikelihoodList != null) {

                    CURRENT_LOCATION=placeLikelihoodList.get(0).getPlace().getName().toString();



                    if(placeLikelihoodList.get(0).getPlace().getPlaceTypes().contains(41)){
//                       // Toast.makeText(getApplicationContext(), "Fuel station found", Toast.LENGTH_SHORT).show();
//                       // sendNotification();
                        CURRENT_LOCATION_IS_FUEL_STATION=true;
                    }
                    else
                    {
                        CURRENT_LOCATION_IS_FUEL_STATION=false;
                    }

                    for (int i = 0; i < placeLikelihoodList.size(); i++) {
                        PlaceLikelihood p = placeLikelihoodList.get(i);
                        Log.d(TAG, p.getPlace().getName().toString() +",place type: "+p.getPlace().getPlaceTypes().contains(41));


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

        else if(id==R.id.action_timeline)
        {
            startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCurrentRate()
    {

        String Url;
        if(CURRENT_CITY=="")
            Url="http://fuelpriceindia.herokuapp.com/price?city=hyderabad";
        else
            Url="http://fuelpriceindia.herokuapp.com/price?city="+CURRENT_CITY;
            RequestQueue queue= Volley.newRequestQueue(this);
            JsonObjectRequest getRequest= new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());

                    try {



                        if(response.has("city")) {

                            if (CURRENT_FUEL_TYPE.equals("Petrol"))
                                if (response.getString("petrol") == null || response.getString("petrol").equals("null"))
                                    CURRENT_RATE = CURRENT_PETROL_RATE;
                                else {
                                    CURRENT_RATE = Double.parseDouble(response.getString("petrol"));
                                    android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putFloat("petrolRate", (float) CURRENT_RATE).apply();
                                    android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putFloat("dieselRate", (float) Double.parseDouble(response.getString("diesel"))).apply();
                                }
                            else if (response.getString("diesel") == null || response.getString("diesel").equals("null"))
                                CURRENT_RATE = CURRENT_DIESEL_RATE;
                            else {
                                CURRENT_RATE = Double.parseDouble(response.getString("diesel"));
                                android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putFloat("dieselRate", (float) CURRENT_RATE).apply();
                                android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putFloat("petrolRate", (float) Double.parseDouble(response.getString("petrol"))).apply();
                            }
                            currentRateTextView.setText("₹" + (String.valueOf(CURRENT_RATE)));
                            CURRENT_LITRES = Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
                            currentLitresTextView.setText((String.valueOf(String.format("%.2f", CURRENT_LITRES))));
                        }
                        else
                        {
                            if(CURRENT_FUEL_TYPE.equals("Petrol"))
                            {
                                CURRENT_RATE=CURRENT_PETROL_RATE;
                            }
                            else
                            {
                                CURRENT_RATE=CURRENT_DIESEL_RATE;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();

//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
//                        alertDialog.setMessage("You are not connected to the internet right now. Please try again later.");
//                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        });
//                        alertDialog.setNegativeButton("Cancel", null);
//                        AlertDialog a = alertDialog.create();
//                        a.show();
//                        Window window = a.getWindow();
//                        window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);





                        Log.d(TAG, e.toString());
                    }

                }
            },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d(TAG, error.toString());


                            NetworkResponse response= error.networkResponse;
                            if(error instanceof ServerError && response!=null){
                                try{
                                    String res=new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                    JSONObject obj= new JSONObject(res);
                                    if(obj.has("msg"))
                                    {
                                        if(CURRENT_FUEL_TYPE.equals("Petrol"))
                                        {
                                            CURRENT_RATE=CURRENT_PETROL_RATE;
                                        }
                                        else
                                        {
                                            CURRENT_RATE=CURRENT_DIESEL_RATE;
                                        }
                                    }
                                    Log.d("network", res);
                                }
                                catch (UnsupportedEncodingException e1){
                                    e1.printStackTrace();
                                    Log.d("network", "unsupportedencoding");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.d("network", "jsonexception");
                                }
                            }

                            else{
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AppTheme);
                            alertDialog.setMessage("You are not connected to the internet right now. Please try again later.");
                            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.setNegativeButton("Cancel", null);
                            a = alertDialog.create();
                            Log.d("network", "volley error");
                            Log.d("network", error.toString());
                            if(!a.isShowing())
                                a.show();
                            Window window = a.getWindow();
                            window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

                            }
                        }
                    }


            );
            queue.add(getRequest);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


        preferencesUpdated=true;
        CURRENT_FUEL_TYPE=sharedPreferences.getString("FuelType", "Petrol");
        Log.d(TAG, CURRENT_FUEL_TYPE);
        CURRENT_FAVOURITE=sharedPreferences.getString("favourite", "100");
        favouriteFuelTextView.setText("₹"+CURRENT_FAVOURITE);

        getCurrentRate();
    }


    protected void getAllData()
    {
        database=new FuelDbHelper(getApplicationContext()).getReadableDatabase();

        Cursor cursor= database.rawQuery("select * from "+ FuelContract.FuelEntry.TABLE_NAME, null );
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String fuelType = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_FUEL_TYPE));
                String lat = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LATITUDE));
                String lon = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LONGITUDE));
                String money = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));
                String litres = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LITRES));
                String date= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_TIME_FILLED));
                String location= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LOCATION));
                Log.d(TAG, fuelType+" ,"+lat+" ,"+lon+" ,"+money+" ,"+litres+" ,"+date);

                cursor.moveToNext();
            }
        }


        
    }



    protected void getCity() {
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> list = null;
        try {
            list = geoCoder.getFromLocation(mCurrentLocation
                    .getLatitude(), mCurrentLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("yolo", e.toString());
        }
        catch (NullPointerException e){
            e.printStackTrace();
            Log.d("yolo", e.toString());
        }
        if (list != null) {

            if(list.size()>0) {


                Address address = list.get(0);
                String result = address.getLocality();
                Log.d("yolo", "current city: " + result);
                if (CURRENT_CITY == "") {
                    if(result.equals("Calcutta"))
                            CURRENT_CITY="kolkata";
                    else if(result.equals("Gurugram"))
                            CURRENT_CITY="gurgaon";
                    else if(result.equals("Thiruvananthapuram"))
                            CURRENT_CITY="trivandrum";
                    else if(result.equals("Puducherry"))
                            CURRENT_CITY="pondichery";
                    else if(result.equals("Jalandhar"))
                            CURRENT_CITY="jullunder";
                    else if(result.equals("Aizawl"))
                            CURRENT_CITY="aizwal";
                    else if(result.equals("Bhubaneswar"))
                            CURRENT_CITY="bhubhaneswar";
                    else
                            CURRENT_CITY = result;

                    Log.d("yolo", "current city: " + CURRENT_CITY);
                }
            }

        }

        getCurrentRate();
    }




    public boolean onTouchEvent(MotionEvent touchEvent)
    {
        switch (touchEvent.getAction())
        {
            case  MotionEvent.ACTION_DOWN:
            {
                x1=touchEvent.getX();
                y1=touchEvent.getY();
                break;
            }

            case MotionEvent.ACTION_UP:
            {
                x2=touchEvent.getX();
                y2=touchEvent.getY();

                if(x1<x2)
                {
                    //Toast.makeText(getApplicationContext(), "Left to right swipe performed", Toast.LENGTH_SHORT).show();
                }

                if(x1>x2)
                {
                    //Toast.makeText(getApplicationContext(), "Right to left swipe performed", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
                }
                break;
            }
        }

        return false;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        onTouchEvent(e);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        onTouchEvent(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        onTouchEvent(e);
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        onTouchEvent(e1);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        onTouchEvent(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        onTouchEvent(e1);
        return false;
    }
}
