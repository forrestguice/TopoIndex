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
import android.os.Environment;
import android.util.Log;

import com.forrestguice.topoindex.AppSettings;

import java.io.File;
import java.util.Calendar;

public class DatabaseScanTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public DatabaseScanTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        int count = 0;
        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
        {
            Context context = contextRef.get();
            if (context != null)
            {
                String collectionPath = AppSettings.getCollectionPath(context);
                File dataDirectory = Environment.getExternalStorageDirectory();
                File mapDirectory = new File(dataDirectory + File.separator + collectionPath);

                if (!mapDirectory.exists()) {
                    if (!mapDirectory.mkdirs())
                    {
                        Log.e(TAG, "scanCollection: failed to create path... " + mapDirectory.getAbsolutePath());
                        return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
                    }
                }

                Log.d("DEBUG", "dataDir: " + mapDirectory.getAbsolutePath());

                // TODO: iterate over files, add to database
                count++;

            } else {
                Log.e(TAG, "Context is null!");
                return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
            }
        } else {
            Log.e(TAG, "External storage is unavailable!");
            return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
        }

        return new DatabaseTaskResult(true, count, Calendar.getInstance().getTimeInMillis());
    }
}
