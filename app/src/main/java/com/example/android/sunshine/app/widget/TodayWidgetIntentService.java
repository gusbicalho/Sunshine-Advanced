package com.example.android.sunshine.app.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.android.sunshine.app.MainActivity;
import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;

import java.util.Date;

/**
 * Created by Gustavo on 30/07/2015.
 */
public class TodayWidgetIntentService extends IntentService {
    private static final String LOG_TAG = TodayWidgetIntentService.class.getSimpleName();
    private static final int TODAY_LOADER = 0;

    private static final String[] TODAY_WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        Cursor cursor = retrieveTodayData(this);
        Integer weatherId = cursor != null ? cursor.getInt(INDEX_WEATHER_ID) : null;
        Double maxTemp = cursor != null ? cursor.getDouble(INDEX_MAX_TEMP) : null;
        Double minTemp = cursor != null ? cursor.getDouble(INDEX_MIN_TEMP) : null;

        int mediumBreakpoint =
                Math.round(
                        getResources().getDimension(R.dimen.widget_today_default_width) /
                                getResources().getDisplayMetrics().density
                );
        int largeBreakpoint = Math.round(
                getResources().getDimension(R.dimen.widget_today_large_width) /
                        getResources().getDisplayMetrics().density
        );

        for (int appWidgetId : appWidgetIds) {
            int width = getResources().getDimensionPixelSize(R.dimen.widget_today_default_width);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
                width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, width);
            }
            int layoutId =
                    width < mediumBreakpoint ? R.layout.widget_today_small :
                    width < largeBreakpoint ? R.layout.widget_today :
                            R.layout.widget_today_large;
            RemoteViews updatedViews = buildUpdatedViews(this, layoutId, weatherId, maxTemp, minTemp);
            appWidgetManager.updateAppWidget(appWidgetId, updatedViews);
        }
    }

    private Cursor retrieveTodayData(Context context) {
        String location = Utility.getPreferredLocation(context);
        if (location == null)
            return null;
        Cursor cursor =
                getContentResolver().query(
                        WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, new Date().getTime()),
                        TODAY_WEATHER_PROJECTION, null, null, null);
        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    private RemoteViews buildUpdatedViews(Context context, int layoutId, Integer weatherId, Double maxTemp, Double minTemp) {
        RemoteViews views = new RemoteViews(context.getPackageName(), layoutId);
        // Create an Intent to launch MainActivity
        Intent launchIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        if (weatherId != null && maxTemp != null && minTemp != null) {
            int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
            String description = Utility.getStringForWeatherCondition(context, weatherId);
            String formattedMaxTemperature = Utility.formatTemperature(context, maxTemp);
            String formattedMinTemperature = Utility.formatTemperature(context, minTemp);

            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
            views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);
            views.setTextViewText(R.id.widget_description, description);
        }

        return views;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }
}
