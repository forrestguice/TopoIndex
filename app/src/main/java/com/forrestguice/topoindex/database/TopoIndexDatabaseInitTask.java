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
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
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

                        String line = reader.readLine();
                        Log.d(TAG, "DEBUG: First Line: " + line);
                        // TODO
                        int c = 0;             // count items

                        zipInput.closeEntry();
                        zipInput.close();
                        return new InitTaskResult(true, c);

                    } else {
                        zipInput.closeEntry();
                        zipInput.close();
                        Log.e(TAG, "DatabaseInitTask: Zip is missing file: " + filename);
                        return new InitTaskResult(false, 0);
                    }

                } catch (FileNotFoundException e) {
                    Log.e(TAG, "DatabaseInitTask: FileNotFound! " + e);

                } catch (IOException e) {
                    Log.e(TAG, "DatabaseInitTask: IOException! " + e);
                }
                return new InitTaskResult(false, 0);

            } else {
                Log.e(TAG, "DatabaseInitTask: null context!");
                return new InitTaskResult(false, 0);
            }
        } else {
            Log.e(TAG, "DatabaseInitTask: missing uri!");
            return new InitTaskResult(false, 0);
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
        public InitTaskResult(boolean result, int count)
        {
            this.result = result;
            this.count = count;
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


