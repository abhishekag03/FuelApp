package com.example.android.fuelapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.fuelapp.data.FuelAdapter;
import com.example.android.fuelapp.data.FuelContract;
import com.example.android.fuelapp.data.FuelDbHelper;
import com.example.android.fuelapp.model.Orientation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimelineActivity extends AppCompatActivity implements FuelAdapter.ListItemClickListener
{


    private RecyclerView mRecyclerView;
    private FuelAdapter mFuelAdapter;
    private Orientation mOrientation;
    private boolean mWithLinePadding;


    public static List<String> arrayForTimelineFuelType=new ArrayList<>();
    public static List<String> arrayForTimelineDate=new ArrayList<>();
    public static List<String> arrayForTimelineLocation=new ArrayList<>();
    public static List<String> arrayForTimelineCost=new ArrayList<>();
    public static List<String> arrayForTimelineLitres=new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getDataForTimeline();

        if (arrayForTimelineDate.size() > 0) {


            setContentView(R.layout.activity_timeline);


            getSupportActionBar().setTitle("Timeline");
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

        }

        else
        {
            setContentView(R.layout.empty_timeline);

            getSupportActionBar().setTitle("Timeline");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        }
    }

    private void initView()
    {
        mFuelAdapter= new FuelAdapter(arrayForTimelineDate.size(), this, mOrientation, mWithLinePadding);
        mRecyclerView.setAdapter(mFuelAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //Toast.makeText(getApplicationContext(), arrayForTimelineLocation.get(clickedItemIndex)+" clicked", Toast.LENGTH_SHORT).show();
        String locationToQuery= arrayForTimelineLocation.get(clickedItemIndex);
        String dateToQuery= arrayForTimelineDate.get(clickedItemIndex);
        String fuelTypeToQuery= arrayForTimelineFuelType.get(clickedItemIndex);
        String costToQuery= arrayForTimelineCost.get(clickedItemIndex);
        String litresToQuery=arrayForTimelineLitres.get(clickedItemIndex);

        SQLiteDatabase db= (new FuelDbHelper(getApplicationContext())).getReadableDatabase();

        Cursor cursor=db.rawQuery("select * from "+ FuelContract.FuelEntry.TABLE_NAME+" where "+

                FuelContract.FuelEntry.COLUMN_LOCATION+"=? and "+
                FuelContract.FuelEntry.COLUMN_FUEL_TYPE+"=? and "+
                FuelContract.FuelEntry.COLUMN_MONEY+"=? and "+
                FuelContract.FuelEntry.COLUMN_LITRES+"=? and "+
                FuelContract.FuelEntry.COLUMN_TIME_FILLED+"=?", new String[]{locationToQuery, fuelTypeToQuery, costToQuery, litresToQuery, dateToQuery});



        String lat="", lon="";

        if(!(!cursor.moveToFirst() || cursor.getCount()==0))
        {
            Log.d("database", "lat and lon updated");
                lat=cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LATITUDE));
                lon=cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LONGITUDE));
        }


        Log.d("database","lat: "+lat+" , lon: "+lon);
        Intent intent= new Intent(Intent.ACTION_VIEW);
        if(lat!="" && lon!="") {


            Uri uri = Uri.parse("geo:0,0?q=" + lat + "," + lon+"("+arrayForTimelineLocation.get(clickedItemIndex)+")");
            intent.setData(uri);
            if(intent.resolveActivity(getPackageManager())!=null)
                startActivity(intent);
        }
        else{
            Log.d("database", "error while opening maps.");
            Toast.makeText(getApplicationContext(), "error while opening maps.", Toast.LENGTH_SHORT).show();
        }

        db.close();

    }


    protected void getDataForTimeline()
    {
        SQLiteDatabase database=new FuelDbHelper(getApplicationContext()).getReadableDatabase();

        Cursor cursor= database.rawQuery("select * from "+ FuelContract.FuelEntry.TABLE_NAME, null );
        if (cursor.moveToFirst()) {


            arrayForTimelineDate.clear();
            arrayForTimelineFuelType.clear();
            arrayForTimelineLocation.clear();
            arrayForTimelineCost.clear();
            arrayForTimelineLitres.clear();

            while (!cursor.isAfterLast()) {
                String fuelType = cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_FUEL_TYPE));
                String date= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_TIME_FILLED));
                String location= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LOCATION));
                String litres= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_LITRES));
                String cost= cursor.getString(cursor.getColumnIndex(FuelContract.FuelEntry.COLUMN_MONEY));

                arrayForTimelineDate.add(date);
                arrayForTimelineFuelType.add(fuelType);
                arrayForTimelineLocation.add(location);
                arrayForTimelineCost.add(cost);
                arrayForTimelineLitres.add(litres);
                cursor.moveToNext();
            }
        }


        database.close();
    }

}
