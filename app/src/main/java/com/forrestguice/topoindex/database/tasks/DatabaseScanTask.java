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
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.io.File;
import java.util.Calendar;

public class DatabaseScanTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public static final String EXT_GEOPDF = "geo.pdf";

    public DatabaseScanTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        ScanResult result = new ScanResult();

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
                        Log.e(TAG, "scan: failed to create path... " + mapDirectory.getAbsolutePath());
                        return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
                    } else Log.i(TAG, "scan: created path... " + mapDirectory.getAbsolutePath());
                } // else Log.d("DEBUG", "dataDir: " + mapDirectory.getAbsolutePath());

                database.open();
                scanDirectory(mapDirectory, result);
                database.close();

            } else {
                Log.e(TAG, "scan: Context is null!");
                return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
            }
        } else {
            Log.e(TAG, "scan: External storage is unavailable!");
            return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
        }

        return new DatabaseTaskResult(true, result.count, Calendar.getInstance().getTimeInMillis());
    }

    protected void scanDirectory(File dir, ScanResult result)
    {
        if (dir.exists())
        {
            Log.d(TAG, "scanDirectory: " + dir.getAbsolutePath());
            for (File file : dir.listFiles())
            {
                if (file.isDirectory())
                    scanDirectory(file, result);
                else scanFile(file, result);
            }
        }
    }

    protected void scanFile(File file, ScanResult result)
    {
        if (file.exists())
        {
            String fileName = file.getName();
            if (fileName.toLowerCase().endsWith(EXT_GEOPDF))
            {
                ContentValues values = getValuesFromFileName(file);
                if (values != null)
                {
                    Log.d(TAG, "scanFile: " + file.getAbsolutePath());
                    database.addMaps(TopoIndexDatabaseAdapter.TABLE_MAPS, values);
                    result.count++;
                }

            } else {
                Log.d(TAG, "scanFile: ignoring " + file.getAbsolutePath());
            }
        }
    }

    protected static class ScanResult
    {
        int count = 0;
    }

    protected ContentValues getValuesFromFileName( File file )
    {
        String fileName = file.getName();
        String[] parts = fileName.split("_");
        if (parts.length == 6)
        {
            ContentValues values = new ContentValues();
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_STATE, parts[0]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_NAME, parts[1]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_CELLID, parts[2]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_DATE, parts[3]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SCALE, parts[4]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SERIES, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC);       // TODO: how to determine this from the filename... possible?
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_VERSION, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC);        // TODO: is this value more or less the same as series?
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_URL, file.getAbsolutePath());
            return values;

        } else {
            Log.w(TAG, "scanFile: unrecognized filename " + fileName);
            return null;
        }
    }
}
