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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;

public class TopoIndexDatabaseAdapter
{
    private static final String DATABASE_NAME = "topoindex";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_MAP_SERIES = "series";
    public static final String DEF_MAP_SERIES = KEY_MAP_SERIES + " text not null";
    public static final String VAL_MAP_SERIES_HTMC = "\"HTMC\"";
    public static final String VAL_MAP_SERIES_USTOPO = "\"US Topo\"";

    public static final String KEY_MAP_VERSION = "version";
    public static final String DEF_MAP_VERSION = KEY_MAP_VERSION + " text";

    public static final String KEY_MAP_GDAITEMID = "gdaitemid";
    public static final String DEF_MAP_GDAITEMID = KEY_MAP_GDAITEMID + " text";

    public static final String KEY_MAP_CELLID = "cellid";
    public static final String DEF_MAP_CELLID = KEY_MAP_CELLID + " text";

    public static final String KEY_MAP_SCANID = "scanid";
    public static final String DEF_MAP_SCANID = KEY_MAP_SCANID + " text";

    public static final String KEY_MAP_NAME = "name";
    public static final String DEF_MAP_NAME = KEY_MAP_NAME + " text not null";

    public static final String KEY_MAP_STATE = "state";
    public static final String DEF_MAP_STATE = KEY_MAP_STATE + " text not null";

    public static final String KEY_MAP_DATE = "mapdate";
    public static final String DEF_MAP_DATE = KEY_MAP_DATE + " text not null";

    public static final String KEY_MAP_LATITUDE_NORTH = "nlat";
    public static final String DEF_MAP_LATITUDE_NORTH = KEY_MAP_LATITUDE_NORTH + " text";

    public static final String KEY_MAP_LONGITUDE_WEST = "wlon";
    public static final String DEF_MAP_LONGITUDE_WEST = KEY_MAP_LONGITUDE_WEST + " text";

    public static final String KEY_MAP_LATITUDE_SOUTH = "slat";
    public static final String DEF_MAP_LATITUDE_SOUTH = KEY_MAP_LATITUDE_SOUTH + " text";

    public static final String KEY_MAP_LONGITUDE_EAST = "elon";
    public static final String DEF_MAP_LONGITUDE_EAST = KEY_MAP_LONGITUDE_EAST + " text";

    public static final String KEY_MAP_SCALE = "scale";
    public static final String DEF_MAP_SCALE = KEY_MAP_SCALE + " text";

    public static final String KEY_MAP_DATUM = "datum";
    public static final String DEF_MAP_DATUM = KEY_MAP_DATUM + " text";

    public static final String KEY_MAP_PROJECTION = "projection";
    public static final String DEF_MAP_PROJECTION = KEY_MAP_PROJECTION + " text";

    public static final String KEY_MAP_URL = "url0";                           // url: geo pdf download (pre Jan 2018; legacy)
    public static final String DEF_MAP_URL = KEY_MAP_URL + " text";

    public static final String KEY_MAP_URL1 = "url1";                          // url: S3 cloud download (post Jan 2018)
    public static final String DEF_MAP_URL1 = KEY_MAP_URL1 + " text";

    public static final String KEY_MAP_URL2 = "url2";                          // url: (reserved) not-used
    public static final String DEF_MAP_URL2 = KEY_MAP_URL2 + " text";

