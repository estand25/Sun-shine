package com.example.android.sunshine.app;

/**
 * Created by StandleyEugene on 7/9/2016.
 */

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private ShareActionProvider mShareActionProvider;
    private static final int DETAIL_LOADER = 0;
    private String mForecast;
    private static final String[] DETAIL_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_HUMIDITY = 5;
    public static final int COL_WEATHER_WIND_SPEED = 6;
    public static final int COL_WEATHER_DEGREES = 7;
    public static final int COL_WEATHER_PRESSURE = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    public void onCreateOptionMenu(Menu menu, MenuInflater inflater){
        // Inflate the menu; this adds items to the action bar if it is present/
        inflater.inflate(R.menu.detailfragment, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Attach an intent to this ShareActionProvider. You can update this at any time,
        // like when the user selects a new pieve of data they might like to share
        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    private Intent createShareForecastIntent(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    /*
    Creates and returns our cursorLoader
    */
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle){
        Log.v(LOG_TAG, "In onCreateLoader");

        Intent intent = getActivity().getIntent();
        if(intent == null){
            return null;
        }

        // Now create and return a CursorLoader that will take care of
        // creating a cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                intent.getData(),
                DETAIL_COLUMNS,
                null,
                null,
                null
        );
    }

    /*
        Remove curse data for cursorAdapter before they are destroy
     */
    public void onLoaderReset(Loader<Cursor> loader){}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER,null,this);

        super.onActivityCreated(savedInstanceState);
    }

    /*
        Set the ready curse data to the curseAdapter for uses
     */
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        Log.v(LOG_TAG,"In onLoadFinished");
        if(!data.moveToFirst()){ return;}

        // Get the current view information
        ViewHolder viewHolder = (ViewHolder)  getView().getTag();

        // Read weather condition id from cursor
        int weatherId = data.getInt(COL_WEATHER_ID);
        viewHolder.iconView.setImageResource(R.drawable.ic_launcher);

        // Get and set the day string
        String dayString = Utility.getDayName(getActivity(),
                data.getLong(COL_WEATHER_DATE));
        viewHolder.dayView.setText(dayString);

        // Get and set the date string
        String dataString = Utility.getFormattedMonthDay(getActivity(),
                data.getLong(COL_WEATHER_DATE));
        viewHolder.dateView.setText(dataString);

        // Get and set the weather description
        String weatherDescription =
                data.getString(COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(weatherDescription);

        // Get the current temp setting metric or imperial
        boolean isMetric = Utility.isMetric(getActivity());

        // Get and set the high temperature
        String high = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MAX_TEMP),isMetric);
        viewHolder.highTempView.setText(high);

        // Get and set the low temperature
        String low = Utility.formatTemperature(getActivity(),
                data.getDouble(COL_WEATHER_MIN_TEMP),isMetric);
        viewHolder.lowTempView.setText(low);

        // Get and set the humidity (also add correct symbol to end in UI)
        double humidity = data.getDouble(COL_WEATHER_HUMIDITY);
        viewHolder.humidityView.setText(getActivity().getApplicationContext().
                getString(R.string.format_humidity,humidity));

        // Get and set the wind (also add correct symbols to end in UI)
        String windDegree = Utility.getFormattedWind(getActivity(),
                data.getFloat(COL_WEATHER_WIND_SPEED),
                data.getFloat(COL_WEATHER_DEGREES));
        viewHolder.windDegreeView.setText(windDegree);

        // Get and set the pressure (also add correct symbols to end in UI)
        double pressure = data.getDouble(COL_WEATHER_PRESSURE);
        viewHolder.pressureView.setText(getActivity().
                getString(R.string.format_pressure,pressure));

        // if onCreateOptionMenu has already happend, we need to update the shared intent now.
        if(mShareActionProvider != null){
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }
    /**
     * Cache of the childer views for a forecast list item
     */
    public static class ViewHolder{
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView dayView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;
        public final TextView humidityView;
        public final TextView windDegreeView;
        public final TextView pressureView;

        public ViewHolder(View view){
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            dayView = (TextView) view.findViewById(R.id.list_item_day_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
            humidityView = (TextView) view.findViewById(R.id.list_item_humidity_textview);
            windDegreeView = (TextView) view.findViewById(R.id.list_item_winddegree_textview);
            pressureView = (TextView) view.findViewById(R.id.list_item_pressure_textview);
        }
    }

}
