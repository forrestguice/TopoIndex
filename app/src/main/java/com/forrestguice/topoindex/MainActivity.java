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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;
import com.forrestguice.topoindex.database.TopoIndexDatabaseInitTask;
import com.forrestguice.topoindex.database.TopoIndexDatabaseService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String TAG = "TopoIndexActivity";
    public static final String TAG_ABOUT = "about";

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews(this);
    }

    private void initViews(Context context)
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.list_maps);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                initListAdapter(MainActivity.this, null);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initListAdapter(Context context, String table)
    {
        if (database == null) {
            database = new TopoIndexDatabaseAdapter(MainActivity.this);
        }

        ListAdapterTask task = new ListAdapterTask();
        task.execute(table);
    }

    private TopoIndexDatabaseAdapter database;
    private TopoIndexDatabaseCursorAdapter adapter;
    private class ListAdapterTask extends AsyncTask<String, Void, Cursor>
    {
        @Override
        protected void onPreExecute()
        {
            database.open();
        }

        @Override
        protected Cursor doInBackground(String... tables)
        {
            if (tables.length > 0 && tables[0] != null)
            {
                return database.getMaps(tables[0], 0, false);

            } else {
                return database.getMaps_USGS_HTMC(0, false);
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor)
        {
            adapter = new TopoIndexDatabaseCursorAdapter(MainActivity.this, cursor);
            if (listView != null) {
                listView.setAdapter(adapter);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Menus / Navigation
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_about:
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.show(getSupportFragmentManager(), TAG_ABOUT);
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.nav_local_list:
                // TODO
                break;

            case R.id.nav_index_usgs_htmc:
                initListAdapter(this, TopoIndexDatabaseAdapter.TABLE_MAPS_USGS_HTMC);
                break;

            case R.id.nav_index_usgs_ustopo:
                initListAdapter(this, TopoIndexDatabaseAdapter.TABLE_MAPS_USGS_USTOPO);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // initDatabase
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initDatabase()
    {
        initDatabase(this, null);
    }

    private boolean initDatabase(Context context, Uri uri)
    {
        if (databaseService.getStatus() != TopoIndexDatabaseService.STATUS_READY)
        {
            Log.w(TAG, "initDatabase: DatabaseInitTask is already running (or pending); ignoring call...");
            return false;
        }
        return databaseService.runDatabaseInitTask(context, null, uri, initTaskListener);
    }

    private Snackbar progressSnackbar;
    private TopoIndexDatabaseInitTask.InitTaskListener initTaskListener = new TopoIndexDatabaseInitTask.InitTaskListener()
    {
        @Override
        public void onStarted()
        {
            progressSnackbar = Snackbar.make(findViewById(R.id.fab), "Initializing the database . . .", Snackbar.LENGTH_LONG);
            progressSnackbar.setAction("Cancel", null);
            progressSnackbar.show();
        }

        @Override
        public void onProgress(TopoIndexDatabaseInitTask.DatabaseTaskProgress... progress)
        {
            if (progressSnackbar != null && progress.length > 0) {
                progressSnackbar.setText(progress[0].getMessage());
            }
        }

        @Override
        public void onFinished(TopoIndexDatabaseInitTask.InitTaskResult result)
        {
            progressSnackbar.setText("Database Initialization " + (result.getResult() ? "succeeded" : "failed"));
            progressSnackbar.setAction(null, null);
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DatabaseService
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private static TopoIndexDatabaseService databaseService;
    boolean boundToService = false;

    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, TopoIndexDatabaseService.class),
                databaseServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseService.removeServiceListener(serviceListener);
        unbindService(databaseServiceConnection);
        boundToService = false;
    }

    private ServiceConnection databaseServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            TopoIndexDatabaseService.TopoIndexDatabaseServiceBinder binder = (TopoIndexDatabaseService.TopoIndexDatabaseServiceBinder) service;
            databaseService = binder.getService();
            boundToService = true;
            databaseService.addServiceListener(serviceListener);
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
            // TODO
        }

        @Override
        public void onProgress(TopoIndexDatabaseInitTask.DatabaseTaskProgress progress)
        {
            // TODO
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public class TopoIndexDatabaseCursorAdapter extends CursorAdapter
    {
        private LayoutInflater layoutInflater = null;

        public TopoIndexDatabaseCursorAdapter(Context context, Cursor c)
        {
            super(context, c);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup)
        {
            if (layoutInflater == null) {
                layoutInflater = LayoutInflater.from(context);
            }
            return layoutInflater.inflate(R.layout.map_list_item0, viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor)
        {
            TextView itemName = (TextView)view.findViewById(android.R.id.text1);
            itemName.setText(cursor.getString(cursor.getColumnIndex(TopoIndexDatabaseAdapter.KEY_MAP_NAME)));

            TextView itemScale = (TextView)view.findViewById(R.id.mapItem_scale);
            setText(itemScale, TopoIndexDatabaseAdapter.KEY_MAP_SCALE, cursor);

            TextView itemState = (TextView)view.findViewById(R.id.mapItem_state);
            setText(itemState, TopoIndexDatabaseAdapter.KEY_MAP_STATE, cursor);

            TextView itemDate = (TextView)view.findViewById(R.id.mapItem_date);
            setText(itemDate, TopoIndexDatabaseAdapter.KEY_MAP_DATE, cursor);

            TextView itemSeries = (TextView)view.findViewById(R.id.mapItem_series);
            setText(itemSeries, TopoIndexDatabaseAdapter.KEY_MAP_SERIES, cursor);
        }

        private void setText(TextView item, String columnName, Cursor cursor)
        {
            if (item != null) {
                int index = cursor.getColumnIndex(columnName);
                if (index != -1) {
                    item.setText(cursor.getString(index));
                }
            }
        }
    }
}
