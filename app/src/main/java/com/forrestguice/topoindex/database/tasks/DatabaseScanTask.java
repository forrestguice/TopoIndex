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
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class DatabaseScanTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public static final String EXT_GEOPDF = "geo.pdf";
    public static final String EXT_GEOPDF_TM = "tm_geo.pdf";

    public static final String EXT_GEOJPG = "geo_jpg.zip";
    public static final String EXT_GEOJPG_TM = "tm_geo_jpg.zip";

    public static final String EXT_GEOTIFF = "geo_tiff.zip";
    public static final String EXT_GEOTIFF_TM = "tm_geo_tiff.zip";

    public static final String EXT_GEOKMZ = "geo_kmz.zip";
    public static final String EXT_GEOKMZ_TM = "tm_geo_kmz.zip";

    public static final String[] HTCM_EXTS = new String[] { EXT_GEOPDF, EXT_GEOTIFF, EXT_GEOJPG, EXT_GEOKMZ };
    public static final String[] USTOPO_EXTS = new String[] { EXT_GEOPDF_TM, EXT_GEOTIFF_TM, EXT_GEOJPG_TM, EXT_GEOKMZ_TM };

    public DatabaseScanTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        long bench_start = System.nanoTime();
        ScanResult result = new ScanResult();

        String storageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState))
        {
            Context context = contextRef.get();
            if (context != null)
            {
                String collectionPath = AppSettings.getCollectionPath(context)[0];    // TODO: support for multiple paths
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

        long bench_end = System.nanoTime();
        Log.d(TAG, "scan: took " + ((bench_end - bench_start) / 1000000.0) + " ms; " + result.count + " items.");
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
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(EXT_GEOPDF))
            {
                if (fileName.endsWith(EXT_GEOPDF_TM)) {
                    scanFile_USTopo(file, result);

                } else {
                    scanFile_HTMC(file, result);
                }
            } else {
                Log.d(TAG, "scanFile: ignoring " + file.getAbsolutePath());
            }
        }
    }

    protected void scanFile_USTopo(File file, ScanResult result)
    {
        ContentValues fileValues = getValuesFromFileName_USTopo(file);
        if (fileValues != null)
        {
            Log.d(TAG, "scanFile: USTopo: " + file.getAbsolutePath());
            fileValues = updateValuesFromDB_USTopo(fileValues);

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

    protected void scanFile_HTMC(File file, ScanResult result)
    {
        ContentValues fileValues = getValuesFromFileName_HTMC(file);
        if (fileValues != null)
        {
            Log.d(TAG, "scanFile: HTMC: " + file.getAbsolutePath());
            fileValues = updateValuesFromDB_HTMC(fileValues);

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

    protected static class ScanResult
    {
        int count = 0;
    }

    protected ContentValues getValuesFromFileName_USTopo( File file )
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

    protected ContentValues getValuesFromFileName_HTMC( File file )
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
            Log.w(TAG, "scanFile: unrecognized HTMC filename " + fileName);
            return null;
        }
    }

    protected ContentValues updateValuesFromDB_USTopo(ContentValues values)
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
                    updateValuesFromDB(values, cursor, gdaItemID);

                } else {
                    Log.w(TAG, "updateValuesFromDB: missing GDA Item ID; trying name + date instead...");
                    String mapName = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME);
                    String mapYear = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_DATE);
                    if (mapName != null && !mapName.isEmpty() && mapYear != null && !mapYear.isEmpty())
                    {
                        Cursor cursor = database.getMap(TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO, mapName, mapYear, TopoIndexDatabaseAdapter.QUERY_MAPS_FULLENTRY_USTOPO);
                        updateValuesFromDB(values, cursor, mapName);

                    } else Log.w(TAG, "updateValuesFromDB: missing name + date; skipping...");
                }
            } else Log.w(TAG, "updateValuesFromDB: missing series; skipping ");
        }  else Log.w(TAG, "updateValuesFromDB: missing values; skipping");

        return values;
    }

    protected ContentValues updateValuesFromDB_HTMC(ContentValues values)
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
                    updateValuesFromDB(values, cursor, scanID);

                } else Log.w(TAG, "updateValuesFromDB: missing Scan ID; skipping");
            } else Log.w(TAG, "updateValuesFromDB: missing series; skipping ");
        }  else Log.w(TAG, "updateValuesFromDB: missing values; skipping");

        return values;
    }

    protected void updateValuesFromDB(ContentValues values, Cursor cursor, @Nullable String itemID)
    {
        if (cursor != null)
        {
            if (cursor.getCount() > 0)
            {
                Log.d(TAG, "updateValuesFromDB: " + itemID);
                cursor.moveToFirst();
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_SCANID, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_CELLID, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID, cursor, values);

                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST, cursor, values);

                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_SCALE, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_PROJECTION, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_DATUM, cursor, values);
                putIntoValues(TopoIndexDatabaseAdapter.KEY_MAP_VERSION, cursor, values);

            } else Log.w(TAG, "updateValuesFromDB: empty cursor; skipping " + itemID);
            cursor.close();

        } else Log.w(TAG, "updateValuesFromDB: null cursor; skipping " + itemID);
    }

    private static void putIntoValues(String key, Cursor cursor, ContentValues values)
    {
        int i = cursor.getColumnIndex(key);
        if (i != -1) {
            values.put(key, cursor.getString(i));
        }
    }
}
