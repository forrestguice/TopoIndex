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

public class AppSettings
{
    public static final String TAG = "TopoIndexSettings";

    public static final String KEY_LOCATION_AUTO = "locationAuto";
    public static final boolean DEF_LOCATION_AUTO = true;

    public static final String KEY_LOCATION_LAT = "latitude";
    public static final float DEF_LOCATION_LAT = 0;

    public static final String KEY_LOCATION_LON = "longitude";
    public static final float DEF_LOCATION_LON = 0;

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

        public Location(double latitude, double longitude)
        {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude()
        {
            return latitude;
        }

        public double getLongitude()
        {
            return longitude;
        }
    }

}
