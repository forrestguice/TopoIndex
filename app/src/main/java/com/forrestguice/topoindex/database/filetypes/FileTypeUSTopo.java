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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileTypeUSTopo implements TopoIndexFileType
{
    public static final String TAG = "TopoIndexFileUSTopo";

    public void scanFile(File file, TopoIndexDatabaseAdapter database, DatabaseScanTask.ScanResult result)
    {
        ContentValues fileValues = contentValuesFromName(file);
        if (fileValues != null)
        {
            Log.d(TAG, "scanFile: USTopo: " + file.getAbsolutePath());
            fileValues = updateValuesFromDB(database, fileValues);

            TopoIndexDatabaseAdapter.setBoolean(fileValues, TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED, true);

            String itemID = fileValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID);
            if (database.hasMap_USTopo(TopoIndexDatabaseAdapter.TABLE_MAPS, itemID))
            {
                Log.d(TAG, "scanFile: updating " + file.getAbsolutePath() + " .. " + fileValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH));
                database.updateMaps_USTopo(TopoIndexDatabaseAdapter.TABLE_MAPS, fileValues);

            } else {
                Log.d(TAG, "scanFile: adding " + file.getAbsolutePath());
                database.addMaps(TopoIndexDatabaseAdapter.TABLE_MAPS, fileValues);
            }
            result.count++;

        } else Log.w(TAG, "scanFile: missing values; skipping");
    }

    @Override
    public ContentValues contentValuesFromName(File file)
    {
        String fileName = file.getName();
        String[] parts = fileName.split("_");

        if (parts.length >= 5)
        {
            ArrayList<String> nameParts = new ArrayList<>(Arrays.asList(parts));
            nameParts.remove(nameParts.size() - 1);       // discard _[fileExt]
            nameParts.remove(nameParts.size() - 1);       // discard _TM
            nameParts.remove(nameParts.size() - 1);       // discard _[GDAItemID]
            nameParts.remove(0);                          // discard _[STATE]
            StringBuilder mapName = new StringBuilder();
            for (int i=0; i<nameParts.size(); i++) {
                mapName.append(nameParts.get(i));
                mapName.append(" ");
            }

            List<String> reverseParts = Arrays.asList(parts);
            Collections.reverse(reverseParts);

            ContentValues values = new ContentValues();
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_STATE, reverseParts.get(reverseParts.size()-1));
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_NAME, mapName.toString().trim());
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_DATE, reverseParts.get(2).substring(0, 4));
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_SERIES, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_USTOPO);
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_VERSION, TopoIndexDatabaseAdapter.VAL_MAP_SERIES_USTOPO);        // TODO: is this value more or less the same as series?
            values.put(TopoIndexDatabaseAdapter.KEY_MAP_URL, file.getAbsolutePath());
            return values;

        } else {
            Log.w(TAG, "scanFile: unrecognized USTopo filename " + fileName);
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
                String gdaItemID = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID);
                if (gdaItemID != null && !gdaItemID.isEmpty())
                {
                    Cursor cursor = database.getMap_USTopo(TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO, gdaItemID, TopoIndexDatabaseAdapter.QUERY_MAPS_FULLENTRY_USTOPO);
                    TopoIndexDatabaseAdapter.updateValuesFromDB(values, cursor, gdaItemID);

                } else {
                    Log.w(TAG, "updateValuesFromDB: missing GDA Item ID; trying name + date instead...");
                    String mapName = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME);
                    String mapYear = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_DATE);
                    if (mapName != null && !mapName.isEmpty() && mapYear != null && !mapYear.isEmpty())
                    {
                        Cursor cursor = database.getMap(TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO, mapName, mapYear, TopoIndexDatabaseAdapter.QUERY_MAPS_FULLENTRY_USTOPO);
                        TopoIndexDatabaseAdapter.updateValuesFromDB(values, cursor, mapName);

                    } else Log.w(TAG, "updateValuesFromDB: missing name + date; skipping...");
                }
            } else Log.w(TAG, "updateValuesFromDB: missing series; skipping ");
        }  else Log.w(TAG, "updateValuesFromDB: missing values; skipping");

        return values;
    }

}

