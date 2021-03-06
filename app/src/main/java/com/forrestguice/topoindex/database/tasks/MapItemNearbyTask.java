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

public class MapItemNearbyTask extends AsyncTask<String, Void, ContentValues[][]>
{
    public static final String TAG = "MapItemTask";

    private TopoIndexDatabaseAdapter database;
    private ContentValues[] centerItems;
    private TopoIndexDatabaseAdapter.MapScale mapScale;

    public MapItemNearbyTask(Context context, ContentValues[] itemStack, TopoIndexDatabaseAdapter.MapScale scale)
    {
        database = new TopoIndexDatabaseAdapter(context);
        this.centerItems = itemStack;
        this.mapScale = scale;
    }

    @Override
    protected ContentValues[][] doInBackground(String... tables)
    {
        long bench_start = System.nanoTime();

        String table = (tables.length > 0) ? tables[0] : TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC;  // TODO: support multiple tables

        database.open();
        ContentValues[][] nearbyMaps = database.findNearbyMaps(table, centerItems, mapScale);
        database.close();

        long bench_end = System.nanoTime();
        Log.d(TAG, "nearby (benchmark): " + ((double)(bench_end - bench_start) / 1E9) + " .. " + tables.length + " tables.");

        return nearbyMaps;
    }

    @Override
    protected void onPreExecute()
    {
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected void onPostExecute(ContentValues[][] maps)
    {
        if (taskListener != null) {
            taskListener.onFinished(maps, -1);
        }
    }

    private MapItemNearbyTaskListener taskListener = null;
    public void setTaskListener( MapItemNearbyTaskListener listener ) {
        taskListener = listener;
    }

    public static abstract class MapItemNearbyTaskListener
    {
        public void onStarted() {}
        public void onFinished(ContentValues[][] mapList, int selectedPos) {}
    }
}
