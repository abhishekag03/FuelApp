package com.example.android.fuelapp.data;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.fuelapp.MainActivity;
import com.example.android.fuelapp.R;
import com.example.android.fuelapp.TimelineActivity;
import com.example.android.fuelapp.model.Orientation;
import com.github.vipulasri.timelineview.TimelineView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by vishaal on 30/6/17.
 */

public class FuelAdapter extends RecyclerView.Adapter<FuelAdapter.NumberViewHolder>
{
    private static final String TAG= FuelAdapter.class.getSimpleName();

    final private ListItemClickListener mOnClickListener;

    private static int viewHolderCount;

    private Orientation mOrientation;

    private boolean mWithLinePadding;

    private LayoutInflater mLayoutInflater;

    private TimelineView mTimelineView;

    private List<String> mDataSet;

    private String DATE;
    private String DAY;
    private String TIME;
    private String MONTH;
    private int VIEW_TYPE_EMPTY = 90009;

    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context= parent.getContext();

        int layoutForListItem;

        if(viewType == VIEW_TYPE_EMPTY){
            layoutForListItem = R.layout.empty_timeline;
        }else {
            layoutForListItem = R.layout.list_item_update;
        }
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(layoutForListItem, parent, false);
            NumberViewHolder viewHolder = new NumberViewHolder(view, viewType);
            viewHolderCount++;
            return viewHolder;
    }

    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {

        if(holder.getItemViewType() != VIEW_TYPE_EMPTY) {

            int new_position = holder.getAdapterPosition();

            setDateVariables(TimelineActivity.arrayForTimelineDate.get(new_position));
            holder.bind(TimelineActivity.arrayForTimelineCost.get(new_position), TimelineActivity.arrayForTimelineLitres.get(new_position), TimelineActivity.arrayForTimelineLocation.get(position), DAY, DATE, MONTH, TIME);


            if (TimelineActivity.arrayForTimelineFuelType.get(new_position).equals("Petrol")) {
                mTimelineView.setMarker(holder.itemView.getResources().getDrawable(R.drawable.ic_marker_petrol));
            } else {
                mTimelineView.setMarker(holder.itemView.getResources().getDrawable(R.drawable.ic_marker));
            }
        }

    }

    @Override
    public int getItemCount() {
        if(mDataSet.size() == 0){
            return 1;
        }else {
            return mDataSet.size();
        }
    }

    public interface ListItemClickListener
    {
        void onListItemClick(int clickedItemIndex);
    }

    public FuelAdapter(List<String> dataSet, ListItemClickListener listener, Orientation orientation, boolean withLinePadding)
    {

        mOrientation=orientation;
        mWithLinePadding=withLinePadding;
        mDataSet=dataSet;
        mOnClickListener=listener;
        viewHolderCount=0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataSet.size() == 0) {
            return VIEW_TYPE_EMPTY;
        }else {
            return TimelineView.getTimeLineViewType(position, getItemCount());
        }
    }

    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView displayDateString;
        TextView displayLocationString;
        TextView displayFuelCostString;
        TextView displayDayString;
        TextView displayMonthString;
        TextView displayTimeString;
        TextView displayFuelLitresString;

        public NumberViewHolder(View itemView, int viewType) {
            super(itemView);
            if(viewType != VIEW_TYPE_EMPTY) {
                mTimelineView = (TimelineView) itemView.findViewById(R.id.time_marker);

                displayDateString = (TextView) itemView.findViewById(R.id.fuel_station_numberdate);
                displayDayString = (TextView) itemView.findViewById(R.id.fuel_station_day);
                displayMonthString = (TextView) itemView.findViewById(R.id.fuel_station_month);
                displayTimeString = (TextView) itemView.findViewById(R.id.fuel_station_time);
                displayFuelCostString = (TextView) itemView.findViewById(R.id.fuel_station_cost);
                displayFuelLitresString = (TextView) itemView.findViewById(R.id.fuel_station_litres);
                displayLocationString = (TextView) itemView.findViewById(R.id.fuel_station_location);


                Typeface oratorSTD = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/OratorStd.otf");
                Typeface segment7 = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/Segment7Standard.otf");

                displayFuelCostString.setTypeface(segment7);
                displayFuelLitresString.setTypeface(segment7);
                displayLocationString.setTypeface(oratorSTD);
                displayTimeString.setTypeface(oratorSTD);
                displayMonthString.setTypeface(oratorSTD);

                mTimelineView.initLine(viewType);

                //            mTimelineView.setMarker(ContextCompat.getDrawable(itemView.getContext(), R.mipmap.diesel_drop));
                mTimelineView.setMarkerSize(75);
                itemView.setOnClickListener(this);
            }

        }


        void bind(String cost, String litres, String location, String day, String date, String month, String time)
        {
            displayFuelCostString.setText("₹"+cost);
            displayFuelLitresString.setText(litres);
            displayLocationString.setText(location);
            displayDayString.setText(day);
            displayDateString.setText(date);
            displayMonthString.setText(month);
            displayTimeString.setText(time);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition=getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }

    }

    protected void setDateVariables(String date)
    {
        String preDate=date.substring(0,10);

        String year=preDate.substring(0,4);
        String month=preDate.substring(5,7);
        String day=preDate.substring(8);
        String time=date.substring(11);

        try {
            DAY=getDay(setProperDate(preDate));
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("database", "ParseException for date");
        }

        DATE=day;

        TIME=getTimeInFormat(time);

        MONTH=getMonth(month);

    }


    protected String setProperDate(String date)
    {
        String year=date.substring(0,4);
        String month=date.substring(5,7);
        String day=date.substring(8);

        String dateToReturn= day+"/"+month+"/"+year;
        return dateToReturn;
    }


    protected String getDay(String date) throws ParseException {

        SimpleDateFormat format= new SimpleDateFormat("dd/MM/yyyy");
        Date date1=format.parse(date);
        DateFormat format1= new SimpleDateFormat("EEEE");
        String dayToPass=format1.format(date1);
        return dayToPass;
    }



    protected String getMonth(String m)
    {
       switch (Integer.parseInt(m))
       {
           case 1:  return "January";

           case 2:  return "February";

           case 3:  return "March";

           case 4:  return "April";

           case 5:  return "May";

           case 6:  return "June";

           case 7:  return "July";

           case 8:  return "August";

           case 9:  return "September";

           case 10: return "October";

           case 11: return "November";

           case 12: return "December";

           default: throw new NumberFormatException("Not between 1 and 12");
       }
    }


    protected String getTimeInFormat(String time)
    {


        String hour=time.substring(0,2);
        String min=time.substring(3,5);

        int hourInInt=Integer.parseInt(hour);


        String AMPM="";


        if(hourInInt==0)
        {
            hourInInt=12;
            AMPM="AM";
        }

        if(hourInInt>=13)
        {
            AMPM="PM";
            hourInInt-=12;
        }

        else if(hourInInt!=0 && hourInInt<13)
        {
            AMPM="AM";
        }

        String timeToPass=String.valueOf(hourInInt)+":"+min+AMPM;

        return timeToPass;

    }



}
