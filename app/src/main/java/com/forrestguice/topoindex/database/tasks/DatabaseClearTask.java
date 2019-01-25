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

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;

public class DatabaseClearTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public DatabaseClearTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        boolean result = true;
        int count = 0;

        database.open();
        for (Uri uri : uris)
        {
            String table = uri.getLastPathSegment();
            if (database.clearMaps(table))
            {
                Log.i(TAG, "clearTable: cleared " + table);
                count++;
            } else result = false;
        }
        database.close();

        return new DatabaseTaskResult(result, count, Calendar.getInstance().getTimeInMillis());
    }

}
