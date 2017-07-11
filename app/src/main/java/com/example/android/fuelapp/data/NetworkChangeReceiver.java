package com.example.android.fuelapp.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
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
import com.example.android.fuelapp.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by vishaal on 11/7/17.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (checkInternet(context)) {

            if (MainActivity.a!=null && MainActivity.a.isShowing())
                MainActivity.a.dismiss();
            if (MainActivity.mCurrentLocation != null) {

                double currentLat = MainActivity.mCurrentLocation.getLatitude();
                double currentLon = MainActivity.mCurrentLocation.getLongitude();

                for (Map.Entry<String, MapObject> entry : MainActivity.mapContainingCities.entrySet()) {
                    float[] results = new float[1];
                    Location.distanceBetween(currentLat, currentLon, entry.getValue().latitude, entry.getValue().longitude, results);
                    float distance = results[0];

                    MapObject m = new MapObject(distance, entry.getValue().latitude, entry.getValue().longitude);
                    entry.setValue(m);

                    // Log.d("city", m.distance+" "+entry.getKey());
                }

                double minDistance = MainActivity.mapContainingCities.get("New Delhi").distance;
                String city = "New Delhi";

                for (Map.Entry<String, MapObject> entry : MainActivity.mapContainingCities.entrySet()) {
                    if (entry.getValue().distance < minDistance) {
                        minDistance = entry.getValue().distance;
                        city = entry.getKey();
                    }
                }

                Log.d("city", minDistance + "minimum " + city);
                MainActivity.CURRENT_CITY = city;
                //getCurrentRate();


                String Url;
                if ("".equals(MainActivity.CURRENT_CITY))
                    Url = "http://fuelpriceindia.herokuapp.com/price?city=hyderabad";
                else
                    Url = "http://fuelpriceindia.herokuapp.com/price?city=" + MainActivity.CURRENT_CITY;
                RequestQueue queue = Volley.newRequestQueue(context);
                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("database", response.toString());

                        try {

                            if (response.has("city")) {

                                if (MainActivity.CURRENT_FUEL_TYPE.equals("Petrol")) {
                                    if (response.getString("petrol") == null || response.getString("petrol").equals("null")) {
                                        MainActivity.CURRENT_RATE = MainActivity.CURRENT_PETROL_RATE;
                                        Log.d("oncheck", MainActivity.CURRENT_RATE + "");
                                    } else {
                                        MainActivity.CURRENT_RATE = Double.parseDouble(response.getString("petrol"));
                                        android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("petrolRate", (float) MainActivity.CURRENT_RATE).apply();
                                        android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("dieselRate", (float) Double.parseDouble(response.getString("diesel"))).apply();
                                        Log.d("oncheck", MainActivity.CURRENT_RATE + "");
                                    }
                                } else {
                                    if (response.getString("diesel") == null || response.getString("diesel").equals("null")) {
                                        MainActivity.CURRENT_RATE = MainActivity.CURRENT_DIESEL_RATE;
                                        Log.d("oncheck", MainActivity.CURRENT_RATE + "");
                                    } else {
                                        MainActivity.CURRENT_RATE = Double.parseDouble(response.getString("diesel"));
                                        android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("dieselRate", (float) MainActivity.CURRENT_RATE).apply();
                                        android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat("petrolRate", (float) Double.parseDouble(response.getString("petrol"))).apply();
                                        Log.d("oncheck", MainActivity.CURRENT_RATE + "");
                                    }
                                }
                                MainActivity.currentRateTextView.setText("₹" + (String.valueOf(String.format("%.2f", MainActivity.CURRENT_RATE))));
                                MainActivity.CURRENT_LITRES = Double.parseDouble(MainActivity.CURRENT_FAVOURITE) / MainActivity.CURRENT_RATE;
                                MainActivity.currentLitresTextView.setText((String.valueOf(String.format("%.2f", MainActivity.CURRENT_LITRES))));
                            } else {
                                if (MainActivity.CURRENT_FUEL_TYPE.equals("Petrol")) {
                                    MainActivity.CURRENT_RATE = MainActivity.CURRENT_PETROL_RATE;
                                } else {
                                    MainActivity.CURRENT_RATE = MainActivity.CURRENT_DIESEL_RATE;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("database", error.toString());

                                NetworkResponse response = error.networkResponse;
                                if (error instanceof ServerError && response != null) {
                                    try {
                                        String res = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                        JSONObject obj = new JSONObject(res);
                                        if (obj.has("msg")) {
                                            if (MainActivity.CURRENT_FUEL_TYPE.equals("Petrol")) {
                                                MainActivity.CURRENT_RATE = MainActivity.CURRENT_PETROL_RATE;
                                                Log.d("network", MainActivity.CURRENT_PETROL_RATE + "");
                                            } else {
                                                MainActivity.CURRENT_RATE = MainActivity.CURRENT_DIESEL_RATE;
                                                Log.d("network", MainActivity.CURRENT_DIESEL_RATE + "");
                                            }
                                        }

                                        MainActivity.currentRateTextView.setText("₹" + (String.valueOf(String.format("%.2f", MainActivity.CURRENT_RATE))));
                                        MainActivity.CURRENT_LITRES = Double.parseDouble(MainActivity.CURRENT_FAVOURITE) / MainActivity.CURRENT_RATE;
                                        MainActivity.currentLitresTextView.setText((String.valueOf(String.format("%.2f", MainActivity.CURRENT_LITRES))));
                                        Log.d("network", res);
                                    } catch (UnsupportedEncodingException e1) {
                                        e1.printStackTrace();
                                        Log.d("network", "unsupportedencoding");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Log.d("network", "jsonexception");
                                    }
                                } else {
                                    if (MainActivity.a == null) {
                                        MainActivity.dialogBuilder.setMessage("You are not connected to the internet right now. Please try again later.");
                                        MainActivity.dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setNegativeButton("Cancel", null);
                                        Log.d("network", error.toString());
                                        MainActivity.a = MainActivity.dialogBuilder.create();
                                    } else if (!MainActivity.a.isShowing())
                                        MainActivity.a.show();
                                }
                            }
                        }
                );
                queue.add(getRequest);


            }


        } else {
            Toast.makeText(context, "Network not available", Toast.LENGTH_SHORT).show();
        }

    }


    boolean checkInternet(Context context) {
        ServiceManager serviceManager = new ServiceManager(context);
        if (serviceManager.isNetworkAvailable()) {
            return true;
        } else {
            return false;
        }
    }

}

class ServiceManager {

    Context context;

    public ServiceManager(Context base) {
        context = base;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}