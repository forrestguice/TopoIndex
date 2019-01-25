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
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

public class TopoIndexDatabaseAdapter
{
    private static final String DATABASE_NAME = "topoindex";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_MAP_SERIES = "series";
    public static final String DEF_MAP_SERIES = KEY_MAP_SERIES + " text not null";
    public static final String VAL_MAP_SERIES_HTMC = "HTMC";
    public static final String VAL_MAP_SERIES_USTOPO = "US Topo";

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
    public static final String DEF_MAP_LATITUDE_NORTH = KEY_MAP_LATITUDE_NORTH + " numeric";

    public static final String KEY_MAP_LONGITUDE_WEST = "wlon";
    public static final String DEF_MAP_LONGITUDE_WEST = KEY_MAP_LONGITUDE_WEST + " numeric";

    public static final String KEY_MAP_LATITUDE_SOUTH = "slat";
    public static final String DEF_MAP_LATITUDE_SOUTH = KEY_MAP_LATITUDE_SOUTH + " numeric";

    public static final String KEY_MAP_LONGITUDE_EAST = "elon";
    public static final String DEF_MAP_LONGITUDE_EAST = KEY_MAP_LONGITUDE_EAST + " numeric";

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

    public static final String KEY_MAP_ISCOLLECTED = "iscollected";

