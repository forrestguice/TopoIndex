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

package com.forrestguice.topoindex.database.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapItemWithinTask extends AsyncTask<String, Void, ContentValues[]>
{
    public static final String TAG = "MapItemTask";

    private ContentValues item;
    private int selectedPos;
    private TopoIndexDatabaseAdapter database;
    private TopoIndexDatabaseAdapter.MapScale mapScale;

    public MapItemWithinTask(Context context, ContentValues mapItem, TopoIndexDatabaseAdapter.MapScale mapScale)
    {
        this.item = mapItem;
        this.mapScale = mapScale;
        database = new TopoIndexDatabaseAdapter(context);
    }

    @Override
    protected ContentValues[] doInBackground(String... tables)
    {
        long bench_start = System.nanoTime();

        if (tables.length == 0 || tables[0] == null) {
            tables = new String[] { TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC };
            Log.w(TAG, "Within: Missing parameter table(s), falling back to HTMC.");
        }

        List<ContentValues> mapList = new ArrayList<>();
        database.open();
        for (int i=0; i<tables.length; i++)
        {
            if (tables[i] != null)
            {
                ContentValues[] contentValues = database.findMapsWithin(tables[i], mapScale, item);
                database.findInCollection(contentValues);
                mapList.addAll(Arrays.asList(contentValues));
            }
        }

        database.close();

        ContentValues[] mapArray = mapList.toArray(new ContentValues[0]);
        selectedPos = TopoIndexDatabaseAdapter.findMapInList(mapArray, item);

        long bench_end = System.nanoTime();
        Log.d(TAG, "within (benchmark): " + ((double)(bench_end - bench_start) / 1E9) + " .. " + tables.length + " tables.");

        return mapArray;
    }

    @Override
    protected void onPreExecute()
    {
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected void onPostExecute(ContentValues[] maps)
    {
        if (taskListener != null) {
            taskListener.onFinished(maps, selectedPos);
        }
    }

    private MapItemTaskListener taskListener = null;
    public void setTaskListener( MapItemTaskListener listener ) {
        taskListener = listener;
    }

}
