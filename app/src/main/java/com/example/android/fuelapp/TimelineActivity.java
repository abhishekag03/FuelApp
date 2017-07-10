package com.example.android.fuelapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.fuelapp.data.FuelAdapter;
import com.example.android.fuelapp.data.FuelContract;
import com.example.android.fuelapp.data.FuelDbHelper;
import com.example.android.fuelapp.model.Orientation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimelineActivity extends AppCompatActivity implements FuelAdapter.ListItemClickListener {


    private RecyclerView mRecyclerView;
    private FuelAdapter mFuelAdapter;
    private Orientation mOrientation;
    private boolean mWithLinePadding;

    private boolean shouldDisplayMenuDelete = true;

    private float x1, x2;
    private float y1, y2;


    private double totalCost = 0;

    public static boolean isRunning = false;


    public static List<String> arrayForTimelineFuelType = new ArrayList<>();
    public static List<String> arrayForTimelineDate = new ArrayList<>();
    public static List<String> arrayForTimelineLocation = new ArrayList<>();
    public static List<String> arrayForTimelineCost = new ArrayList<>();
    public static List<String> arrayForTimelineLitres = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getDataForTimeline();

        if (arrayForTimelineDate.size() > 0) {


            setContentView(R.layout.activity_timeline);

            shouldDisplayMenuDelete = true;

            getSupportActionBar().setTitle("Fuel Log");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
            Collections.reverse(arrayForTimelineDate);
            Collections.reverse(arrayForTimelineFuelType);
            Collections.reverse(arrayForTimelineLocation);
            Collections.reverse(arrayForTimelineCost);
            Collections.reverse(arrayForTimelineLitres);

            mOrientation = (Orientation) getIntent().getSerializableExtra(MainActivity.EXTRA_ORIENTATION);
            mWithLinePadding = getIntent().getBooleanExtra(MainActivity.EXTRA_WITH_LINE_PADDING, false);

            mRecyclerView = (RecyclerView) findViewById(R.id.fuel_stations_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setHasFixedSize(true);

            initView();

            if ((TextView) findViewById(R.id.total_money_spent) != null) {
                Log.d("database", "total money: " + totalCost + ", txt set");
                Typeface oratorSTD = Typeface.createFromAsset(getAssets(), "fonts/OratorStd.otf");
                ((TextView) findViewById(R.id.total_money_spent)).setTypeface(oratorSTD);
                ((TextView) findViewById(R.id.total_money_spent)).setText("Total Money Spent: ₹" + totalCost);
            }

            ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                    final int position = viewHolder.getAdapterPosition(); //get position which is swipe

                    AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this); //alert for confirm to delete
                    builder.setMessage("Are you sure to delete?");    //set message

                    builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() { //when click on DELETE
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position_to_remove = arrayForTimelineDate.size() - position - 1;
                            arrayForTimelineCost.remove(position_to_remove);  //then remove item
                            arrayForTimelineDate.remove(position_to_remove);
                            arrayForTimelineFuelType.remove(position_to_remove);
                            arrayForTimelineLitres.remove(position_to_remove);
                            arrayForTimelineLocation.remove(position_to_remove);

                            Log.d("REMOVING", arrayForTimelineCost.get(position_to_remove) + " and " + arrayForTimelineDate.get(position_to_remove)
                            + " and " + arrayForTimelineFuelType.get(position_to_remove) + " and " + arrayForTimelineLitres.get(position_to_remove));

                            Log.d("Arrays:", "" + arrayForTimelineFuelType.toString() + arrayForTimelineLocation.toString());

                            SQLiteDatabase database = new FuelDbHelper(getApplicationContext()).getWritableDatabase();

                            Cursor cursor= database.rawQuery("SELECT _id, location FROM " + FuelContract.FuelEntry.TABLE_NAME, null);
                            cursor.moveToPosition(position_to_remove);
                            int deleted_rows = database.delete(FuelContract.FuelEntry.TABLE_NAME, "_id = " + cursor.getInt(cursor.getColumnIndex("_id")), null );
                            if( deleted_rows > 0)
                                Toast.makeText(getApplicationContext(), cursor.getString(cursor.getColumnIndex("location")) + " has been removed.", Toast.LENGTH_LONG).show();
                            cursor.close();
                            database.close();

                            mFuelAdapter.notifyItemRemoved(position_to_remove);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
//                            mFuelAdapter.notifyItemRangeChanged(position_to_remove, mFuelAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                            mFuelAdapter.notifyDataSetChanged();

                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {  //not removing items if cancel is done
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mFuelAdapter.notifyItemRemoved(position + 1);    //notifies the RecyclerView Adapter that data in adapter has been removed at a particular position.
                            mFuelAdapter.notifyItemRangeChanged(position, mFuelAdapter.getItemCount());   //notifies the RecyclerView Adapter that positions of element in adapter has been changed from position(removed element index to end of list), please update it.
                        }
                    }).show();  //show alert dialog

                }
            };

            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
            itemTouchHelper.attachToRecyclerView(mRecyclerView);

        } else {
            setContentView(R.layout.empty_timeline);

            shouldDisplayMenuDelete = false;

            getSupportActionBar().setTitle("Timeline");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    private void initView() {
        mFuelAdapter = new FuelAdapter(arrayForTimelineDate.size(), this, mOrientation, mWithLinePadding);
        mRecyclerView.setAdapter(mFuelAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.delete_timeline) {


            AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
            builder
                    .setMessage("Are you sure you want to delete all logs?")
                    .setCancelable(false);
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {


                    final ProgressDialog progressDialog = new ProgressDialog(TimelineActivity.this, R.style.AppTheme);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage("Deleting Logs...");
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Please Wait");
                    progressDialog.show();
                    Window window = progressDialog.getWindow();

                    window.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);


                    new android.os.Handler().postDelayed(

                            new Runnable() {
                                @Override
                                public void run() {
                                    SQLiteDatabase database = (new FuelDbHelper(getApplicationContext())).getWritableDatabase();

                                    int noOfDeletedRowsProfile = database.delete(FuelContract.FuelEntry.TABLE_NAME, null, null);


                                    database.close();
                                    if (noOfDeletedRowsProfile > 0) {

                                        arrayForTimelineDate.clear();
                                        arrayForTimelineFuelType.clear();
                                        arrayForTimelineLocation.clear();
                                        arrayForTimelineCost.clear();
                                        arrayForTimelineLitres.clear();
                                        finish();
                                        Toast.makeText(getApplicationContext(), "Deleted Logs", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.d("database", "Couldnt delete account");
                                    }

                                    progressDialog.dismiss();
                                }
                            },
                            3000
                    );


                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alertDialog = builder.create();

            alertDialog.show();

            Window window1 = alertDialog.getWindow();
            window1.setLayout(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //Toast.makeText(getApplicationContext(), arrayForTimelineLocation.get(clickedItemIndex)+" clicked", Toast.LENGTH_SHORT).show();
        String locationToQuery = arrayForTimelineLocation.get(clickedItemIndex);
        String dateToQuery = arrayForTimelineDate.get(clickedItemIndex);
        String fuelTypeToQuery = arrayForTimelineFuelType.get(clickedItemIndex);
        String costToQuery = arrayForTimelineCost.get(clickedItemIndex);
        String litresToQuery = arrayForTimelineLitres.get(clickedItemIndex);

        SQLiteDatabase db = (new FuelDbHelper(getApplicationContext())).getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from " + FuelContract.FuelEntry.TABLE_NAME + " where " +

                FuelContract.FuelEntry.COLUMN_LOCATION + "=? and " +
                FuelContract.FuelEntry.COLUMN_FUEL_TYPE + "=? and " +
                FuelContract.FuelEntry.COLUMN_MONEY + "=? and " +
                FuelContract.FuelEntry.COLUMN_LITRES + "=? and " +
                FuelContract.FuelEntry.COLUMN_TIME_FILLED + "=?", new String[]{locationToQuery, fuelTypeToQuery, costToQuery, litresToQuery, dateToQuery});

        String lat = "", lon = "";

        if (!(!cursor.moveToFirst() || cursor.getCount() == 0)) {
            Log.d("database", "lat and lon updated");
            lat = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LATITUDE));
            lon = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LONGITUDE));
        }


        Log.d("database", "lat: " + lat + " , lon: " + lon);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (lat != "" && lon != "") {


            Uri uri = Uri.parse("geo:0,0?q=" + lat + "," + lon + "(" + arrayForTimelineLocation.get(clickedItemIndex) + ")");
            intent.setData(uri);
            if (intent.resolveActivity(getPackageManager()) != null)
                startActivity(intent);
        } else {
            Log.d("database", "error while opening maps.");
            Toast.makeText(getApplicationContext(), "error while opening maps.", Toast.LENGTH_SHORT).show();
        }

        db.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (shouldDisplayMenuDelete) {
            getMenuInflater().inflate(R.menu.menu_timeline, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    protected void getDataForTimeline() {
        SQLiteDatabase database = new FuelDbHelper(getApplicationContext()).getReadableDatabase();

        Cursor cursor = database.rawQuery("select * from " + FuelContract.FuelEntry.TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            totalCost = 0;
            arrayForTimelineDate.clear();
            arrayForTimelineFuelType.clear();
            arrayForTimelineLocation.clear();
            arrayForTimelineCost.clear();
            arrayForTimelineLitres.clear();

            while (!cursor.isAfterLast()) {
                String fuelType = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_FUEL_TYPE));
                String date = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_TIME_FILLED));
                String location = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LOCATION));
                String litres = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LITRES));
                String cost = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));
                double dCost =  Double.parseDouble(cost);

                if(!((dCost - (int) dCost) > 0.0))
                    cost = "" + (int) dCost;

                arrayForTimelineDate.add(date);
                arrayForTimelineFuelType.add(fuelType);
                arrayForTimelineLocation.add(location);
                arrayForTimelineCost.add(cost);
                arrayForTimelineLitres.add(litres + "L");
                totalCost += Double.parseDouble(cost);
                cursor.moveToNext();
            }
        }

        database.close();
    }

}