    private static final String[] QUERY_MAPS_MINENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_GDAITEMID, KEY_MAP_CELLID, KEY_MAP_SCANID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE, KEY_MAP_SCALE, KEY_MAP_LATITUDE_NORTH, KEY_MAP_LONGITUDE_WEST, KEY_MAP_LATITUDE_SOUTH, KEY_MAP_LONGITUDE_EAST, KEY_MAP_URL, KEY_MAP_URL1, KEY_MAP_URL2};
    private static final String[] QUERY_MAPS_FULLENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_GDAITEMID, KEY_MAP_CELLID, KEY_MAP_SCANID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE, KEY_MAP_SCALE, KEY_MAP_DATUM, KEY_MAP_PROJECTION, KEY_MAP_LATITUDE_NORTH, KEY_MAP_LONGITUDE_WEST, KEY_MAP_LATITUDE_SOUTH, KEY_MAP_LONGITUDE_EAST, KEY_MAP_URL, KEY_MAP_URL1, KEY_MAP_URL2};

    private static final String CREATE_COLS_DEFAULT = DEF_ROWID + ", "
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

    /**
     * USGS HTMC (Historical Topo Collection)
     */
    public static final String TABLE_MAPS_HTMC = "usgs_htmc";
    private static final String TABLE_MAPS_HTMC_CREATE_COLS = CREATE_COLS_DEFAULT;
    private static final String TABLE_MAPS_HTMC_CREATE = "create table " + TABLE_MAPS_HTMC + " (" + TABLE_MAPS_HTMC_CREATE_COLS + ");";

    public static final String INDEX_MAPS_HTMC = "usgs_htmc_index";
    private static final String[] INDEX_MAPS_HTMC_CREATE = new String[] {
            "create index " + INDEX_MAPS_HTMC + "0" + " on " + TABLE_MAPS_HTMC + " (" + KEY_MAP_LONGITUDE_EAST + ");",
            "create index " + INDEX_MAPS_HTMC + "1" + " on " + TABLE_MAPS_HTMC + " (" + KEY_MAP_LONGITUDE_WEST + ");",
            "create index " + INDEX_MAPS_HTMC + "2" + " on " + TABLE_MAPS_HTMC + " (" + KEY_MAP_LATITUDE_SOUTH + ");",
            "create index " + INDEX_MAPS_HTMC + "3" + " on " + TABLE_MAPS_HTMC + " (" + KEY_MAP_LATITUDE_NORTH + ");"
    };

    /**
     * USGS US Topo (Current quadrangles; 2010 and later)
     */
    public static final String TABLE_MAPS_USTOPO = "usgs_ustopo";
    private static final String TABLE_MAPS_USTOPO_CREATE_COLS = CREATE_COLS_DEFAULT;
    private static final String TABLE_MAPS_USTOPO_CREATE = "create table " + TABLE_MAPS_USTOPO + " (" + TABLE_MAPS_USTOPO_CREATE_COLS + ");";

    public static final String INDEX_MAPS_USTOPO = "usgs_ustopo_index";
    private static final String[] INDEX_MAPS_USTOPO_CREATE = new String[] {
            "create index " + INDEX_MAPS_USTOPO + "0" + " on " + TABLE_MAPS_USTOPO + " (" + KEY_MAP_LONGITUDE_EAST + ");",
            "create index " + INDEX_MAPS_USTOPO + "1" + " on " + TABLE_MAPS_USTOPO + " (" + KEY_MAP_LONGITUDE_WEST + ");",
            "create index " + INDEX_MAPS_USTOPO + "2" + " on " + TABLE_MAPS_USTOPO + " (" + KEY_MAP_LATITUDE_SOUTH + ");",
            "create index " + INDEX_MAPS_USTOPO + "3" + " on " + TABLE_MAPS_USTOPO + " (" + KEY_MAP_LATITUDE_NORTH + ");"
    };

    /**
     * Local Topo (files on the local drive)
     */
    public static final String TABLE_MAPS = "maps";
    private static final String TABLE_MAPS_CREATE_COLS = CREATE_COLS_DEFAULT;
    private static final String TABLE_MAPS_CREATE = "create table " + TABLE_MAPS + " (" + TABLE_MAPS_CREATE_COLS + ");";

    public static final String INDEX_MAPS = "maps_index";
    private static final String[] INDEX_MAPS_CREATE = new String[] {
            "create index " + INDEX_MAPS + "0" + " on " + TABLE_MAPS + " (" + KEY_MAP_LONGITUDE_EAST + ");",
            "create index " + INDEX_MAPS + "1" + " on " + TABLE_MAPS + " (" + KEY_MAP_LONGITUDE_WEST + ");",
            "create index " + INDEX_MAPS + "2" + " on " + TABLE_MAPS + " (" + KEY_MAP_LATITUDE_SOUTH + ");",
            "create index " + INDEX_MAPS + "3" + " on " + TABLE_MAPS + " (" + KEY_MAP_LATITUDE_NORTH + ");"
    };

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

    public Cursor getMaps(@NonNull String table, int n, boolean fullEntry, FilterValues filter)
    {
        String[] query = (fullEntry) ? QUERY_MAPS_FULLENTRY : QUERY_MAPS_MINENTRY;
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList<>();
        String groupBy = null;
        boolean firstFilter = true;

        String nameFilter = filter.getNameFilter();
        if (nameFilter != null && !nameFilter.isEmpty())
        {
            selection.append(KEY_MAP_NAME + " LIKE ?");
            selectionArgs.add("%" + nameFilter + "%");
            firstFilter = false;
        }

        String[] stateFilters = filter.getStatesFilter();
        if (stateFilters != null && stateFilters.length > 0)
        {
            if (!firstFilter) {
                selection.append(" AND ");
            }

            for (int i=0; i<stateFilters.length; i++)
            {
                selection.append(KEY_MAP_STATE + " = ?");
                selectionArgs.add(stateFilters[i]);

                if (i != stateFilters.length - 1) {
                    selection.append(" OR ");
                }
            }
            firstFilter = false;
        }

        String scaleFilter = filter.getScaleFilter();
        if (scaleFilter != null && !scaleFilter.isEmpty())
        {
            if (!firstFilter) {
                selection.append(" AND ");
            }

            selection.append(KEY_MAP_SCALE + " = ?");
            selectionArgs.add(scaleFilter);
            firstFilter = false;
        }

        /**String selectionArgsDebug = "";
        String[] selectionArgsArray = selectionArgs.toArray(new String[0]);
        for (int i=0; i<selectionArgsArray.length; i++) {
            selectionArgsDebug += selectionArgsArray[i] + ", ";
        }
        Log.d("DEBUG", "selection: " + selection.toString() + " :: args: " + selectionArgsDebug);*/

        Cursor cursor =  (n > 0) ? database.query( table, query, selection.toString(), selectionArgs.toArray(new String[0]), groupBy, null, "_id ASC", n+"" )
                                 : database.query( table, query, selection.toString(), selectionArgs.toArray(new String[0]), groupBy, null, "_id ASC" );
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getMaps_HTMC(int n, boolean fullEntry)
    {
        return getMaps(TABLE_MAPS_HTMC, n, fullEntry);
    }

    public Cursor getMaps_USTopo(int n, boolean fullEntry)
    {
        return getMaps(TABLE_MAPS_USTOPO, n, fullEntry);
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

    public ContentValues[] findNearbyMaps(ContentValues values, MapScale mapScale)
    {
        double[] corners = TopoIndexDatabaseAdapter.getCorners(values);         // bounding box: n, w, e, s
        double northLat = corners[0];
        double westLon = corners[1];
        double southLat = corners[2];
        double eastLon = corners[3];

        String table = TABLE_MAPS_HTMC;
        String[] query = QUERY_MAPS_FULLENTRY;
        ContentValues[] contentValues = new ContentValues[9];

        String selection = mapScale == null ? "" : KEY_MAP_SCALE + " = " + mapScale.getValue() + " AND ";

        String selection0 = selection + KEY_MAP_LATITUDE_SOUTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_EAST + " = ?";
        String[] selectionArgs0 = new String[] { Double.toString(northLat), Double.toString(westLon) };
        Cursor cursor0 = database.query( table, query, selection0, selectionArgs0, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_NORTHWEST, cursor0);

        String selection1 = selection + KEY_MAP_LATITUDE_SOUTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_WEST + " = ?";
        String[] selectionArgs1 = new String[] { Double.toString(northLat), Double.toString(westLon) };
        Cursor cursor1 = database.query( table, query, selection1, selectionArgs1, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_NORTH, cursor1);

        String selection2 = selection + KEY_MAP_LATITUDE_SOUTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_WEST + " = ?";
        String[] selectionArgs2 = new String[] { Double.toString(northLat), Double.toString(eastLon) };
        Cursor cursor2 = database.query( table, query, selection2, selectionArgs2, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_NORTHEAST, cursor2);

        String selection3 = selection + KEY_MAP_LATITUDE_NORTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_EAST + " = ?";
        String[] selectionArgs3 = new String[] { Double.toString(northLat), Double.toString(westLon) };
        Cursor cursor3 = database.query( table, query, selection3, selectionArgs3, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_WEST, cursor3);

        contentValues[GRID_CENTER] = values;

        String selection5 = selection + KEY_MAP_LATITUDE_NORTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_WEST + " = ?";
        String[] selectionArgs5 = new String[] { Double.toString(northLat), Double.toString(eastLon) };
        Cursor cursor5 = database.query( table, query, selection5, selectionArgs5, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_EAST, cursor5);

        String selection6 = selection + KEY_MAP_LATITUDE_NORTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_EAST + " = ?";
        String[] selectionArgs6 = new String[] { Double.toString(southLat), Double.toString(westLon) };
        Cursor cursor6 = database.query( table, query, selection6, selectionArgs6, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_SOUTHWEST, cursor6);

        String selection7 = selection + KEY_MAP_LATITUDE_NORTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_WEST + " = ?";
        String[] selectionArgs7 = new String[] { Double.toString(southLat), Double.toString(westLon) };
        Cursor cursor7 = database.query( table, query, selection7, selectionArgs7, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_SOUTH, cursor7);

        String selection8 = selection + KEY_MAP_LATITUDE_NORTH + " = ?" + " AND " + KEY_MAP_LONGITUDE_WEST + " = ?";
        String[] selectionArgs8 = new String[] { Double.toString(southLat), Double.toString(eastLon) };
        Cursor cursor8 = database.query( table, query, selection8, selectionArgs8, null, null, "_id DESC" );
        assignGridValue(contentValues, GRID_SOUTHEAST, cursor8);

        return contentValues;
    }

    private void assignGridValue(ContentValues[] contentValues, int gridPos, Cursor cursor)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            cursor.moveToFirst();
            ContentValues gridValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, gridValues);
            cursor.close();
            contentValues[gridPos] = gridValues;
        } else contentValues[gridPos] = null;
    }

    public boolean hasMaps(String table)
    {
        Cursor cursor = database.rawQuery("SELECT * FROM " + table + " LIMIT 1", null);
        boolean retValue = (cursor != null && cursor.getCount() == 1);
        if (cursor != null) {
            cursor.close();
        }
        return retValue;
    }

    public ContentValues findInCollection(ContentValues contentValues)
    {
        String mapSeries = contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SERIES);
        if (mapSeries == null) {
            mapSeries = VAL_MAP_SERIES_HTMC;
        }

        Cursor cursor;
        if (mapSeries.equals(VAL_MAP_SERIES_USTOPO))
        {
            //noinspection UnnecessaryLocalVariable
            String gdaItemID = contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID);
            cursor = getMap_USTopo(TABLE_MAPS, gdaItemID, false);

        } else {
            //noinspection UnnecessaryLocalVariable
            String scanID = contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCANID);
            cursor = getMap_HTMC(TABLE_MAPS, scanID, false);
        }

        ContentValues retValue = null;
        if (cursor != null) {
            if (cursor.getCount() >= 1) {
                retValue = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, retValue);
                retValue.put(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED, true);
            }
            cursor.close();
            if (retValue != null) {
                return retValue;
            }
        }
        contentValues.put(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED, false);
        return contentValues;
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
        return addMaps(TABLE_MAPS_HTMC, values);
    }

    public long addMaps_USTopo(ContentValues... values)
    {
        return addMaps(TABLE_MAPS_USTOPO, values);
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

    public static double[] getCorners(ContentValues values)
    {
        double[] corners = new double[4];
        corners[0] = values.getAsDouble(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH);
        corners[1] = values.getAsDouble(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST);
        corners[2] = values.getAsDouble(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH);
        corners[3] = values.getAsDouble(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST);
        return corners;
    }

    public static String[] getUrls(ContentValues values)
    {
        return new String[] { values.getAsString(KEY_MAP_URL), values.getAsString(KEY_MAP_URL1), values.getAsString(KEY_MAP_URL2) };
    }

    /**
     * Clear Maps
     */

    public boolean clearMaps(String table)
    {
        if (table.equals(TABLE_MAPS_HTMC)) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
            database.execSQL(TABLE_MAPS_HTMC_CREATE);
            return true;

        } else if (table.equals(TABLE_MAPS_USTOPO)) {
            database.execSQL("DROP TABLE IF EXISTS " + table);
            database.execSQL(TABLE_MAPS_USTOPO_CREATE);
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
                    db.execSQL(TABLE_MAPS_CREATE);
                    for (String createIndexStatement : INDEX_MAPS_CREATE) {
                        db.execSQL(createIndexStatement);
                    }

                    db.execSQL(TABLE_MAPS_USTOPO_CREATE);
                    for (String createIndexStatement : INDEX_MAPS_USTOPO_CREATE) {
                        db.execSQL(createIndexStatement);
                    }

                    db.execSQL(TABLE_MAPS_HTMC_CREATE);
                    for (String createIndexStatement : INDEX_MAPS_HTMC_CREATE) {
                        db.execSQL(createIndexStatement);
                    }
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
     * FilterValues
     */
    public static final class FilterValues
    {
        protected String filter_name;
        protected String[] filter_states;
        protected String filter_scale;

        public FilterValues(String nameFilter, String[] statesFilter, String scaleFilter)
        {
            filter_name = nameFilter;
            filter_states = statesFilter;
            filter_scale = scaleFilter;
        }

        public String getNameFilter()
        {
            return filter_name;
        }

        public String[] getStatesFilter()
        {
            return filter_states;
        }

        public String getScaleFilter()
        {
            return filter_scale;
        }

        public String toString()
        {
            StringBuilder statesFilter = new StringBuilder();
            for (String state : filter_states) {
                statesFilter.append(state);
                statesFilter.append(" ");
            }
            return filter_name + " :: " + statesFilter.toString().trim() + " :: " + filter_scale;
        }
    }

    /**
     * MapScale
     */
    public enum MapScale
    {
        SCALE_ANY("Any", ""),                   // TODO: i18n
        SCALE_250K("250K", "250000"),
        SCALE_100K("100K", "100000"),
        SCALE_63K("63K", "63000"),
        SCALE_48K("48K", "48000"),
        SCALE_24K("24K", "24000");

        MapScale(String displayString, String value) {
            this.displayString = displayString;
            this.value = value;
        }

        private String value;
        public String getValue()
        {
            return value;
        }

        public static MapScale findValue(String value)
        {
            for (MapScale scale : MapScale.values()) {
                if (scale.value.equals(value)) {
                    return scale;
                }
            }
            return SCALE_ANY;
        }

        private String displayString;
        public String toString() {
            return displayString;
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

    public static final int GRID_NORTHWEST = 0;
    public static final int GRID_NORTH = 1;
    public static final int GRID_NORTHEAST = 2;
    public static final int GRID_WEST = 3;
    public static final int GRID_CENTER = 4;
    public static final int GRID_EAST = 5;
    public static final int GRID_SOUTHWEST = 6;
    public static final int GRID_SOUTH = 7;
    public static final int GRID_SOUTHEAST = 8;

}

