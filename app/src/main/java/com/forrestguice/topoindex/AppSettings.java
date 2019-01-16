/**
    Copyright (C) 2019 Forrest Guice
    This file is part of TopoIndex.

    TopoIndex is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    TopoIndex is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with TopoIndex.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.topoindex;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSettings
{
    public static final String TAG = "TopoIndexSettings";

    public static final String KEY_FILTER_STATE = "filterByState";
    public static final String[] DEF_FILTER_STATE = new String[0];   // no filter (empty)

    public static final String KEY_COLLECTION_PATH = "collectionPath";            // TODO: expose
    public static final String DEF_COLLECTION_PATH = "maps";

    public static final String KEY_LOCATION_INTERVAL = "locationInterval";        // TODO: expose
    public static final long DEF_LOCATION_INTERVAL = 5 * 1000;

    public static final String KEY_LOCATION_MAXAGE = "locationMaxAge";            // TODO: expose
    public static final long DEF_LOCATION_MAXAGE = 60 * 1000;

    public static final String KEY_LOCATION_AUTO = "locationAuto";
    public static final boolean DEF_LOCATION_AUTO = true;

    public static final String KEY_LOCATION_LAT = "latitude";
    public static final float DEF_LOCATION_LAT = 0;

    public static final String KEY_LOCATION_LON = "longitude";
    public static final float DEF_LOCATION_LON = 0;

    public static String getCollectionPath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_COLLECTION_PATH, DEF_COLLECTION_PATH);
    }

    public static String[] getFilter_byState(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> filterSet = prefs.getStringSet(KEY_FILTER_STATE, new HashSet<String>(Arrays.asList(DEF_FILTER_STATE)));
        return new ArrayList<String>(filterSet).toArray(new String[0]);
    }
    public static void setFilter_byState(Context context, String[] filterSet)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putStringSet(KEY_FILTER_STATE, new HashSet<String>(Arrays.asList(filterSet)));
        prefs.apply();
    }

    public static long getLocationInterval(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_LOCATION_INTERVAL, DEF_LOCATION_INTERVAL);
    }

    public static long getLocationMaxAge(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_LOCATION_MAXAGE, DEF_LOCATION_MAXAGE);
    }

    public static boolean getAutoLocation(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(KEY_LOCATION_AUTO, DEF_LOCATION_AUTO);
    }
    public static void setAutoLocation(Context context, boolean value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putBoolean(KEY_LOCATION_AUTO, value);
        prefs.apply();
    }

    public static Location getLocation(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return new Location(prefs.getFloat(KEY_LOCATION_LAT, DEF_LOCATION_LAT), prefs.getFloat(KEY_LOCATION_LON, DEF_LOCATION_LON));
    }

    public static void setLocation(Context context, Location location)
    {
        setLocation(context, location.getLatitude(), location.getLongitude());
    }
    public static void setLocation(Context context, double latitude, double longitude)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putFloat(KEY_LOCATION_LAT, (float)latitude);
        prefs.putFloat(KEY_LOCATION_LON, (float)longitude);
        prefs.apply();
    }

    /**
     * Location
     */
    public static final class Location
    {
        protected double latitude, longitude;
        private static NumberFormat formatter = null;

        public Location(double latitude, double longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public String getLatitudeDisplay()
        {
            if (formatter == null) {
                formatter = getFormatter();
            }
            return formatter.format(latitude);
        }

        public double getLongitude()
        {
            return longitude;
        }

        public String getLongitudeDisplay()
        {
            if (formatter == null) {
                formatter = getFormatter();
            }
            return formatter.format(longitude);
        }

        public static NumberFormat getFormatter()
        {
            NumberFormat retValue = DecimalFormat.getNumberInstance();
            retValue.setMinimumFractionDigits(0);
            retValue.setMaximumFractionDigits(4);
            return retValue;
        }

    }


}
