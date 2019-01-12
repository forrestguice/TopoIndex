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
    along with SuntimesWidget.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.topoindex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TopoIndexDatabaseAdapter
{
    private static final String DATABASE_NAME = "topoindex";
    private static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String DEF_ROWID = KEY_ROWID + " integer primary key autoincrement";

    public static final String KEY_MAP_SERIES = "series";
    public static final String DEF_MAP_SERIES = KEY_MAP_SERIES + " text not null";

    public static final String KEY_MAP_VERSION = "version";
    public static final String DEF_MAP_VERSION = KEY_MAP_VERSION + " text not null";

    public static final String KEY_MAP_CELLID = "cellid";
    public static final String DEF_MAP_CELLID = KEY_MAP_CELLID + " text not null";

    public static final String KEY_MAP_NAME = "name";
    public static final String DEF_MAP_NAME = KEY_MAP_NAME + " text not null";

    public static final String KEY_MAP_STATE = "state";
    public static final String DEF_MAP_STATE = KEY_MAP_STATE + " text not null";

    public static final String KEY_MAP_DATE = "mapdate";
    public static final String DEF_MAP_DATE = KEY_MAP_STATE + " text not null";

    public static final String KEY_MAP_LATITUDE_NORTH = "nlat";
    public static final String DEF_MAP_LATITUDE_NORTH = KEY_MAP_LATITUDE_NORTH + " text not null";

    public static final String KEY_MAP_LONGITUDE_WEST = "wlon";
    public static final String DEF_MAP_LONGITUDE_WEST = KEY_MAP_LONGITUDE_WEST + " text not null";

    public static final String KEY_MAP_LATITUDE_SOUTH = "slat";
    public static final String DEF_MAP_LATITUDE_SOUTH = KEY_MAP_LATITUDE_SOUTH + " text not null";

    public static final String KEY_MAP_LONGITUDE_EAST = "elon";
    public static final String DEF_MAP_LONGITUDE_EAST = KEY_MAP_LONGITUDE_EAST + " text not null";

    public static final String KEY_MAP_SCALE = "scale";
    public static final String DEF_MAP_SCALE = KEY_MAP_SCALE + " text not null";

    public static final String KEY_MAP_DATUM = "datum";
    public static final String DEF_MAP_DATUM = KEY_MAP_DATUM + " text not null";

    public static final String KEY_MAP_PROJECTION = "projection";
    public static final String DEF_MAP_PROJECTION = KEY_MAP_PROJECTION + " text not null";

    private static final String TABLE_MAPS = "maps";
    private static final String TABLE_MAPS_CREATE_COLS = DEF_ROWID + ", "
            + DEF_MAP_SERIES + ", "
            + DEF_MAP_VERSION + ", "
            + DEF_MAP_CELLID + ", "
            + DEF_MAP_NAME + ", "
            + DEF_MAP_STATE + ", "
            + DEF_MAP_SCALE + ", "
            + DEF_MAP_DATE + ", "
            + DEF_MAP_DATUM + ", "
            + DEF_MAP_PROJECTION + ", "
            + DEF_MAP_LATITUDE_NORTH + ", "
            + DEF_MAP_LONGITUDE_WEST + ", "
            + DEF_MAP_LATITUDE_SOUTH + ", "
            + DEF_MAP_LONGITUDE_EAST;
    private static final String TABLE_MAPS_CREATE = "create table " + TABLE_MAPS + " (" + TABLE_MAPS_CREATE_COLS + ");";

    private static final String[] QUERY_MAPS_MINENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_CELLID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE};
    private static final String[] QUERY_MAPS_FULLENTRY = new String[] {KEY_ROWID, KEY_MAP_SERIES, KEY_MAP_VERSION, KEY_MAP_CELLID, KEY_MAP_NAME, KEY_MAP_DATE, KEY_MAP_STATE, KEY_MAP_SCALE, KEY_MAP_DATUM, KEY_MAP_PROJECTION, KEY_MAP_LATITUDE_NORTH, KEY_MAP_LONGITUDE_WEST, KEY_MAP_LATITUDE_SOUTH, KEY_MAP_LONGITUDE_EAST};

    /**
     *
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
        if (databaseHelper != null)
        {
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

    public Cursor getAllQuads(int n, boolean fullEntry)
    {
        String[] QUERY = (fullEntry) ? QUERY_MAPS_FULLENTRY : QUERY_MAPS_MINENTRY;
        Cursor cursor =  (n > 0) ? database.query( TABLE_MAPS, QUERY, null, null, null, null, "_id DESC", n+"" )
                                 : database.query( TABLE_MAPS, QUERY, null, null, null, null, "_id DESC" );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getQuad(long row) throws SQLException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] QUERY = QUERY_MAPS_FULLENTRY;
        Cursor cursor = database.query( true, TABLE_MAPS, QUERY,
                KEY_ROWID + "=" + row, null,
                null, null, null, null );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     *
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
                    break;
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            /* EMPTY */
        }
    }
}

