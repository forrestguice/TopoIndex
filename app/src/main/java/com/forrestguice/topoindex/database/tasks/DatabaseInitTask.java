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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatabaseInitTask extends DatabaseTask
{
    public static final String TAG = "TopoIndexTask";

    public DatabaseInitTask(Context context )
    {
        super(context);
    }

    @Override
    protected DatabaseTaskResult doInBackground(Uri... uris)
    {
        long bench_start = System.nanoTime();
        if (uris.length > 0)
        {
            Uri uri = uris[0];
            Context context = contextRef.get();
            if (context != null)
            {
                String assetName = "topomaps_all.zip";
                String filename = "topomaps_all.csv";
                ContentResolver resolver = context.getContentResolver();
                InputStream input;
                ZipInputStream zipInput;
                try {
                    if (uri == null) {
                        Log.d(TAG, "DatabaseInitTask: Assets: " + assetName);
                        AssetManager assets = context.getAssets();
                        input = assets.open(assetName);

                    } else {
                        Log.d(TAG, "DatabaseInitTask: URI: " + uri);
                        input = resolver.openInputStream(uri);
                    }

                    zipInput = new ZipInputStream(input);

                    ZipEntry zipFile;
                    boolean foundFile = false;
                    do {
                        zipFile = zipInput.getNextEntry();
                        if (zipFile != null && zipFile.getName().equals(filename)) {
                            foundFile = true;

                        } else if (zipFile != null) {
                            zipInput.closeEntry();
                        }
                    } while (zipFile != null && !foundFile);

                    if (foundFile)
                    {
                        Log.d(TAG, "initDB: zip contains file: " + filename);

                        BufferedInputStream bufferedInput = new BufferedInputStream(zipInput);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInput));

                        String columnsLine = reader.readLine();
                        String[] columns = columnsLine.split(",");
                        Log.d(TAG, "initDB: columns: " + columnsLine);

                        database.open();
                        //database.clearMaps(TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC, TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO);

                        ArrayList<ContentValues> htmcValues = new ArrayList<>();
                        ArrayList<ContentValues> ustopoValues = new ArrayList<>();
                        int batchValuesNum = 1000;

                        int i = 1;         // count lines
                        int c = 0;             // count items
                        int n = 350000;  // rough estimate total items
                        String line;
                        String series;
                        String[] entry;
                        ContentValues values = new ContentValues();
                        ContentValues[] arrayType_contentValues = new ContentValues[0];
                        DatabaseTaskProgress progressObj = new DatabaseTaskProgress("", 0, n);

                        String val_htmc = "\"" + TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC + "\"";
                        String val_ustopo = "\"" + TopoIndexDatabaseAdapter.VAL_MAP_SERIES_USTOPO + "\"";

                        while ((line = reader.readLine()) != null)
                        {
                            entry = line.split(",");
                            if (entry.length == columns.length)
                            {
                                series = entry[0];
                                if (series.equals(val_htmc))
                                {
                                    values.clear();
                                    TopoIndexDatabaseAdapter.toContentValues(values, entry);
                                    htmcValues.add(values);

                                    if (htmcValues.size() >= batchValuesNum) {
                                        database.addMaps_HTMC(htmcValues.toArray(arrayType_contentValues));
                                        htmcValues.clear();
                                        progressObj.count[0] = c;
                                        publishProgress(progressObj);
                                    }
                                    c++;

                                } else if (series.equals(val_ustopo)) {
                                    values.clear();
                                    TopoIndexDatabaseAdapter.toContentValues(values, entry);
                                    ustopoValues.add(values);

                                    if (ustopoValues.size() >= batchValuesNum) {
                                        database.addMaps_USTopo( ustopoValues.toArray(arrayType_contentValues) );
                                        ustopoValues.clear();
                                        progressObj.count[0] = c;
                                        publishProgress(progressObj);
                                    }
                                    c++;

                                } else {
                                    Log.w(TAG, "initDB: unrecognized series: " + entry[0] + " .. " + line + " .. ignoring this line...");
                                }
                            } else {
                                Log.w(TAG, "initDB: line " + i + " has the wrong number of columns; " + entry.length + " (expects " + columns.length + ") .. ignoring this line...");
                            }
                            i++;
                        }

                        if (htmcValues.size() >= 0) {
                            database.addMaps_HTMC(htmcValues.toArray(arrayType_contentValues));
                            htmcValues.clear();
                        }

                        if (ustopoValues.size() >= 0) {
                            database.addMaps_USTopo( ustopoValues.toArray(arrayType_contentValues) );
                            ustopoValues.clear();
                        }

                        database.close();

                        zipInput.closeEntry();
                        zipInput.close();

                        long bench_end = System.nanoTime();
                        Log.d(TAG, "initDB: took " + ((bench_end - bench_start) / 1000000.0 / 1000.0 / 60.0) + " min; " + c + " rows.");
                        return new DatabaseTaskResult(true, c, Calendar.getInstance().getTimeInMillis());

                    } else {
                        zipInput.closeEntry();
                        zipInput.close();
                        Log.e(TAG, "initDB: Zip is missing file: " + filename);
                        return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "initDB: FileNotFound! " + e);

                } catch (IOException e) {
                    Log.e(TAG, "initDB: IOException! " + e);
                }
                return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());

            } else {
                Log.e(TAG, "initDB: null context!");
                return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
            }
        } else {
            Log.e(TAG, "initDB: missing uri!");
            return new DatabaseTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
        }
    }

}


