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

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class TopoIndexDatabaseInitTask extends AsyncTask<Uri, TopoIndexDatabaseInitTask.DatabaseTaskProgress, TopoIndexDatabaseInitTask.InitTaskResult>
{
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
        return new InitTaskResult(false, 0);
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

        private String message;
        public String getMessage() {
            return message;
        }

        private int[] count = new int[] {0, 0};
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


