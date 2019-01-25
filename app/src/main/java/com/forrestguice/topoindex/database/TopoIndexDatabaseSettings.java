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

package com.forrestguice.topoindex.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TopoIndexDatabaseSettings
{
    public static final String TAG = "TopoIndexSettings";

    public static final String KEY_DATABASE_DATE = "database_date";                     // these keys should match those in pref_database.xml
    public static final String KEY_DATABASE_LASTUPDATE = "database_lastupdate";
    public static final String KEY_DATABASE_UPDATENOW = "database_update";
    public static final String KEY_DATABASE_LASTSCAN = "database_lastscan";

    public static long getDatabaseDate(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_DATABASE_DATE, -1);
    }
    public static void setDatabaseDate(Context context, long datetime)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(KEY_DATABASE_DATE, datetime);
        prefs.apply();
    }

    public static long getDatabaseLastUpdate(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_DATABASE_LASTUPDATE, -1);
    }
    public static void setDatabaseLastUpdate(Context context, long datetime)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(KEY_DATABASE_LASTUPDATE, datetime);
        prefs.apply();
    }

    public static long getDatabaseLastScan(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getLong(KEY_DATABASE_LASTSCAN, -1);
    }
    public static void setDatabaseLastScan(Context context, long datetime)
    {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putLong(KEY_DATABASE_LASTSCAN, datetime);
        prefs.apply();
    }

}
