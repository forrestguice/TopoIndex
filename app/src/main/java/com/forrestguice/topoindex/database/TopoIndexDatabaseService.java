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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.forrestguice.topoindex.MainActivity;
import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskListener;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskProgress;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskResult;
import com.forrestguice.topoindex.database.tasks.DatabaseInitTask;
import com.forrestguice.topoindex.database.tasks.DatabaseScanTask;
import com.forrestguice.topoindex.database.tasks.DatabaseTask;

public class TopoIndexDatabaseService extends Service
{
    public static final String TAG = "TopoIndexService";

    public static final String ACTION_INIT = "initDatabase";
    public static final String ACTION_SCAN = "scanCollection";

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
        if (intent != null)
        {
            String action = intent.getAction();
            if (action != null)
            {
                TopoIndexDatabaseServiceListener serviceListener = intent.getParcelableExtra(EXTRA_SERVICE_LISTENER);

                if (action.equals(ACTION_INIT)) {
                    Log.d(TAG, "onStartCommand: " + action);
                    boolean started = runDatabaseInitTask(this, intent, intent.getData(), null);
                    signalOnStartCommand(started);
                    if (serviceListener != null) {
                        serviceListener.onStartCommand(started);
                    }

                } else if (action.equals(ACTION_SCAN)) {
                    Log.d(TAG, "onStartCommand: " + action);
                    boolean started = runScanCollectionTask(this,null);
                    signalOnStartCommand(started);
                    if (serviceListener != null) {
                        serviceListener.onStartCommand(started);
                    }

                } else Log.d(TAG, "onStartCommand: unrecognized action: " + action);

            } else Log.d(TAG, "onStartCommand: null action");
        } else Log.d(TAG, "onStartCommand: null intent");
        return START_NOT_STICKY;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int NOTIFICATION_PROGRESS = 10;
    public static final int NOTIFICATION_COMPLETE = 20;
    public static final int NOTIFICATION_FAILED = 30;

    private static DatabaseTask databaseTask = null;
    private static DatabaseTaskListener databaseTaskListener;

    /**
     * runScanCollectionTask
     */
    public boolean runScanCollectionTask(final Context context, @Nullable final DatabaseTaskListener listener)
    {
        if (getStatus() != STATUS_READY) {
            Log.w(TAG, "scanCollection: A task is already running! ignoring...");
            return false;
        }

        databaseTask = new DatabaseScanTask(context);
        databaseTaskListener = new DatabaseTaskListener()
        {
            private NotificationCompat.Builder progressNotification;

            @Override
            public void onStarted()
            {
                if (listener != null) {
                    listener.onStarted();
                }

                String message = context.getString(R.string.database_scan_progress);
                signalOnStatusChanged(STATUS_BUSY);
                signalOnProgress(new DatabaseTaskProgress(message, 0, 0));

                progressNotification = createProgressNotificationBuilder(context, message);
                startService(new Intent( context, TopoIndexDatabaseService.class));  // bind the service to itself (to keep things running if the activity unbinds)
                startForeground(NOTIFICATION_PROGRESS, progressNotification.build());

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(NOTIFICATION_COMPLETE);
                notificationManager.cancel(NOTIFICATION_FAILED);
            }

            @Override
            public void onProgress(DatabaseTaskProgress... progress)
            {
                if (progressNotification != null) {
                    progressNotification.setProgress(progress[0].numItems(), progress[0].itemNumber(), false);
                    startForeground(NOTIFICATION_PROGRESS, progressNotification.build());
                }

                progress[0].setMessage(context.getString(R.string.database_scan_progress));
                signalOnProgress(progress[0]);
            }

            @Override
            public void onFinished(DatabaseTaskResult result)
            {
                if (listener != null) {
                    listener.onFinished(result);
                }

                if (result.getResult())
                {
                    String message = context.getString(R.string.database_scan_success, result.numItems() + "");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_COMPLETE, createSuccessNotificationBuilder(context, message).build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();

                } else {
                    String message = context.getString(R.string.database_update_failed);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_FAILED, createFailedNotificationBuilder(context, message).build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();
                }
            }


        };
        databaseTask.setTaskListener(databaseTaskListener);
        databaseTask.execute();
        return true;
    }

