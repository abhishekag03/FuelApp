package com.example.android.fuelapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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
import com.example.android.fuelapp.data.MapObject;
import com.example.android.fuelapp.data.NetworkChangeReceiver;
import com.example.android.fuelapp.data.NotificationService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

  private static final int REQUEST_LOCATION_PERMISSION = 122;

  public static List<String> arrayForTimelineFuelType = new ArrayList<>();
  public static List<String> arrayForTimelineDate = new ArrayList<>();
  public static List<String> arrayForTimelineLocation = new ArrayList<>();
  public String TAG = "database";
  public static boolean isRunning = false;
  public final static String EXTRA_ORIENTATION = "EXTRA_ORIENTATION";
  public final static String EXTRA_WITH_LINE_PADDING = "EXTRA_WITH_LINE_PADDING";
  public static Location mCurrentLocation;

  private float x1, x2;
  private float y1, y2;
  public static AlertDialog a;
  private SwitchCompat fuelTypeSwitch;

  public static String CURRENT_LOCATION;
  public static double CURRENT_COST;
  public static double CURRENT_PETROL_RATE;
  public static double CURRENT_DIESEL_RATE;
  public static double CURRENT_RATE;
  public static String CURRENT_FUEL_TYPE;
  public static double CURRENT_LITRES;
  public static String CURRENT_FAVOURITE;
  public static String CURRENT_CITY = "";
  private SharedPreferences sharedPreferences = null;

  public static HashMap<String, MapObject> mapContainingCities = new HashMap<String, MapObject>();
  private static boolean preferencesUpdated = false;
  SQLiteDatabase database;

  public static EditText currentFuelEditText;
  public static TextView currentLitresTextView;
  public static TextView currentRateTextView;
  public static TextView favouriteFuelTextView;
  public static TextView frequentFuelTextView;
  public static TextView lastUsedFuelTextView;

  private NetworkChangeReceiver networkReceiver;

  private TextView actionBar;

  private TextView priceHolder;
  private TextView litresHolder;
  private TextView rateHolder;
  private TextView favouriteHolder;
  private TextView frequentHolder;
  private TextView lastUsedHolder;
  private Typeface oratorSTD;

  private FloatingActionButton fillButton;
  public static AlertDialog.Builder dialogBuilder;

  private Boolean isFirstTime = false;

  private void askForPermission(final String permission, final Integer requestCode) {
    if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
        != PackageManager.PERMISSION_GRANTED) {
      // Should we show an explanation?
      if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

        //This is called if user has denied the permission before
        //In this case I am just asking the permission again
        if (a == null) {
          dialogBuilder.setMessage("Location Permission is required for this app to work.");
          dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission },
                  requestCode);
            }
          }).setNegativeButton("Cancel", null);
          a = dialogBuilder.create();
        } else if (!a.isShowing()) a.show();
      } else {

        ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission },
            requestCode);
      }
    } else {
      // Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
      Log.d(TAG, "" + permission + " is already granted. ");
    }
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    networkReceiver = new NetworkChangeReceiver();

    mapContainingCities.put("New Delhi", new MapObject(-1, 28.6139, 77.2090));
    mapContainingCities.put("Kolkata", new MapObject(-1, 22.5726, 88.3639));
    mapContainingCities.put("Mumbai", new MapObject(-1, 19.0760, 72.8777));
    mapContainingCities.put("Chennai", new MapObject(-1, 13.0827, 80.2707));
    mapContainingCities.put("Faridabad", new MapObject(-1, 28.4089, 77.3178));
    mapContainingCities.put("Gurgaon", new MapObject(-1, 28.4595, 77.0266));
    mapContainingCities.put("Noida", new MapObject(-1, 28.5355, 77.3910));
    mapContainingCities.put("Ghaziabad", new MapObject(-1, 28.6692, 77.4538));
    mapContainingCities.put("Agartala", new MapObject(-1, 23.8315, 91.2868));
    mapContainingCities.put("Aizwal", new MapObject(-1, 23.7271, 92.7176));
    mapContainingCities.put("Ambala", new MapObject(-1, 30.3782, 76.7767));
    mapContainingCities.put("Bangalore", new MapObject(-1, 12.9716, 77.5946));
    mapContainingCities.put("Bhopal", new MapObject(-1, 23.2599, 77.4126));
    mapContainingCities.put("Bhubhaneswar", new MapObject(-1, 20.2961, 85.8245));
    mapContainingCities.put("Chandigarh", new MapObject(-1, 30.7333, 76.7794));
    mapContainingCities.put("Dehradun", new MapObject(-1, 30.3165, 78.0322));
    mapContainingCities.put("Gandhinagar", new MapObject(-1, 23.2156, 72.6369));
    mapContainingCities.put("Gangtok", new MapObject(-1, 27.3389, 88.6065));
    mapContainingCities.put("Guwahati", new MapObject(-1, 26.1445, 91.7362));
    mapContainingCities.put("Hyderabad", new MapObject(-1, 17.3850, 78.4867));
    mapContainingCities.put("Imphal", new MapObject(-1, 24.8170, 93.9368));
    mapContainingCities.put("Itanagar", new MapObject(-1, 27.0844, 93.6053));
    mapContainingCities.put("Jaipur", new MapObject(-1, 26.9124, 75.7873));
    mapContainingCities.put("Jammu", new MapObject(-1, 33.7782, 76.5762));
    mapContainingCities.put("Jullunder", new MapObject(-1, 31.3260, 75.5762));
    mapContainingCities.put("Kohima", new MapObject(-1, 25.6586, 94.1053));
    mapContainingCities.put("Lucknow", new MapObject(-1, 26.8467, 80.9462));
    mapContainingCities.put("Panjim", new MapObject(-1, 15.4909, 73.8278));
    mapContainingCities.put("Patna", new MapObject(-1, 25.5941, 85.1376));
    mapContainingCities.put("Pondichery", new MapObject(-1, 11.9139, 79.8145));
    mapContainingCities.put("Port Blair", new MapObject(-1, 11.6234, 92.7265));
    mapContainingCities.put("Raipur", new MapObject(-1, 21.2514, 81.6296));
    mapContainingCities.put("Ranchi", new MapObject(-1, 23.3441, 85.3096));
    mapContainingCities.put("Shillong", new MapObject(-1, 25.5788, 91.8933));
    mapContainingCities.put("Shimla", new MapObject(-1, 31.1048, 77.1734));
    mapContainingCities.put("Srinagar", new MapObject(-1, 34.0837, 74.7973));
    mapContainingCities.put("Trivandrum", new MapObject(-1, 8.5241, 76.9366));
    mapContainingCities.put("Silvasa", new MapObject(-1, 20.2763, 73.0083));
    mapContainingCities.put("Daman", new MapObject(-1, 20.4283, 72.8397));

    getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
    getSupportActionBar().setCustomView(R.layout.action_bar_main);

    NotificationService.notificationIsSent = false;

    priceHolder = findViewById(R.id.price_text_view);
    litresHolder = findViewById(R.id.litres_text_view);
    rateHolder = findViewById(R.id.rate_text_view);
    favouriteHolder = findViewById(R.id.favourite_text_view);
    frequentHolder = findViewById(R.id.frequent_text_view);
    lastUsedHolder = findViewById(R.id.last_used_text_view);
    fuelTypeSwitch = findViewById(R.id.switch_fuel_type);

    oratorSTD = Typeface.createFromAsset(getAssets(), "fonts/OratorStd.otf");
    Typeface segment7 = Typeface.createFromAsset(getAssets(), "fonts/Segment7Standard.otf");

    priceHolder.setTypeface(oratorSTD);
    litresHolder.setTypeface(oratorSTD);
    rateHolder.setTypeface(oratorSTD);
    favouriteHolder.setTypeface(oratorSTD);
    frequentHolder.setTypeface(oratorSTD);
    lastUsedHolder.setTypeface(oratorSTD);
    ((TextView) findViewById(R.id.petrol_switch)).setTypeface(oratorSTD);
    ((TextView) findViewById(R.id.diesel_switch)).setTypeface(oratorSTD);

    currentFuelEditText = findViewById(R.id.current_fuel_cost);
    currentLitresTextView = findViewById(R.id.current_fuel_litres);
    currentRateTextView = findViewById(R.id.current_fuel_rate);
    favouriteFuelTextView = findViewById(R.id.favourite_fuel_cost);
    frequentFuelTextView = findViewById(R.id.most_used_fuel_cost);
    lastUsedFuelTextView = findViewById(R.id.last_fuel_cost);
    old_location = getResources().getString(R.string.save_fuel_save_money);

    currentFuelEditText.setTypeface(segment7);
    currentRateTextView.setTypeface(segment7);
    currentLitresTextView.setTypeface(segment7);
    favouriteFuelTextView.setTypeface(segment7);
    frequentFuelTextView.setTypeface(segment7);
    lastUsedFuelTextView.setTypeface(segment7);

    CURRENT_COST = Double.parseDouble(currentFuelEditText.getText().toString());
    CURRENT_LITRES = Double.parseDouble(currentLitresTextView.getText().toString());
    CURRENT_RATE = Double.parseDouble(currentRateTextView.getText().toString());

    database = (new FuelDbHelper(getApplicationContext())).getWritableDatabase();
    dialogBuilder = new AlertDialog.Builder(this);

    getAllData();

    sharedPreferences = getDefaultSharedPreferences(this);
    sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    if (!sharedPreferences.contains("FuelType")) {
      sharedPreferences.edit().putString("FuelType", "Petrol").apply();
    }

    CURRENT_FUEL_TYPE = sharedPreferences.getString("FuelType", "Petrol");
    CURRENT_FAVOURITE = sharedPreferences.getString("favourite", "500");

    fuelTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
          sharedPreferences.edit().putString("FuelType", "Diesel").apply();
          CURRENT_FUEL_TYPE = "Diesel";
          getCurrentRate();
          Log.d("oncheck", "diesel: " + CURRENT_RATE);
          //diesel

        } else {
          sharedPreferences.edit().putString("FuelType", "Petrol").apply();
          CURRENT_FUEL_TYPE = "Petrol";
          getCurrentRate();
          Log.d("oncheck", "petrol: " + CURRENT_RATE);
        }
      }
    });

    SharedPreferences sharedPreferences1 = getDefaultSharedPreferences(getApplicationContext());
    if (!sharedPreferences1.contains("petrolRate")) {
      sharedPreferences1.edit().putFloat("petrolRate", 70).apply();
    }

    if (!sharedPreferences1.contains("dieselRate")) {
      sharedPreferences1.edit().putFloat("dieselRate", 60).apply();
    }

    CURRENT_PETROL_RATE = sharedPreferences1.getFloat("petrolRate", 70);
    CURRENT_DIESEL_RATE = sharedPreferences1.getFloat("dieselRate", 60);

    favouriteFuelTextView.setText(CURRENT_FAVOURITE);
    Cursor mC = database.rawQuery(
        "SELECT money FROM(SELECT COUNT(money) AS c, money FROM fuel GROUP BY money order by c DESC LIMIT 1)",
        null);

    if (!mC.moveToFirst() || mC.getCount() == 0) {
      frequentFuelTextView.setText(String.valueOf("₹100"));
    } else {
      frequentFuelTextView.setText("₹" + String.valueOf(mC.getInt(0)));
    }

    mC.close();

    startService(new Intent(this, NotificationService.class));

    currentFuelEditText.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (s.length() > 1) {
          CURRENT_COST = Integer.parseInt(stripNonDigits(s.toString().trim()));
          CURRENT_LITRES = CURRENT_COST / CURRENT_RATE;
        }
      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        currentLitresTextView.setText(String.valueOf(String.format("%.2f", CURRENT_LITRES)));
        if (s.length() > 1) {
          CURRENT_COST = Integer.parseInt(stripNonDigits(s.toString().trim()));
          CURRENT_LITRES = CURRENT_COST / CURRENT_RATE;
          Log.d(TAG, CURRENT_COST + " " + CURRENT_LITRES);
        }
      }

      @Override public void afterTextChanged(Editable s) {
        if (s.length() > 1) {
          currentLitresTextView.setText(String.valueOf(String.format("%.2f", CURRENT_LITRES)));
          CURRENT_COST = Integer.parseInt(stripNonDigits(s.toString().trim()));
          CURRENT_LITRES = CURRENT_COST / CURRENT_RATE;
          Log.d(TAG, CURRENT_COST + " " + CURRENT_LITRES);
        }
      }
    });

    askForPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION);

    fillButton = findViewById(R.id.fill_fuel_button);

    if (!GooglePlayServicesAvailable()) {
      finish();
    }

    fillButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        ContentValues cv = new ContentValues();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        Date date = new Date();

        cv.put(FuelContract.FuelEntry.COLUMN_TIME_FILLED, dateFormat.format(date));
        cv.put(FuelContract.FuelEntry.COLUMN_MONEY, CURRENT_COST);
        cv.put(FuelContract.FuelEntry.COLUMN_FUEL_TYPE, CURRENT_FUEL_TYPE);
        cv.put(FuelContract.FuelEntry.COLUMN_LITRES,
            String.valueOf(String.format("%.2f", CURRENT_LITRES, Locale.US)));
        cv.put(FuelContract.FuelEntry.COLUMN_LOCATION, CURRENT_LOCATION);

        arrayForTimelineDate.add(dateFormat.format(date));
        arrayForTimelineFuelType.add(CURRENT_FUEL_TYPE);
        arrayForTimelineLocation.add(CURRENT_LOCATION);

        if (mCurrentLocation != null) {
          cv.put(FuelContract.FuelEntry.COLUMN_LATITUDE, mCurrentLocation.getLatitude());
          cv.put(FuelContract.FuelEntry.COLUMN_LONGITUDE, mCurrentLocation.getLongitude());
        } else {

          if (a == null) {
            dialogBuilder.setMessage(
                "Location Permission is required for this app to work. Please turn on location");
            dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
              }
            }).setNegativeButton("Cancel", null);
            a = dialogBuilder.create();
          } else if (!a.isShowing()) a.show();
        }
        long id = database.insert(FuelContract.FuelEntry.TABLE_NAME, null, cv);
        getAllData();
        if (id <= 0) {
          Toast.makeText(getApplicationContext(), "Sorry! Could not add fuel to your timeline.",
              Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getApplicationContext(),
              String.valueOf(String.format("%.2f", CURRENT_LITRES, Locale.US))
                  + " ltrs of "
                  + CURRENT_FUEL_TYPE
                  + " have been added to your timeline", Toast.LENGTH_LONG).show();
          startActivity(new Intent(MainActivity.this, TimelineActivity.class));
        }
      }
    });

    favouriteFuelTextView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        currentFuelEditText.setText(removeRupeeSymbol(favouriteFuelTextView.getText().toString()));
      }
    });

    lastUsedFuelTextView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        currentFuelEditText.setText(removeRupeeSymbol(lastUsedFuelTextView.getText().toString()));
      }
    });

    frequentFuelTextView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        currentFuelEditText.setText(removeRupeeSymbol(frequentFuelTextView.getText().toString()));
      }
    });
    actionBar = findViewById(R.id.app_bar_main_view);
    actionBar.setTypeface(oratorSTD);
    actionBar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
    actionBar.setSingleLine(true);
    actionBar.setSelected(true);
    actionBar.setMarqueeRepeatLimit(-1);

    isFirstTime = sharedPreferences.getBoolean("firstTime", true);

    if(isFirstTime) {
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
      sharedPreferences.edit().putBoolean("firstTime", false).apply();
      new MaterialTapTargetPrompt.Builder(MainActivity.this).setTarget(fillButton)
          .setPrimaryText("Add Fuel")
          .setSecondaryText("Tap the button to add in fuel log")
          .show();
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

  @Override protected void onStart() {
    super.onStart();
    isRunning = true;

    if (CURRENT_FUEL_TYPE.equals("Petrol")) {
      fuelTypeSwitch.setChecked(false);
    } else {
      fuelTypeSwitch.setChecked(true);
    }

    fuelTypeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (isChecked) {
          getDefaultSharedPreferences(getApplicationContext()).edit()
              .putString("FuelType", "Diesel")
              .apply();
          CURRENT_FUEL_TYPE = "Diesel";
          getCurrentRate();
          Log.d("oncheck", "diesel: " + CURRENT_RATE);
        } else {
          getDefaultSharedPreferences(getApplicationContext()).edit()
              .putString("FuelType", "Petrol")
              .apply();
          CURRENT_FUEL_TYPE = "Petrol";
          getCurrentRate();
          Log.d("oncheck", "petrol: " + CURRENT_RATE);
        }
      }
    });

    NotificationService.notificationIsSent = false;
    //        mGoogleApiClient.connect();
    if (preferencesUpdated) {
      preferencesUpdated = false;
    }

    database = (new FuelDbHelper(getApplicationContext())).getReadableDatabase();
    Cursor cursor = database.rawQuery("select * from " + FuelContract.FuelEntry.TABLE_NAME, null);

    String lastItemCost = "0";
    while (!cursor.isAfterLast()) {
      cursor.moveToLast();
      lastItemCost = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));
      cursor.moveToNext();
    }

    cursor.close();

    lastUsedFuelTextView.setText("₹" + removeAfterDecimalPoint(lastItemCost));
    favouriteFuelTextView.setText("₹" + CURRENT_FAVOURITE);
    currentFuelEditText.setText(removeRupeeSymbol("₹" + CURRENT_FAVOURITE));

    CURRENT_LITRES = Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
    currentLitresTextView.setText((String.valueOf(String.format("%.2f", CURRENT_LITRES))));

    updatedGetCity();
    getCurrentRate();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    getDefaultSharedPreferences(this);
    sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    database.close();
  }

  @Override protected void onStop() {
    super.onStop();
    isRunning = false;
    //mGoogleApiClient.disconnect();
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (permissions.length > 0
        && ActivityCompat.checkSelfPermission(this, permissions[0])
        == PackageManager.PERMISSION_GRANTED) {

      switch (requestCode) {

        case REQUEST_LOCATION_PERMISSION:
          // Toast.makeText(getApplicationContext(), "Permission allowed", Toast.LENGTH_SHORT).show();
          break;
      }
    }
  }

  public boolean isNetworkConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
      return true;
    }
    return false;
  }

  @Override protected void onResume() {
    super.onResume();
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(networkReceiver, filter);
    database = new FuelDbHelper(getApplicationContext()).getReadableDatabase();

    Cursor mC = database.rawQuery(
        "SELECT money FROM(SELECT COUNT(money) AS c, money FROM fuel GROUP BY money order by c DESC LIMIT 1)",
        null);

    if (!mC.moveToFirst() || mC.getCount() == 0) {
      frequentFuelTextView.setText(String.valueOf("₹100"));
    } else {
      frequentFuelTextView.setText("₹" + String.valueOf(mC.getInt(0)));
    }

    mC.close();

    if (currentFuelEditText.requestFocus()) {
      getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
      currentFuelEditText.setSelection(currentFuelEditText.getText().length());
      currentFuelEditText.post(new Runnable() {
        @Override public void run() {
          currentFuelEditText.setSelection(currentFuelEditText.getText().length());
        }
      });
    }
    currentFuelEditText.setSelectAllOnFocus(true);

    LocalBroadcastManager.getInstance(this)
        .registerReceiver(mMessageReceiver, new IntentFilter("location_update_intent_filter"));
  }

  private String old_location;
  private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    @Override public void onReceive(Context context, Intent intent) {

      if (CURRENT_LOCATION != null && !old_location.equals(CURRENT_LOCATION)) {
        // Extract data included in the Intent
        if (intent.getBooleanExtra("location_changed", false)) {
          actionBar.setText("@" + CURRENT_LOCATION);
          old_location = CURRENT_LOCATION;
        } else {
          actionBar.setText(R.string.save_fuel_save_money);
        }
      }
    }
  };

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
      return true;
    } else if (id == R.id.action_timeline) {
      startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
    } else if (id == R.id.action_about) {
      startActivity(new Intent(getApplicationContext(), AboutActivity.class));
    }
    return super.onOptionsItemSelected(item);
  }

  private void getCurrentRate() {

    String Url;
    if ("".equals(CURRENT_CITY)) {
      Url = "http://fuelpriceindia.herokuapp.com/price?city=hyderabad";
    } else {
      Url = "http://fuelpriceindia.herokuapp.com/price?city=" + CURRENT_CITY;
    }
    RequestQueue queue = Volley.newRequestQueue(this);
    JsonObjectRequest getRequest =
        new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
          @Override public void onResponse(JSONObject response) {
            Log.d(TAG, response.toString());

            try {

              if (response.has("city")) {

                if (CURRENT_FUEL_TYPE.equals("Petrol")) {
                  if (response.getString("petrol") == null || response.getString("petrol")
                      .equals("null")) {
                    CURRENT_RATE = CURRENT_PETROL_RATE;
                    Log.d("oncheck", CURRENT_RATE + "");
                  } else {
                    CURRENT_RATE = Double.parseDouble(response.getString("petrol"));
                    getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putFloat("petrolRate", (float) CURRENT_RATE)
                        .apply();
                    getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putFloat("dieselRate",
                            (float) Double.parseDouble(response.getString("diesel")))
                        .apply();
                    Log.d("oncheck", CURRENT_RATE + "");
                  }
                } else {
                  if (response.getString("diesel") == null || response.getString("diesel")
                      .equals("null")) {
                    CURRENT_RATE = CURRENT_DIESEL_RATE;
                    Log.d("oncheck", CURRENT_RATE + "");
                  } else {
                    CURRENT_RATE = Double.parseDouble(response.getString("diesel"));
                    getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putFloat("dieselRate", (float) CURRENT_RATE)
                        .apply();
                    getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putFloat("petrolRate",
                            (float) Double.parseDouble(response.getString("petrol")))
                        .apply();
                    Log.d("oncheck", CURRENT_RATE + "");
                  }
                }
                currentRateTextView.setText(
                    "₹" + (String.valueOf(String.format("%.2f", CURRENT_RATE))));
                CURRENT_LITRES = Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
                currentLitresTextView.setText(
                    (String.valueOf(String.format("%.2f", CURRENT_LITRES))));
              } else {
                if (CURRENT_FUEL_TYPE.equals("Petrol")) {
                  CURRENT_RATE = CURRENT_PETROL_RATE;
                } else {
                  CURRENT_RATE = CURRENT_DIESEL_RATE;
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        },

            new Response.ErrorListener() {
              @Override public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.toString());

                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                  try {
                    String res = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                    JSONObject obj = new JSONObject(res);
                    if (obj.has("msg")) {
                      if (CURRENT_FUEL_TYPE.equals("Petrol")) {
                        CURRENT_RATE = CURRENT_PETROL_RATE;
                        Log.d("network", CURRENT_PETROL_RATE + "");
                      } else {
                        CURRENT_RATE = CURRENT_DIESEL_RATE;
                        Log.d("network", CURRENT_DIESEL_RATE + "");
                      }
                    }

                    currentRateTextView.setText(
                        "₹" + (String.valueOf(String.format("%.2f", CURRENT_RATE))));
                    CURRENT_LITRES = Double.parseDouble(CURRENT_FAVOURITE) / CURRENT_RATE;
                    currentLitresTextView.setText(
                        (String.valueOf(String.format("%.2f", CURRENT_LITRES))));
                    Log.d("network", res);
                  } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                    Log.d("network", "unsupportedencoding");
                  } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("network", "jsonexception");
                  }
                } else {
                  if (a == null) {
                    dialogBuilder.setMessage(
                        "You are not connected to the internet right now. Please try again later.");
                    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                      @Override public void onClick(DialogInterface dialog, int which) {
                      }
                    }).setNegativeButton("Cancel", null);
                    Log.d("network", error.toString());
                    a = dialogBuilder.create();
                  } else if (!a.isShowing()) a.show();
                }
              }
            });
    queue.add(getRequest);
  }

  @Override protected void onPause() {
    super.onPause();
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    unregisterReceiver(networkReceiver);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    preferencesUpdated = true;
    CURRENT_FUEL_TYPE = sharedPreferences.getString("FuelType", "Petrol");
    Log.d(TAG, CURRENT_FUEL_TYPE);
    CURRENT_FAVOURITE = sharedPreferences.getString("favourite", "100");
    favouriteFuelTextView.setText("₹" + CURRENT_FAVOURITE);

    getCurrentRate();
  }

  protected void getAllData() {
    database = new FuelDbHelper(getApplicationContext()).getReadableDatabase();

    Cursor cursor = database.rawQuery("select * from " + FuelContract.FuelEntry.TABLE_NAME, null);
    if (cursor.moveToFirst()) {
      while (!cursor.isAfterLast()) {
        String fuelType =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_FUEL_TYPE));
        String lat =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LATITUDE));
        String lon =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LONGITUDE));
        String money = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));
        String litres =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LITRES));
        String date =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_TIME_FILLED));
        String location =
            cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LOCATION));
        Log.d(TAG,
            fuelType + " ," + lat + " ," + lon + " ," + money + " ," + litres + " ," + date + ", ");

        cursor.moveToNext();
      }
    }
    cursor.close();
  }

  protected void updatedGetCity() {

    if (mCurrentLocation != null) {

      double currentLat = mCurrentLocation.getLatitude();
      double currentLon = mCurrentLocation.getLongitude();

      for (Map.Entry<String, MapObject> entry : mapContainingCities.entrySet()) {
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLon, entry.getValue().latitude,
            entry.getValue().longitude, results);
        float distance = results[0];

        MapObject m =
            new MapObject(distance, entry.getValue().latitude, entry.getValue().longitude);
        entry.setValue(m);

        // Log.d("city", m.distance+" "+entry.getKey());
      }

      double minDistance = mapContainingCities.get("New Delhi").distance;
      String city = "New Delhi";

      for (Map.Entry<String, MapObject> entry : mapContainingCities.entrySet()) {
        if (entry.getValue().distance < minDistance) {
          minDistance = entry.getValue().distance;
          city = entry.getKey();
        }
      }

      Log.d("city", minDistance + "minimum " + city);
      CURRENT_CITY = city;
      getCurrentRate();
    }
  }

  public boolean onTouchEvent(MotionEvent touchEvent) {
    switch (touchEvent.getAction()) {
      case MotionEvent.ACTION_DOWN: {
        x1 = touchEvent.getX();
        y1 = touchEvent.getY();
        break;
      }

      case MotionEvent.ACTION_UP: {
        x2 = touchEvent.getX();
        y2 = touchEvent.getY();

        if (x1 < x2) {
          //Toast.makeText(getApplicationContext(), "Left to right swipe performed", Toast.LENGTH_SHORT).show();
        }

        if (x1 > x2 + 100) {
          //Toast.makeText(getApplicationContext(), "Right to left swipe performed", Toast.LENGTH_SHORT).show();
          startActivity(new Intent(getApplicationContext(), TimelineActivity.class));
        }
        break;
      }
    }

    return false;
  }

  @Override public boolean onDown(MotionEvent e) {
    onTouchEvent(e);
    return true;
  }

  @Override public void onShowPress(MotionEvent e) {
    onTouchEvent(e);
  }

  @Override public boolean onSingleTapUp(MotionEvent e) {
    onTouchEvent(e);
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    onTouchEvent(e1);
    return true;
  }

  @Override public void onLongPress(MotionEvent e) {
    onTouchEvent(e);
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

    onTouchEvent(e1);
    return false;
  }

  public void updateFuelType(View v) {
    fuelTypeSwitch.setChecked(v.getId() == R.id.diesel_switch);
  }

  String removeRupeeSymbol(String string) {
    String s = "";

    for (int i = 0; i < string.length(); i++) {
      if (!(string.charAt(i) == '₹')) {
        s += string.charAt(i);
      }
    }

    return s;
  }

  private String removeAfterDecimalPoint(String x) {
    String out = "";
    int i = 0;
    while (i < x.length()) {
      if (x.charAt(i) != '.') {
        out += x.charAt(i);
      } else {
        break;
      }
      i++;
    }
    return out;
  }

  public static String stripNonDigits(final CharSequence input) {
    final StringBuilder sb = new StringBuilder(input.length());
    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (c > 47 && c < 58) {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
