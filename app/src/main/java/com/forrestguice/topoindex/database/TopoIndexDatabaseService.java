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

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import com.forrestguice.topoindex.MainActivity;
import com.forrestguice.topoindex.R;

public class TopoIndexDatabaseService extends Service
{
    public static final String TAG = "TopoIndexService";

    public static final String ACTION_INIT_DB = "initDatabase";

    public final static int STATUS_READY = 0;
    public final static int STATUS_BUSY = 1;

    public static final String EXTRA_SERVICE_LISTENER = "service_listener";

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return taskBinder;
    }

    private final TopoIndexDatabaseServiceBinder taskBinder = new TopoIndexDatabaseServiceBinder();
    public class TopoIndexDatabaseServiceBinder extends Binder
    {
        public TopoIndexDatabaseService getService() {
            return TopoIndexDatabaseService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        if (action != null)
        {
            TopoIndexDatabaseServiceListener serviceListener = intent.getParcelableExtra(EXTRA_SERVICE_LISTENER);

            if (action.equals(ACTION_INIT_DB))
            {
                Log.d(TAG, "onStartCommand: " + action);
                boolean started = runDatabaseInitTask(this, intent, intent.getData(), null);

                signalOnStartCommand(started);
                if (serviceListener != null) {
                    serviceListener.onStartCommand(started);
                }

            } else Log.d(TAG, "onStartCommand: unrecognized action: " + action);

        } else Log.d(TAG, "onStartCommand: null action");
        return START_NOT_STICKY;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int NOTIFICATION_PROGRESS = 10;
    public static final int NOTIFICATION_COMPLETE = 20;
    public static final int NOTIFICATION_FAILED = 30;

    private static TopoIndexDatabaseInitTask databaseTask = null;
    private static TopoIndexDatabaseInitTask.InitTaskListener databaseTaskListener;
    public boolean runDatabaseInitTask(final Context context, Intent intent, Uri uri, @Nullable final TopoIndexDatabaseInitTask.InitTaskListener listener)
    {
        if (getStatus() != STATUS_READY) {
            Log.w(TAG, "runCalendarTask: A task is already running! ignoring...");
            return false;
        }

        databaseTask = new TopoIndexDatabaseInitTask(context);

        databaseTaskListener = new TopoIndexDatabaseInitTask.InitTaskListener()
        {
            @Override
            public void onStarted()
            {
                if (listener != null) {
                    listener.onStarted();
                }

                String message = "Initializing database";  // TODO

                signalOnStatusChanged(STATUS_BUSY);
                signalOnProgress(new TopoIndexDatabaseInitTask.DatabaseTaskProgress(message, 0, 0));  // TODO

                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                notificationBuilder.setContentTitle(context.getString(R.string.app_name))
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_launcher)        // TODO
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setProgress(0, 0, true);
                startService(new Intent( context, TopoIndexDatabaseService.class));  // bind the service to itself (to keep things running if the activity unbinds)
                startForeground(NOTIFICATION_PROGRESS, notificationBuilder.build());

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(NOTIFICATION_COMPLETE);
                notificationManager.cancel(NOTIFICATION_FAILED);
            }

            @Override
            public void onProgress(TopoIndexDatabaseInitTask.DatabaseTaskProgress... progress)
            {
                if (listener != null) {
                    listener.onProgress(progress);
                }
            }

            @Override
            public void onFinished(TopoIndexDatabaseInitTask.InitTaskResult result)
            {
                if (listener != null) {
                    listener.onFinished(result);
                }

                if (result.getResult())
                {
                    String message = "Initialized database";   // TODO
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                    notificationBuilder.setContentTitle(context.getString(R.string.app_name))
                            .setContentText(message)
                            .setSmallIcon(R.mipmap.ic_launcher)       // TODO
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(getNotificationIntent()).setAutoCancel(true)
                            .setProgress(0, 0, false);

                    notificationManager.notify(NOTIFICATION_COMPLETE, notificationBuilder.build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();

                } else {
                    String message = "Failed to initialize database";    // TODO
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);
                    notificationBuilder.setContentTitle(context.getString(R.string.app_name))
                            .setContentText(message)
                            .setSmallIcon(R.mipmap.ic_launcher)       // TODO
                            .setPriority(NotificationCompat.PRIORITY_LOW)
                            .setContentIntent(getNotificationIntent()).setAutoCancel(true)
                            .setProgress(0, 0, false);

                    notificationManager.notify(NOTIFICATION_FAILED, notificationBuilder.build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();
                }
            }

            private PendingIntent getNotificationIntent()
            {
                Intent intent = new Intent(context, MainActivity.class);
                return PendingIntent.getActivity(context, 0, intent, 0);
            }
        };
        databaseTask.setTaskListener(databaseTaskListener);
        databaseTask.execute(uri);
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private int status = STATUS_READY;
    public int getStatus()
    {
        if (databaseTask != null)
        {
            switch (databaseTask.getStatus())
            {
                case PENDING:
                case RUNNING:
                    status = STATUS_BUSY;

                case FINISHED:
                default:
                    status = STATUS_READY;
            }
        } else status = STATUS_READY;

        return status;
    }

    private TopoIndexDatabaseInitTask.DatabaseTaskProgress lastProgress;
    public TopoIndexDatabaseInitTask.DatabaseTaskProgress getLastProgress()
    {
        return lastProgress;
    }

    private ArrayList<TopoIndexDatabaseServiceListener> serviceListeners = new ArrayList<>();
    public void addServiceListener(TopoIndexDatabaseServiceListener listener)
    {
        serviceListeners.add(listener);
    }

    public void removeServiceListener(TopoIndexDatabaseServiceListener listener)
    {
        if (serviceListeners.contains(listener)) {
            serviceListeners.remove(listener);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * SuntimesCalendarServiceListener
     */
    public static abstract class TopoIndexDatabaseServiceListener implements Parcelable
    {
        public void onStartCommand(boolean result) {}
        public void onStatusChanged(int status) {}
        public void onProgress(TopoIndexDatabaseInitTask.DatabaseTaskProgress progress) {}

        public TopoIndexDatabaseServiceListener() {}
        protected TopoIndexDatabaseServiceListener(Parcel in) {}

        @Override
        public void writeToParcel(Parcel dest, int flags) {}

        @Override
        public int describeContents() {
            return 0;
        }
    }

    private void signalOnStartCommand(boolean result)
    {
        for (TopoIndexDatabaseServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onStartCommand(result);
            }
        }
    }

    private void signalOnStatusChanged(int status)
    {
        for (TopoIndexDatabaseServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onStatusChanged(status);
            }
        }
    }

    private void signalOnProgress(TopoIndexDatabaseInitTask.DatabaseTaskProgress progress)
    {
        lastProgress = progress;
        for (TopoIndexDatabaseServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onProgress(progress);
            }
        }
    }

}
