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

package com.forrestguice.topoindex;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.forrestguice.topoindex.database.tasks.DatabaseTaskProgress;
import com.forrestguice.topoindex.database.TopoIndexDatabaseService;
import com.forrestguice.topoindex.database.TopoIndexDatabaseSettings;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity
{
    public static final String TAG = "TopoIndexSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onIsMultiPane()
    {
        return (getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || DatabasePreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * GeneralPreferenceFragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            //bindPreferenceSummaryToValue(findPreference("example_text"));  // TODO
            //bindPreferenceSummaryToValue(findPreference("example_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * DatabasePreferenceFragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DatabasePreferenceFragment extends PreferenceFragment
    {
        private static TopoIndexDatabaseService databaseService;
        private boolean boundToService = false;
        private Snackbar snackbar;
        private ProgressBar snackbarProgress;

        private ServiceConnection databaseServiceConnection = new ServiceConnection()
        {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                TopoIndexDatabaseService.TopoIndexDatabaseServiceBinder binder = (TopoIndexDatabaseService.TopoIndexDatabaseServiceBinder) service;
                databaseService = binder.getService();
                boundToService = true;
                databaseService.addServiceListener(serviceListener);
                if (serviceListener != null) {
                    serviceListener.onStatusChanged(databaseService.getStatus());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                boundToService = false;
            }
        };

        private TopoIndexDatabaseService.TopoIndexDatabaseServiceListener serviceListener = new TopoIndexDatabaseService.TopoIndexDatabaseServiceListener() {
            @Override
            public void onStatusChanged(int status)
            {
                if (action_sync != null) {
                    action_sync.setEnabled(status == TopoIndexDatabaseService.STATUS_READY);
                }

                if (snackbar == null)                      // lazy init snackbar
                {
                    Activity activity = getActivity();
                    View view = getView();
                    if (activity != null && view != null)
                    {
                        snackbar = Snackbar.make(getView(), "", Snackbar.LENGTH_INDEFINITE);
                        ViewGroup snackbarContent = (ViewGroup) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
                        if (snackbarContent != null) {
                            snackbarProgress = new ProgressBar(getActivity()); // for  horizontal progress: // (context, null, android.R.attr.progressBarStyleHorizontal);
                            snackbarProgress.setPadding(0, 8, 0, 8);
                            snackbarContent.addView(snackbarProgress);
                        } else Log.w(TAG, "initViews: android.support.design.R.id.snackbar_text not found!");
                    }
                }

                if (snackbar != null) {
                    if (status == TopoIndexDatabaseService.STATUS_READY)
                        snackbar.dismiss();
                    else snackbar.show();
                }

                if (status != TopoIndexDatabaseService.STATUS_READY) {
                    DatabaseTaskProgress lastProgress = databaseService.getLastProgress();
                    if (lastProgress != null) {
                        onProgress(lastProgress);
                    }
                }

                updateDateFields();
            }

            @Override
            public void onProgress(DatabaseTaskProgress progress)
            {
                if (snackbar != null) {
                    snackbar.setText(progress.getMessage());
                }

                if (snackbarProgress != null) {
                    int n = progress.numItems();
                    if (n > 0) {
                        snackbarProgress.setIndeterminate(false);
                    }
                    snackbarProgress.setMax(progress.numItems());
                    snackbarProgress.setProgress(progress.itemNumber());
                }
            }
        };

        @Override
        public void onStart()
        {
            super.onStart();
            Activity activity = getActivity();
            activity.bindService(new Intent(activity, TopoIndexDatabaseService.class),
                    databaseServiceConnection, Context.BIND_AUTO_CREATE);
        }

        @Override
        public void onStop()
        {
            super.onStop();
            databaseService.removeServiceListener(serviceListener);
            getActivity().unbindService(databaseServiceConnection);
            boundToService = false;
            Log.d(TAG, "Unbound from database service...");
        }

        private Preference action_sync, info_date, info_lastupdate;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_database);
            setHasOptionsMenu(true);

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));  // TODO

            info_date = findPreference(TopoIndexDatabaseSettings.KEY_DATABASE_DATE);
            info_lastupdate = findPreference(TopoIndexDatabaseSettings.KEY_DATABASE_LASTUPDATE);
            updateDateFields();

            action_sync = findPreference(TopoIndexDatabaseSettings.KEY_DATABASE_UPDATENOW);
            if (action_sync != null)
            {
                action_sync.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
                {
                    @Override
                    public boolean onPreferenceClick(Preference preference)
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle(getString(R.string.database_update_confirm_title));
                        dialog.setMessage(getString(R.string.database_update_confirm_message));

                        dialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (databaseService != null) {
                                    databaseService.runDatabaseInitTask(getActivity(), null, null, null);
                                }
                            }
                        });
                        dialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                /* EMPTY */
                            }
                        });

                        dialog.show();
                        return false;
                    }
                });
            }
        }

        private void updateDateFields()
        {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity().getApplicationContext());

            long databaseDateMillis = TopoIndexDatabaseSettings.getDatabaseDate(getActivity());
            Calendar databaseDate = Calendar.getInstance();
            databaseDate.setTimeInMillis(databaseDateMillis);
            info_date.setSummary( databaseDateMillis == -1 ? "none" : dateFormat.format(databaseDate.getTime()) );   // TODO: i18n

            long updateDateMillis = TopoIndexDatabaseSettings.getDatabaseLastUpdate(getActivity());
            Calendar updateDate = Calendar.getInstance();
            updateDate.setTimeInMillis(updateDateMillis);
            info_lastupdate.setSummary( updateDateMillis == -1 ? "never" : dateFormat.format(updateDate.getTime()) );  // TODO: i18n
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            int id = item.getItemId();
            if (id == android.R.id.home)
            {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * preferenceSummaryListener
     */
    private static Preference.OnPreferenceChangeListener preferenceSummaryListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value)
        {
            String stringValue = value.toString();
            if (preference instanceof ListPreference)
            {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void applyPreferenceSummaryListener(Preference preference)
    {
        preference.setOnPreferenceChangeListener(preferenceSummaryListener);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        preferenceSummaryListener.onPreferenceChange(preference, prefs.getString(preference.getKey(), ""));
    }

}
