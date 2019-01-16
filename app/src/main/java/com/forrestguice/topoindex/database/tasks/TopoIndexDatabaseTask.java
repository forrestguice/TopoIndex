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
import android.os.AsyncTask;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.lang.ref.WeakReference;

public abstract class TopoIndexDatabaseTask extends AsyncTask<Uri, DatabaseTaskProgress, DatabaseTaskResult>
{
    public static final String TAG = "TopoIndexTask";

    protected WeakReference<Context> contextRef;
    protected TopoIndexDatabaseAdapter database;

    public TopoIndexDatabaseTask(Context context )
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
    protected void onProgressUpdate( DatabaseTaskProgress... progress )
    {
        super.onProgressUpdate(progress);
        if (taskListener != null) {
            taskListener.onProgress(progress);
        }
    }

    @Override
    protected void onPostExecute( DatabaseTaskResult result )
    {
        super.onPostExecute(result);
        if (taskListener != null) {
            taskListener.onFinished(result);
        }
    }

    protected DatabaseTaskListener taskListener = null;
    public void setTaskListener( DatabaseTaskListener listener )
    {
        this.taskListener = listener;
    }

}


