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

    public static final String KEY_QUAD_NAME = "name";
    public static final String DEF_QUAD_NAME = KEY_QUAD_NAME + " text not null";

    private static final String TABLE_QUADS = "quads";
    private static final String TABLE_QUADS_CREATE_COLS = DEF_ROWID + ", "
            + DEF_QUAD_NAME;
    private static final String TABLE_QUADS_CREATE = "create table " + TABLE_QUADS + " (" + TABLE_QUADS_CREATE_COLS + ");";

    private static final String[] QUERY_QUADS_MINENTRY = new String[] {KEY_ROWID, KEY_QUAD_NAME};
    private static final String[] QUERY_QUADS_FULLENTRY = new String[] {KEY_ROWID, KEY_QUAD_NAME};

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
        String[] QUERY = (fullEntry) ? QUERY_QUADS_FULLENTRY : QUERY_QUADS_MINENTRY;
        Cursor cursor =  (n > 0) ? database.query( TABLE_QUADS, QUERY, null, null, null, null, "_id DESC", n+"" )
                                 : database.query( TABLE_QUADS, QUERY, null, null, null, null, "_id DESC" );
        if (cursor != null)
        {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getQuad(long row) throws SQLException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] QUERY = QUERY_QUADS_FULLENTRY;
        Cursor cursor = database.query( true, TABLE_QUADS, QUERY,
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
                    db.execSQL(TABLE_QUADS_CREATE);
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

