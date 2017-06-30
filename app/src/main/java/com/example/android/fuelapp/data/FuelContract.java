package com.example.android.fuelapp.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vishaal on 28/6/17.
 */

public class FuelContract
{

    public static final String AUTHORITY ="com.example.android.fuelapp";

    public static final Uri BASE_CONTENT_URI= Uri.parse("content://"+AUTHORITY);

    public static final String PATH_FUEL="fuel";

    public static final class FuelEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI =BASE_CONTENT_URI.buildUpon().appendPath(PATH_FUEL).build();

        public static final String TABLE_NAME="fuel";

        public static final String COLUMN_TIME_FILLED="time_filled";

        public static final String COLUMN_MONEY="money";

        public static final String COLUMN_FUEL_TYPE="fuel_type";

        public static final String COLUMN_LITRES="litres";

        public static final String COLUMN_LATITUDE="latitude";

        public static final String COLUMN_LONGITUDE="longitude";

        public static final String COLUMN_LOCATION="location";

    }

}
