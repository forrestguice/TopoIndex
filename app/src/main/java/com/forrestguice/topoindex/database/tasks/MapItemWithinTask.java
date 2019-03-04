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

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

public class MapItemWithinTask extends AsyncTask<String, Void, ContentValues[]>
{
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
        String table = (tables.length > 0) ? tables[0] : TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC;  // TODO: support multiple tables

        database.open();
        ContentValues[] contentValues = database.findMapsWithin(table, mapScale, item);
        ContentValues[] collectedValues =  database.findInCollection(contentValues);
        database.close();

        selectedPos = TopoIndexDatabaseAdapter.findMapInList(contentValues, item);
        return collectedValues;
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
