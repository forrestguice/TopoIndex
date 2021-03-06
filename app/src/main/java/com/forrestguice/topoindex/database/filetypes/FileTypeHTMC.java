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

package com.forrestguice.topoindex.database.filetypes;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;
import com.forrestguice.topoindex.database.tasks.DatabaseScanTask;

import java.io.File;

public class FileTypeHTMC implements TopoIndexFileType
{
    public static final String TAG = "TopoIndexFileHTMC";

    public void scanFile(File file, TopoIndexDatabaseAdapter database, DatabaseScanTask.ScanResult result)
    {
        ContentValues fileValues = contentValuesFromName(file);
        if (fileValues != null)
        {
            Log.d(TAG, "scanFile: HTMC: " + file.getAbsolutePath());
            fileValues = updateValuesFromDB(database, fileValues);

            TopoIndexDatabaseAdapter.setBoolean(fileValues, TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED, true);

            String scanID = fileValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCANID);
            if (database.hasMap_HTMC(TopoIndexDatabaseAdapter.TABLE_MAPS, scanID))
            {
                Log.d(TAG, "scanFile: updating " + file.getAbsolutePath() + " .. " + fileValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH));
                database.updateMaps_HTMC(TopoIndexDatabaseAdapter.TABLE_MAPS, fileValues);

            } else {
                Log.d(TAG, "scanFile: adding " + file.getAbsolutePath());
                database.addMaps(TopoIndexDatabaseAdapter.TABLE_MAPS, fileValues);
            }
            result.count++;

        } else Log.w(TAG, "scanFile: missing values; skipping");
    }

    @Override
    public ContentValues contentValuesFromName(File file )
    {
        String fileName = file.getName();
        String[] parts = fileName.split("_");
        if (parts.length == 6)
        {
            ContentValues values = new ContentValues();
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_STATE, parts[0]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_NAME, parts[1]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SCANID, parts[2]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_DATE, parts[3]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SCALE, parts[4]);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SERIES, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_VERSION, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_URL, file.getAbsolutePath());
            return values;

        } else {
            Log.w(TAG, "scanFile: unrecognized filename " + fileName);
            return null;
        }
    }

    public ContentValues updateValuesFromDB(TopoIndexDatabaseAdapter database, ContentValues values)
    {
        if (values != null)
        {
            String series = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SERIES);
            if (series != null && !series.isEmpty())
            {
                String scanID = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCANID);
                if (scanID != null && !scanID.isEmpty())
                {
                    Cursor cursor = database.getMap_HTMC(TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC, scanID, TopoIndexDatabaseAdapter.QUERY_MAPS_FULLENTRY_HTMC);
                    TopoIndexDatabaseAdapter.updateValuesFromDB(values, cursor, scanID);

                } else Log.w(TAG, "updateValuesFromDB: missing Scan ID; skipping");
            } else Log.w(TAG, "updateValuesFromDB: missing series; skipping ");
        }  else Log.w(TAG, "updateValuesFromDB: missing values; skipping");

        return values;
    }
}