    private static final String[] QUERY_MAPS_MINENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_GDAITEMID, KEY_MAP_CELLID, KEY_MAP_SCANID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE, KEY_MAP_SCALE, KEY_MAP_LATITUDE_NORTH, KEY_MAP_LONGITUDE_WEST, KEY_MAP_LATITUDE_SOUTH, KEY_MAP_LONGITUDE_EAST, KEY_MAP_URL, KEY_MAP_URL1, KEY_MAP_URL2};
    private static final String[] QUERY_MAPS_FULLENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_GDAITEMID, KEY_MAP_CELLID, KEY_MAP_SCANID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE, KEY_MAP_SCALE, KEY_MAP_DATUM, KEY_MAP_PROJECTION, KEY_MAP_LATITUDE_NORTH, KEY_MAP_LONGITUDE_WEST, KEY_MAP_LATITUDE_SOUTH, KEY_MAP_LONGITUDE_EAST, KEY_MAP_URL, KEY_MAP_URL1, KEY_MAP_URL2};

    /**
     * USGS HTMC (Historical Topo Collection)
     */
    public static final String TABLE_MAPS_USGS_HTMC = "usgs_htmc";
    private static final String TABLE_MAPS_USGS_HTMC_CREATE_COLS = DEF_ROWID + ", "
            + DEF_MAP_SERIES + ", "
            + DEF_MAP_VERSION + ", "
            + DEF_MAP_GDAITEMID + ", "
            + DEF_MAP_CELLID + ", "
            + DEF_MAP_SCANID + ", "
            + DEF_MAP_NAME + ", "
            + DEF_MAP_STATE + ", "
            + DEF_MAP_SCALE + ", "
            + DEF_MAP_DATE + ", "
            + DEF_MAP_DATUM + ", "
            + DEF_MAP_PROJECTION + ", "
            + DEF_MAP_LATITUDE_NORTH + ", "
            + DEF_MAP_LONGITUDE_WEST + ", "
            + DEF_MAP_LATITUDE_SOUTH + ", "
            + DEF_MAP_LONGITUDE_EAST + ", "
            + DEF_MAP_URL + ", "
            + DEF_MAP_URL1 + ", "
            + DEF_MAP_URL2;
    private static final String TABLE_MAPS_USGS_HTMC_CREATE = "create table " + TABLE_MAPS_USGS_HTMC + " (" + TABLE_MAPS_USGS_HTMC_CREATE_COLS + ");";

    /**
     * USGS US Topo (Current quadrangles; 2010 and later)
     */
    public static final String TABLE_MAPS_USGS_USTOPO = "usgs_ustopo";
    private static final String TABLE_MAPS_USGS_USTOPO_CREATE_COLS = DEF_ROWID + ", "
            + DEF_MAP_SERIES + ", "
            + DEF_MAP_VERSION + ", "
            + DEF_MAP_GDAITEMID + ", "
            + DEF_MAP_CELLID + ", "
            + DEF_MAP_SCANID + ", "
            + DEF_MAP_NAME + ", "
            + DEF_MAP_STATE + ", "
            + DEF_MAP_SCALE + ", "
            + DEF_MAP_DATE + ", "
            + DEF_MAP_DATUM + ", "
            + DEF_MAP_PROJECTION + ", "
            + DEF_MAP_LATITUDE_NORTH + ", "
            + DEF_MAP_LONGITUDE_WEST + ", "
            + DEF_MAP_LATITUDE_SOUTH + ", "
            + DEF_MAP_LONGITUDE_EAST + ", "
            + DEF_MAP_URL + ", "
            + DEF_MAP_URL1 + ", "
            + DEF_MAP_URL2;
    private static final String TABLE_MAPS_USGS_USTOPO_CREATE = "create table " + TABLE_MAPS_USGS_USTOPO + " (" + TABLE_MAPS_USGS_USTOPO_CREATE_COLS + ");";

    /**
     * Local Topo (files on the local drive)
     */
    public static final String TABLE_MAPS = "maps";
    private static final String TABLE_MAPS_CREATE_COLS = DEF_ROWID + ", "
            + DEF_MAP_SERIES + ", "
            + DEF_MAP_VERSION + ", "
            + DEF_MAP_GDAITEMID + ", "
            + DEF_MAP_CELLID + ", "
            + DEF_MAP_SCANID + ", "
            + DEF_MAP_NAME + ", "
            + DEF_MAP_STATE + ", "
            + DEF_MAP_SCALE + ", "
            + DEF_MAP_DATE + ", "
            + DEF_MAP_DATUM + ", "
            + DEF_MAP_PROJECTION + ", "
            + DEF_MAP_LATITUDE_NORTH + ", "
            + DEF_MAP_LONGITUDE_WEST + ", "
            + DEF_MAP_LATITUDE_SOUTH + ", "
            + DEF_MAP_LONGITUDE_EAST + ", "
            + DEF_MAP_URL + ", "
            + DEF_MAP_URL1 + ", "
            + DEF_MAP_URL2;
    private static final String TABLE_MAPS_CREATE = "create table " + TABLE_MAPS + " (" + TABLE_MAPS_CREATE_COLS + ");";

    /**
     * TopoIndexDatabaseAdapter
     */
    private final Context context;
    private SQLiteDatabase database;
    private DatabaseHelper databaseHelper;

    public TopoIndexDatabaseAdapter(Context context)
    {
        this.context = context;
    }

    public TopoIndexDatabaseAdapter open() throws SQLException
    {
        if (databaseHelper != null) {
            databaseHelper.close();
        }
        databaseHelper = new DatabaseHelper(context);
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        databaseHelper.close();
        database = null;
    }

    /**
     * Get Maps
     */

    public Cursor getMaps(int n, boolean fullEntry)
    {
        return getMaps(TABLE_MAPS, n, fullEntry);
    }

    public Cursor getMaps(@NonNull String table, int n, boolean fullEntry)
    {
        return getMaps(table, n, fullEntry, null);
    }

    public Cursor getMaps(@NonNull String table, int n, boolean fullEntry, String nameFilter)
    {
        String[] query = (fullEntry) ? QUERY_MAPS_FULLENTRY : QUERY_MAPS_MINENTRY;
        String selection = null;
        String[] selectionArgs = null;

        if (nameFilter != null) {
            selection = KEY_MAP_NAME + " LIKE ?";
            selectionArgs = new String[] {"%" + nameFilter + "%"};
        }

        Cursor cursor =  (n > 0) ? database.query( table, query, selection, selectionArgs, null, null, "_id DESC", n+"" )
                                 : database.query( table, query, selection, selectionArgs, null, null, "_id DESC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getMaps_HTMC(int n, boolean fullEntry)
    {
        return getMaps(TABLE_MAPS_USGS_HTMC, n, fullEntry);
    }

    public Cursor getMaps_USTopo(int n, boolean fullEntry)
    {
        return getMaps(TABLE_MAPS_USGS_USTOPO, n, fullEntry);
    }

    public Cursor getMap_HTMC(String table, @NonNull String scanID, boolean fullEntry)
    {
        String[] query = (fullEntry) ? QUERY_MAPS_FULLENTRY : QUERY_MAPS_MINENTRY;
        String selection = KEY_MAP_SCANID + " = ?";
        String[] selectionArgs = new String[] { scanID };
        Cursor cursor = database.query( table, query, selection, selectionArgs, null, null, "_id DESC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getMap_USTopo(String table, @NonNull String gdaItemID, boolean fullEntry)
    {
        String[] query = (fullEntry) ? QUERY_MAPS_FULLENTRY : QUERY_MAPS_MINENTRY;
        String selection = KEY_MAP_GDAITEMID + " = ?";
        String[] selectionArgs = new String[] { gdaItemID };
        Cursor cursor = database.query( table, query, selection, selectionArgs, null, null, "_id DESC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean hasMap_HTMC(@NonNull String table, String scanID)
    {
        Cursor cursor = getMap_HTMC(table, scanID, false);
        if (cursor != null) {
            return (cursor.getCount() > 0);
        }
        return false;
    }

    public boolean hasMap_USTopo(@NonNull String table, String gdaItemID)
    {
        Cursor cursor = getMap_USTopo(table, gdaItemID, false);
        if (cursor != null) {
            return (cursor.getCount() > 0);
        }
        return false;
    }

    /**
     * Add Maps
     */

    public long addMaps(String table, ContentValues... values)
    {
        long lastRowId = -1;
        database.beginTransaction();
        for (ContentValues entry : values) {
            lastRowId = database.insert(table, null, entry);
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return lastRowId;
    }

    public long addMaps_HTMC(ContentValues... values)
    {
        return addMaps(TABLE_MAPS_USGS_HTMC, values);
    }

    public long addMaps_USTopo(ContentValues... values)
    {
        return addMaps(TABLE_MAPS_USGS_USTOPO, values);
    }

    public long updateMaps_HTMC(String table, ContentValues... values)
    {
        long lastRowId = -1;
        database.beginTransaction();
        for (ContentValues entry : values)
        {
            String scanID = entry.getAsString(KEY_MAP_SCANID);         // HTMC rows matched by "Scan ID" (54)
            if (scanID != null && !scanID.isEmpty())
            {
                String where = KEY_MAP_SCANID + " = ?";
                String[] whereArgs = new String[] { scanID };
                lastRowId = database.update(table, entry, where, whereArgs);
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return lastRowId;
    }

    public long updateMaps_USTopo(String table, ContentValues... values)
    {
        long lastRowId = -1;
        database.beginTransaction();
        for (ContentValues entry : values)
        {
            String itemID = entry.getAsString(KEY_MAP_GDAITEMID);        // US Topo rows matched by "GDA Item ID" (55)
            if (itemID != null && !itemID.isEmpty())
            {
                String where = KEY_MAP_GDAITEMID + " = ?";
                String[] whereArgs = new String[] { itemID };
                lastRowId = database.update(table, entry, where, whereArgs);
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        return lastRowId;
    }

    public static void toContentValues( ContentValues values, String[] fields )
    {
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_SERIES, fields[0].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_VERSION, fields[1].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_CELLID, fields[2].replaceAll("\"",""));

        values.put(TopoIndexDatabaseAdapter.KEY_MAP_NAME, fields[3].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_STATE, fields[4].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_SCALE, fields[5].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_DATE, fields[6].replaceAll("\"",""));

        values.put(TopoIndexDatabaseAdapter.KEY_MAP_DATUM, fields[16].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_PROJECTION, fields[17].replaceAll("\"",""));

        values.put(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH, fields[45].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST, fields[46].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH, fields[47].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST, fields[48].replaceAll("\"",""));

        values.put(TopoIndexDatabaseAdapter.KEY_MAP_URL, fields[50].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_URL1, fields[58].replaceAll("\"",""));

        values.put(TopoIndexDatabaseAdapter.KEY_MAP_SCANID, fields[53].replaceAll("\"",""));
        values.put(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID, fields[54].replaceAll("\"",""));
    }

    /**
     * Clear Maps
     */

    public boolean clearMaps(String table)
    {
        if (table.equals(TABLE_MAPS_USGS_HTMC)) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
            database.execSQL(TABLE_MAPS_USGS_HTMC_CREATE);
            return true;

        } else if (table.equals(TABLE_MAPS_USGS_USTOPO)) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
            database.execSQL(TABLE_MAPS_USGS_USTOPO_CREATE);
            return true;

        } else if (table.equals(TABLE_MAPS)) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
            database.execSQL(TABLE_MAPS_CREATE);
            return true;
        }
        return false;
    }

    /**
     * DatabaseHelper
     */
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            switch (DATABASE_VERSION)
            {
                //noinspection ConstantConditions
                case 0:
                default:
                    db.execSQL(TABLE_MAPS_CREATE);                 // local maps
                    db.execSQL(TABLE_MAPS_USGS_HTMC_CREATE);       // htmc index
                    db.execSQL(TABLE_MAPS_USGS_USTOPO_CREATE);     // ustopo index
                    break;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            /* EMPTY */
        }
    }

    /**
     * VAL_STATES
     */
    public static final HashMap<String, String> VAL_STATES = new HashMap<String, String>()
    {{
        put("AL", "Alabama");
        put("AK", "Alaska");
        put("AZ", "Arizona");
        put("AR", "Arkansas");
        put("CA", "California");
        put("CO", "Colorado");
        put("CT", "Connecticut");
        put("DE", "Delaware");
        put("FL", "Florida");
        put("GA", "Georgia");
        put("HI", "Hawaii");
        put("ID", "Idaho");
        put("IL", "Illinois");
        put("IN", "Indiana");
        put("IA", "Iowa");
        put("KS", "Kansas");
        put("KY", "Kentucky");
        put("LA", "Louisiana");
        put("ME", "Maine");
        put("MD", "Maryland");
        put("MA", "Massachusetts");
        put("MI", "Michigan");
        put("MN", "Minnesota");
        put("MS", "Mississippi");
        put("MO", "Missouri");
        put("MT", "Montana");
        put("NE", "Nebraska");
        put("NV", "Nevada");
        put("NH", "New Hampshire");
        put("NJ", "New Jersey");
        put("NM", "New Mexico");
        put("NY", "New York");
        put("NC", "North Carolina");
        put("ND", "North Dakota");
        put("OH", "Ohio");
        put("OK", "Oklahoma");
        put("OR", "Oregon");
        put("PA", "Pennsylvania");
        put("RI", "Rhode Island");
        put("SC", "South Carolina");
        put("SD", "South Dakota");
        put("TN", "Tennessee");
        put("TX", "Texas");
        put("UT", "Utah");
        put("VT", "Vermont");
        put("VA", "Virginia");
        put("WA", "Washington");
        put("WV", "West Virginia");
        put("WI", "Wisconsin");
        put("WY", "Wyoming");
        assert (size() == 50);

        put("AS", "American Samoa");
        put("DC", "District of Columbia");
        put("FM", "Federated States of Micronesia");
        put("GU", "Guam");
        put("MH", "Marshall Islands");
        put("MP", "Northern Mariana Islands");
        put("PW", "Palau");
        put("PR", "Puerto Rico");
        put("VI", "Virgin Islands");
        assert (size() == 59);
    }};

}

