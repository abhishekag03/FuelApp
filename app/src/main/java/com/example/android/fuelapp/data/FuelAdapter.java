package com.example.android.fuelapp.data;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
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

    private int mNumberitems;

    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context= parent.getContext();
        int layoutForListItem=R.layout.list_item;
        LayoutInflater inflater=LayoutInflater.from(context);
        View view= inflater.inflate(layoutForListItem, parent, false);
        NumberViewHolder viewHolder= new NumberViewHolder(view, viewType);
        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "+viewHolderCount);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {

        Log.d(TAG, "#"+position);
        holder.bind(TimelineActivity.arrayForTimelineDate.get(position), TimelineActivity.arrayForTimelineFuelType.get(position), TimelineActivity.arrayForTimelineCost.get(position), TimelineActivity.arrayForTimelineLitres.get(position), TimelineActivity.arrayForTimelineLocation.get(position));
    }

    @Override
    public int getItemCount() {
        return mNumberitems;
    }

    public interface ListItemClickListener
    {
        void onListItemClick(int clickedItemIndex);
    }

    public FuelAdapter(int numberOfItems, ListItemClickListener listener, Orientation orientation, boolean withLinePadding)
    {

        mOrientation=orientation;
        mWithLinePadding=withLinePadding;
        mNumberitems=numberOfItems;
        mOnClickListener=listener;
        viewHolderCount=0;
    }

    @Override
    public int getItemViewType(int position) {
        return TimelineView.getTimeLineViewType(position, getItemCount());
    }

    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        TextView displayDateString;
        TextView displayFuelTypeString;
        TextView displayFuelCostString;
        TextView displayFuelLitresString;
        TextView displayLocationString;

        public NumberViewHolder(View itemView, int viewType) {
            super(itemView);
            mTimelineView= (TimelineView) itemView.findViewById(R.id.time_marker);
            displayDateString=(TextView) itemView.findViewById(R.id.fuel_station_date);
            displayLocationString=(TextView) itemView.findViewById(R.id.fuel_station_location);
            displayFuelCostString=(TextView) itemView.findViewById(R.id.fuel_station_fuel_price);
            displayFuelLitresString=(TextView) itemView.findViewById(R.id.fuel_station_fuel_rate);
            displayFuelTypeString=(TextView) itemView.findViewById(R.id.fuel_station_fuel_type);
            mTimelineView.initLine(viewType);
            itemView.setOnClickListener(this);
        }


        void bind(String date, String fuelType, String cost, String litres, String Location)
        {
            displayDateString.setText(date);
            displayFuelTypeString.setText(fuelType);
            displayLocationString.setText(Location);
            displayFuelCostString.setText("₹"+cost);
            displayFuelLitresString.setText(litres);
        }

        @Override
        public void onClick(View v) {
            int clickedPosition=getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }



    }

}