    /**
     * runDatabaseInitTask
     */
    public boolean runDatabaseInitTask(final Context context, @Nullable Intent intent, Uri uri, @Nullable final DatabaseTaskListener listener)
    {
        if (getStatus() != STATUS_READY) {
            Log.w(TAG, "runDatabaseInitTask: A task is already running! ignoring...");
            return false;
        }

        databaseTask = new DatabaseInitTask(context);

        databaseTaskListener = new DatabaseTaskListener()
        {
            @Override
            public void onStarted()
            {
                if (listener != null) {
                    listener.onStarted();
                }

                String message = context.getString(R.string.database_update_progress, "");
                signalOnStatusChanged(STATUS_BUSY);
                signalOnProgress(new DatabaseTaskProgress(message, 0, 100));

                progressNotification = createProgressNotificationBuilder(context, message);
                startService(new Intent( context, TopoIndexDatabaseService.class));  // bind the service to itself (to keep things running if the activity unbinds)
                startForeground(NOTIFICATION_PROGRESS, progressNotification.build());

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.cancel(NOTIFICATION_COMPLETE);
                notificationManager.cancel(NOTIFICATION_FAILED);
            }

            private NotificationCompat.Builder progressNotification;
            private NumberFormat percentFormatter;

            @Override
            public void onProgress(DatabaseTaskProgress... progress)
            {
                if (percentFormatter == null) {
                    percentFormatter = DecimalFormat.getPercentInstance();
                    percentFormatter.setMinimumFractionDigits(0);
                    percentFormatter.setMaximumFractionDigits(0);
                }
                String percentString = percentFormatter.format((double)progress[0].itemNumber() / (double)progress[0].numItems());
                progress[0].setMessage(context.getString(R.string.database_update_progress, percentString));
                signalOnProgress(progress[0]);

                if (progressNotification != null) {
                    progressNotification.setContentText(progress[0].getMessage());
                    progressNotification.setProgress(progress[0].numItems(), progress[0].itemNumber(), false);
                    startForeground(NOTIFICATION_PROGRESS, progressNotification.build());
                }
            }

            @Override
            public void onFinished(DatabaseTaskResult result)
            {
                if (result.getResult()) {
                    TopoIndexDatabaseSettings.setDatabaseLastUpdate(context, Calendar.getInstance().getTimeInMillis());
                    TopoIndexDatabaseSettings.setDatabaseDate(context, result.getDate());
                }

                if (listener != null) {
                    listener.onFinished(result);
                }

                if (result.getResult())
                {
                    String message = context.getString(R.string.database_update_success, result.numItems() + "");
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_COMPLETE, createSuccessNotificationBuilder(context, message).build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();

                } else {
                    String message = context.getString(R.string.database_update_failed);
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    notificationManager.notify(NOTIFICATION_FAILED, createFailedNotificationBuilder(context, message).build());
                    signalOnStatusChanged(STATUS_READY);
                    stopForeground(true);
                    stopSelf();
                }
            }

        };
        databaseTask.setTaskListener(databaseTaskListener);
        databaseTask.execute(uri);
        return true;
    }

    private static NotificationCompat.Builder createSuccessNotificationBuilder(Context context, String message)
    {
        NotificationCompat.Builder retValue = new NotificationCompat.Builder(context);
        retValue.setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)       // TODO
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getNotificationIntent(context)).setAutoCancel(true)
                .setProgress(0, 0, false);
        return retValue;
    }

    private static NotificationCompat.Builder createFailedNotificationBuilder(Context context, String message)
    {
        NotificationCompat.Builder retValue = new NotificationCompat.Builder(context);
        retValue.setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)       // TODO
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(getNotificationIntent(context)).setAutoCancel(true)
                .setProgress(0, 0, false);
        return retValue;
    }

    private static NotificationCompat.Builder createProgressNotificationBuilder(Context context, String message)
    {
        NotificationCompat.Builder retValue = new NotificationCompat.Builder(context);
        retValue.setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)        // TODO
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setProgress(0, 0, true);
        return retValue;
    }

    public static PendingIntent getNotificationIntent(Context context)
    {
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public int getStatus()
    {
        if (databaseTask != null)
        {
            switch (databaseTask.getStatus())
            {
                case PENDING:
                case RUNNING:
                    return STATUS_BUSY;

                case FINISHED:
                default:
                    return STATUS_READY;
            }
        }
        return STATUS_READY;
    }

    private DatabaseTaskProgress lastProgress;
    public DatabaseTaskProgress getLastProgress()
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

    public static abstract class TopoIndexDatabaseServiceListener implements Parcelable
    {
        public void onStartCommand(boolean result) {}
        public void onStatusChanged(int status) {}
        public void onProgress(DatabaseTaskProgress progress) {}

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

    private void signalOnProgress(DatabaseTaskProgress progress)
    {
        lastProgress = progress;
        for (TopoIndexDatabaseServiceListener listener : serviceListeners) {
            if (listener != null) {
                listener.onProgress(progress);
            }
        }
    }

}
