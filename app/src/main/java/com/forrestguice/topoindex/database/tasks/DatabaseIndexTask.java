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

public class DatabaseIndexTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public DatabaseIndexTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        long bench_start = System.nanoTime();
        database.open();

        boolean result = true;
        if (database.clearIndices())
        {
            Log.i(TAG, "clearIndex: cleared indices");
            if (database.createIndices()) {
                Log.i(TAG, "createIndex: created indices");
            } else result = false;
        } else result = false;

        database.close();
        long bench_end = System.nanoTime();
        Log.d(TAG, "indexDB: took " + ((bench_end - bench_start) / 1000000.0 / 1000.0 / 60.0) + " min;");
        return new DatabaseTaskResult(result, 0, Calendar.getInstance().getTimeInMillis());
    }

}


