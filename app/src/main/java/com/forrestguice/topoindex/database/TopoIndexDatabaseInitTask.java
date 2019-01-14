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

package com.forrestguice.topoindex.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.Pools;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TopoIndexDatabaseInitTask extends AsyncTask<Uri, TopoIndexDatabaseInitTask.DatabaseTaskProgress, TopoIndexDatabaseInitTask.InitTaskResult>
{
    public static final String TAG = "TopoIndexTask";

    private WeakReference<Context> contextRef;
    private TopoIndexDatabaseAdapter database;

    public TopoIndexDatabaseInitTask( Context context )
    {
        contextRef = new WeakReference<>(context);
        database = new TopoIndexDatabaseAdapter(context);
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
        if (taskListener != null) {
            taskListener.onStarted();
        }
    }

    @Override
    protected InitTaskResult doInBackground(Uri... uris)
    {
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
                        Log.d(TAG, "DatabaseInitTask: zip contains file: " + filename);

                        BufferedInputStream bufferedInput = new BufferedInputStream(zipInput);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(bufferedInput));

                        String columnsLine = reader.readLine();
                        String[] columns = columnsLine.split(",");
                        Log.d(TAG, "DatabaseInitTask: columns: " + columnsLine);

                        database.open();

                        ArrayList<ContentValues> htmcValues = new ArrayList<>();
                        ArrayList<ContentValues> ustopoValues = new ArrayList<>();
                        int batchValuesNum = 1000;

                        int i = 1;         // count lines
                        int c = 0;             // count items
                        int n = 350000;  // rough estimate total items
                        String line;
                        String series;
                        String[] entry;
                        DatabaseTaskProgress progressObj = new DatabaseTaskProgress("", 0, n);

                        while ((line = reader.readLine()) != null)
                        {
                            entry = line.split(",");
                            if (entry.length == columns.length)
                            {
                                series = entry[0];
                                if (series.equals(TopoIndexDatabaseAdapter.VAL_MAP_SERIES_HTMC))
                                {
                                    ContentValues values = new ContentValues();
                                    TopoIndexDatabaseAdapter.toContentValues(values, entry);
                                    htmcValues.add(values);

                                    if (htmcValues.size() >= batchValuesNum) {
                                        database.addMaps_USGS_HTMC(htmcValues.toArray(new ContentValues[0]));
                                        htmcValues.clear();
                                        progressObj.count[0] = c;
                                        publishProgress(progressObj);
                                    }
                                    c++;

                                } else if (series.equals(TopoIndexDatabaseAdapter.VAL_MAP_SERIES_USTOPO)) {
                                    ContentValues values = new ContentValues();
                                    TopoIndexDatabaseAdapter.toContentValues(values, entry);
                                    ustopoValues.add(values);

                                    if (ustopoValues.size() >= batchValuesNum) {
                                        database.addMaps_USGS_USTopo( ustopoValues.toArray(new ContentValues[0]) );
                                        ustopoValues.clear();
                                        progressObj.count[0] = c;
                                        publishProgress(progressObj);
                                    }
                                    c++;

                                } else {
                                    Log.w(TAG, "DatabaseInitTask: unrecognized series: " + entry[0] + " .. " + line + " .. ignoring this line...");
                                }
                            } else {
                                Log.w(TAG, "DatabaseInitTask: line " + i + " has the wrong number of columns; " + entry.length + " (expects " + columns.length + ") .. ignoring this line...");
                            }
                            i++;
                        }

                        if (htmcValues.size() >= 0) {
                            database.addMaps_USGS_HTMC(htmcValues.toArray(new ContentValues[0]));
                            htmcValues.clear();
                        }

                        if (ustopoValues.size() >= 0) {
                            database.addMaps_USGS_USTopo( ustopoValues.toArray(new ContentValues[0]) );
                            ustopoValues.clear();
                        }

                        database.close();

                        zipInput.closeEntry();
                        zipInput.close();
                        return new InitTaskResult(true, c, Calendar.getInstance().getTimeInMillis());

                    } else {
                        zipInput.closeEntry();
                        zipInput.close();
                        Log.e(TAG, "DatabaseInitTask: Zip is missing file: " + filename);
                        return new InitTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "DatabaseInitTask: FileNotFound! " + e);

                } catch (IOException e) {
                    Log.e(TAG, "DatabaseInitTask: IOException! " + e);
                }
                return new InitTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());

            } else {
                Log.e(TAG, "DatabaseInitTask: null context!");
                return new InitTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
            }
        } else {
            Log.e(TAG, "DatabaseInitTask: missing uri!");
            return new InitTaskResult(false, 0, Calendar.getInstance().getTimeInMillis());
        }
    }

    @Override
    protected void onProgressUpdate( DatabaseTaskProgress... progress )
    {
        super.onProgressUpdate(progress);
        if (taskListener != null) {
            taskListener.onProgress(progress);
        }
    }

    @Override
    protected void onPostExecute( InitTaskResult result )
    {
        super.onPostExecute(result);
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * InitTaskResult
     */
    public static class InitTaskResult
    {
        public InitTaskResult(boolean result, int count, long date)
        {
            this.result = result;
            this.count = count;
            this.date = date;
        }

        private boolean result;
        public boolean getResult()
        {
            return result;
        }

        private int count;
        public int numItems() {
            return count;
        }

        private long date;
        public long getDate()
        {
            return date;
        }
    }

    /**
     * InitTaskProgress
     */
    public static class DatabaseTaskProgress
    {
        public DatabaseTaskProgress( String msg, int itemNum, int numItems )
        {
            this.message = msg;
            this.count[0] = itemNum;
            this.count[1] = numItems;
        }

        protected String message;
        public String getMessage() {
            return message;
        }

        protected int[] count = new int[] {0, 0};
        public int itemNumber() {
            return count[0];
        }
        public int numItems() {
            return count[1];
        }
    }

    /**
     * InitTaskListener
     */
    public static abstract class InitTaskListener
    {
        public abstract void onStarted();
        public abstract void onProgress( DatabaseTaskProgress... progress );
        public abstract void onFinished( InitTaskResult result );
    }

    private InitTaskListener taskListener = null;
    public void setTaskListener( InitTaskListener listener )
    {
        this.taskListener = listener;
    }

}


