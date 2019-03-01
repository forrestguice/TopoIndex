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
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AppSettings
{
    public static final String TAG = "TopoIndexSettings";

    public static final String KEY_COLLECTION_PATH = "collectionPath";            // TODO: expose
    public static final String[] DEF_COLLECTION_PATH = new String[] { "maps" };   // /sdcard/<PATH>

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

    public static final String KEY_FILTER_NAME = "filterByName";
    public static final String DEF_FILTER_NAME = "";    // def no filter (empty)

    public static final String KEY_FILTER_STATE = "filterByState";
    public static final String[] DEF_FILTER_STATE = new String[0];    // def no filter (empty)

    public static final String KEY_FILTER_SCALE = "filterByScale";
    public static final String DEF_FILTER_SCALE = TopoIndexDatabaseAdapter.MapScale.SCALE_ANY.getValue();

    public static final String KEY_FILTER_MINYEAR = "filterByMinYear";
    public static final int DEF_FILTER_MINYEAR = -1;           // def no min

    public static final String KEY_FILTER_MAXYEAR = "filterByMaxYear";
    public static final int DEF_FILTER_MAXYEAR = -1;            // def no max

    public static final String KEY_FILTER_PROXIMITY = "filterByProximity";
    public static final float DEF_FILTER_PROXIMITY = 0.0f;      // def no min

    public static final String KEY_LASTUPDATE_SELECTION = "lastUpdateSelection";
    public static final String[] DEF_LASTUPDATE_SELECTION = TopoIndexDatabaseAdapter.VAL_STATES.keySet().toArray(new String[0]);  // all states

    public static String[] getCollectionPath(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> paths = prefs.getStringSet(KEY_COLLECTION_PATH, new HashSet<String>(Arrays.asList(DEF_COLLECTION_PATH)));
        return new ArrayList<String>(paths).toArray(new String[0]);
    }
    public static void setCollectionPath(Context context, String[] paths)
    {
        if (paths.length > 0 && paths[0] != null && !paths[0].isEmpty())
        {
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefs.putStringSet(KEY_COLLECTION_PATH, new HashSet<String>(Arrays.asList(paths)));
            prefs.apply();
        } else Log.w(TAG, "setCollectionPath: at least one non-empty path must be provided; ignored");
    }
    public static void addCollectionPath(Context context, String path)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> paths = prefs.getStringSet(KEY_COLLECTION_PATH, new HashSet<String>(Arrays.asList(DEF_COLLECTION_PATH)));
        if (!paths.contains(path)) {
            paths.add(path);
        }
        SharedPreferences.Editor prefsEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEdit.putStringSet(KEY_COLLECTION_PATH, paths);
        prefsEdit.apply();
    }

    public static String[] getLastUpdateSelection(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> filterSet = prefs.getStringSet(KEY_LASTUPDATE_SELECTION, new HashSet<String>(Arrays.asList(DEF_LASTUPDATE_SELECTION)));
        return new ArrayList<String>(filterSet).toArray(new String[0]);
    }
    public static void setLastUpdateSelection(Context context, String[] filterSet)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putStringSet(KEY_LASTUPDATE_SELECTION, new HashSet<String>(Arrays.asList(filterSet)));
        prefs.apply();
    }

    public static boolean hasNoFilters(Context context)
    {
        if (context == null) {
            return true;
        }

        String nameFilter = getFilter_byName(context);
        String[] stateFilter = getFilter_byState(context);
        String scaleFilter = getFilter_byScale(context);
        return (nameFilter == null || nameFilter.isEmpty())
                && (stateFilter.length == 0)
                && (scaleFilter == null || scaleFilter.isEmpty());
    }

    public static TopoIndexDatabaseAdapter.FilterValues getFilters(Context context)
    {
        String nameFilter = getFilter_byName(context);
        String[] statesFilter = getFilter_byState(context);
        String scaleFilter = getFilter_byScale(context);
        return new TopoIndexDatabaseAdapter.FilterValues(nameFilter, statesFilter, scaleFilter);
    }

    public static String getFilter_byName(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_FILTER_NAME, DEF_FILTER_NAME);
    }
    public static void setFilter_byName(Context context, String value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(KEY_FILTER_NAME, value);
        prefs.apply();
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

    public static String getFilter_byScale(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(KEY_FILTER_SCALE, DEF_FILTER_SCALE);
    }
    public static void setFilter_byScale(Context context, String value)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putString(KEY_FILTER_SCALE, value);
        prefs.apply();
    }

    public static int getFilter_byMinYear(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_FILTER_MINYEAR, DEF_FILTER_MINYEAR);
    }
    public static void setFilter_byMinYear(Context context, int year)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(KEY_FILTER_MINYEAR, year);
        prefs.apply();
    }

    public static int getFilter_byMaxYear(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_FILTER_MAXYEAR, DEF_FILTER_MAXYEAR);
    }
    public static void setFilter_byMaxYear(Context context, int year)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(KEY_FILTER_MAXYEAR, year);
        prefs.apply();
    }

    public static float getFilter_byProximity(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(KEY_FILTER_PROXIMITY, DEF_FILTER_PROXIMITY);
    }
    public static void setFilter_byProximity(Context context, float proximity)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putFloat(KEY_FILTER_PROXIMITY, proximity);
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

        public String toString()
        {
            return getLongitudeDisplay() + ", " + getLatitudeDisplay();
        }

    }


}
