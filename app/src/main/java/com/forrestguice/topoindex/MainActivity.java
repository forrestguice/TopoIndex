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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AlertDialog;
import android.util.Log;

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
import android.widget.AdapterView;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;
import com.forrestguice.topoindex.database.tasks.DatabaseScanTask;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskListener;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskProgress;
import com.forrestguice.topoindex.database.tasks.DatabaseTaskResult;
import com.forrestguice.topoindex.database.TopoIndexDatabaseService;
import com.forrestguice.topoindex.dialogs.AboutDialog;
import com.forrestguice.topoindex.dialogs.FilterDialog;
import com.forrestguice.topoindex.dialogs.LocationDialog;
import com.forrestguice.topoindex.dialogs.MapItemDialog;
import com.forrestguice.topoindex.fragments.ListViewFragment;
import com.forrestguice.topoindex.fragments.QuadViewFragment;
import com.forrestguice.topoindex.fragments.TopoIndexFragment;

import java.io.File;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String TAG = "TopoIndexActivity";
    public static final String TAG_DIALOG_LOCATION = "location";
    public static final String TAG_DIALOG_FILTERS = "filters";
    public static final String TAG_DIALOG_ABOUT = "about";

    private Toolbar toolbar;
    private ViewPager pager;
    private TopoIndexPagerAdapter pagerAdapter;

    private Snackbar progressSnackbar;
    private ProgressBar progressSnackbarProgress;

    private FloatingActionButton fabFilters, fabFiltersClear;
    private FloatingActionButton[] fabs = new FloatingActionButton[0];

    @Override
    protected void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        setContentView(R.layout.activity_main);
        initViews(this);
        initLocation(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateViews();

        FragmentManager fragments = getSupportFragmentManager();
        restoreLocationDialog(fragments);
        restoreFilterDialog(fragments);
    }

    @Override
    protected void onSaveInstanceState( Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState( Bundle savedState )
    {
        super.onRestoreInstanceState(savedState);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViews(Context context)
    {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new TopoIndexPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        progressSnackbar = Snackbar.make(pager, getString(R.string.database_update_progress, ""), Snackbar.LENGTH_INDEFINITE);

        ViewGroup snackbarContent = (ViewGroup) progressSnackbar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        if (snackbarContent != null) {
            progressSnackbarProgress = new ProgressBar(context); // for  horizontal progress: // (context, null, android.R.attr.progressBarStyleHorizontal);
            progressSnackbarProgress.setPadding(0, 8, 0, 8);
            snackbarContent.addView(progressSnackbarProgress);
        } else Log.w(TAG, "initViews: android.support.design.R.id.snackbar_text not found!");

        fabFilters = (FloatingActionButton) findViewById(R.id.fab_filters);
        fabFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });

        fabFiltersClear = (FloatingActionButton) findViewById(R.id.fab_filters_clear);
        fabFiltersClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                clearFilters(MainActivity.this);
                fabFiltersClear.hide();
            }
        });

        fabs = new FloatingActionButton[] { fabFilters, fabFiltersClear };

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void showFabs(boolean withDelay)
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                fabFilters.setEnabled(true);
                if (!fabFilters.isShown()) {
                    fabFilters.show();
                }
            }
        }, withDelay ? 750 : 0);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                fabFiltersClear.setEnabled(true);
                if (AppSettings.hasNoFilters(MainActivity.this))
                    fabFiltersClear.hide();
                else fabFiltersClear.show();
            }
        }, withDelay ? 1250 : 0);
    }

    private void hideFabs()
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                for (FloatingActionButton fab : fabs) {
                    fab.setEnabled(false);
                    fab.hide();
                }
            }
        }, 350);
    }

    private void updateViews()
    {
        AppSettings.Location location = AppSettings.getLocation(MainActivity.this);
        toolbar.setSubtitle(getString(R.string.location_format, location.getLatitudeDisplay(), location.getLongitudeDisplay()));
        updateMenus();
    }

    private void updateMenus()
    {
        if (mainMenu != null)
        {
            boolean autoMode = AppSettings.getAutoLocation(MainActivity.this);
            MenuItem positionItem = mainMenu.findItem(R.id.action_location_auto);
            positionItem.setVisible(autoMode);

            if (autoMode)
            {
                LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null)
                {
                    boolean locationServicesEnabled = (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
                    positionItem.setIcon( locationServicesEnabled ? R.drawable.ic_menu_mylocation : R.drawable.ic_menu_mylocation_disabled  );
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Menus / Navigation
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private Menu mainMenu;
    private Menu navMenu;

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
        mainMenu = menu;
        updateMenus();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_scan:
                scanCollection();
                return true;

            case R.id.action_filters:
                showFilterDialog();
                return true;

            case R.id.action_help:
                AlertDialog.Builder helpDialog = new AlertDialog.Builder(MainActivity.this);
                helpDialog.setMessage(getString(R.string.help_general));
                helpDialog.show();
                return true;

            case R.id.action_location:
                showLocationDialog();
                return true;

            case R.id.action_about:
                AboutDialog aboutDialog = new AboutDialog();
                aboutDialog.show(getSupportFragmentManager(), TAG_DIALOG_ABOUT);
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.nav_local_list:
                initListAdapter(this, TopoIndexDatabaseAdapter.TABLE_MAPS);
                break;

            case R.id.nav_index_usgs_htmc:
                initListAdapter(this, TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC);
                break;

            case R.id.nav_index_usgs_ustopo:
                initListAdapter(this, TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private int getNavItemForTable(String table)
    {
        if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC)) {
            return R.id.nav_index_usgs_htmc;

        } else if (table.equals(TopoIndexDatabaseAdapter.VAL_MAP_SERIES_USTOPO)) {
            return R.id.nav_index_usgs_ustopo;

        } else {
            return R.id.nav_local_list;
        }
    }

    private void setNavItemChecked(String table)
    {
        NavigationView navView = (NavigationView) findViewById(R.id.nav_view);
        if (navView != null)
        {
            Menu navMenu = navView.getMenu();
            if (navMenu != null)
            {
                MenuItem menuItem = navMenu.findItem(getNavItemForTable(table));
                if (menuItem != null) {
                    menuItem.setChecked(true);
                }
            }
        }
    }

    private void openMapURL(String currentTable, String[] urls)
    {
        if (currentTable.equals(TopoIndexDatabaseAdapter.TABLE_MAPS))
        {
            String url = urls[0];
            if (url != null && url.endsWith(DatabaseScanTask.EXT_GEOPDF))
            {
                Uri uri = Uri.fromFile(new File(urls[0]));
                openGeoPDF(uri);

            } else {
                Toast.makeText(MainActivity.this, "url: " + urls[0], Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "url: " + urls[0], Toast.LENGTH_SHORT).show();
        }
    }

    private void openGeoPDF(Uri uri)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Filters
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void showFilterDialog()
    {
        FilterDialog filterDialog = new FilterDialog();
        filterDialog.setFilter_name(AppSettings.getFilter_byName(MainActivity.this));
        filterDialog.setFilter_state(AppSettings.getFilter_byState(MainActivity.this));
        filterDialog.setFilter_scale(AppSettings.getFilter_byScale(MainActivity.this));
        filterDialog.setAppCompatActivity(this);
        filterDialog.setFilterDialogListener(onFilterChanged);
        filterDialog.show(getSupportFragmentManager(), TAG_DIALOG_FILTERS);
    }

    private void restoreFilterDialog(FragmentManager fragments)
    {
        FilterDialog filterDialog = (FilterDialog) fragments.findFragmentByTag(TAG_DIALOG_FILTERS);
        if (filterDialog != null) {
            filterDialog.setAppCompatActivity(this);
            filterDialog.setFilterDialogListener(onFilterChanged);
        }
    }

    FilterDialog.FilterDialogListener onFilterChanged = new FilterDialog.FilterDialogListener()
    {
        public void onFilterChanged( FilterDialog dialog, String filterName )
        {
            if (filterName.equals(FilterDialog.FILTER_NAME))
            {
                Log.d(TAG, "onFilterChanged: " + filterName + ": " + dialog.getFilter_name());
                AppSettings.setFilter_byName(MainActivity.this, dialog.getFilter_name());
                initListAdapter(MainActivity.this, null, false);

            } else if (filterName.equals(FilterDialog.FILTER_STATE)) {
                Log.d(TAG, "onFilterChanged: " + filterName + ": " + dialog.getFilter_stateDisplay());
                AppSettings.setFilter_byState(MainActivity.this, dialog.getFilter_state());
                initListAdapter(MainActivity.this, null, false);

            } else if (filterName.equals(FilterDialog.FILTER_SCALE)) {
                Log.d(TAG, "onFilterChanged: " + filterName + ": " + dialog.getFilter_scale());
                AppSettings.setFilter_byScale(MainActivity.this, dialog.getFilter_scale());
                initListAdapter(MainActivity.this, null, false);
            }
        }
    };

    public void clearFilters(Context context)
    {
        AppSettings.setFilter_byName(context, "");
        AppSettings.setFilter_byState(context, new String[0]);
        AppSettings.setFilter_byScale(context, TopoIndexDatabaseAdapter.MapScale.SCALE_ANY.getValue());
        initListAdapter(context, null);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Location
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final int PERMISSION_REQUEST_LOCATION = 0;

    private void initLocation(Context context)
    {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null)
        {
            boolean autoLocation = AppSettings.getAutoLocation(context);
            if (autoLocation)
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    requestLocationPermissions();
                    return;
                }
                requestLocationUpdates(locationManager);

            } else {
                locationManager.removeUpdates(locationListener);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates(LocationManager locationManager)
    {
        Location location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location != null) {
            lastLocationAccuracy = location.getAccuracy();
            AppSettings.setLocation(MainActivity.this, location.getLatitude(), location.getLongitude());
        }
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0, 0, locationListener);
    }

    private void requestLocationPermissions()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(getString(R.string.permissions_dialog_title));
            dialog.setMessage(AboutDialog.fromHtml(getString(R.string.privacy_permission_location)));
            dialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
                }
            });
            dialog.show();

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (requestCode == PERMISSION_REQUEST_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                if (locationManager != null) {
                    requestLocationUpdates(locationManager);
                }
            }
        }
    }

    private float lastLocationAccuracy;
    private long lastLocationUpdate = 0;
    private LocationListener locationListener = new LocationListener()
    {
        public void onLocationChanged(Location location)
        {
            Calendar now = Calendar.getInstance();
            long d;
            long updateInterval = AppSettings.getLocationInterval(MainActivity.this);
            if ((d = now.getTimeInMillis() - lastLocationUpdate) > updateInterval)
            {
                lastLocationUpdate = Calendar.getInstance().getTimeInMillis();
                long maxAge = AppSettings.getLocationMaxAge(MainActivity.this);
                if (location.getAccuracy() < lastLocationAccuracy || d > maxAge)
                {
                    lastLocationAccuracy = location.getAccuracy();
                    AppSettings.setLocation(MainActivity.this, location.getLatitude(), location.getLongitude());
                    updateViews();
                }
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    private void showLocationDialog()
    {
        LocationDialog locationDialog = new LocationDialog();

        AppSettings.Location location = AppSettings.getLocation(this);
        locationDialog.setLatitude(location.getLatitude());
        locationDialog.setLongitude(location.getLongitude());
        locationDialog.setAutomaticMode(AppSettings.getAutoLocation(this));

        locationDialog.setDialogListener(onLocationDialogDismissed);
        locationDialog.show(getSupportFragmentManager(), TAG_DIALOG_LOCATION);
    }

    private void restoreLocationDialog(FragmentManager fragments)
    {
        LocationDialog locationDialog = (LocationDialog) fragments.findFragmentByTag(TAG_DIALOG_LOCATION);
        if (locationDialog != null) {
            locationDialog.setDialogListener(onLocationDialogDismissed);
        }
    }

    private LocationDialog.LocationDialogListener onLocationDialogDismissed = new LocationDialog.LocationDialogListener()
    {
        @Override
        public void onOk(LocationDialog dialog)
        {
            Log.d(TAG, "OnLocationDialogDismissed: " + dialog.getLatitude() + ", " + dialog.getLongitude() + " [automatic? " + (dialog.automaticMode() ? "true" : "false") + "]");
            AppSettings.setAutoLocation(MainActivity.this, dialog.automaticMode());
            AppSettings.setLocation(MainActivity.this, dialog.getLatitude(), dialog.getLongitude());
            initLocation(MainActivity.this);
            updateViews();
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // scanCollection
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean scanCollection()
    {
        if (databaseService.getStatus() != TopoIndexDatabaseService.STATUS_READY)
        {
            Log.w(TAG, "scanCollection: a task is already running (or pending); ignoring call...");
            return false;
        }
        return databaseService.runScanCollectionTask(MainActivity.this, scanTaskListener);
    }

    private DatabaseTaskListener scanTaskListener = new DatabaseTaskListener()
    {
        @Override
        public void onStarted()
        {
            progressSnackbar.setText(getString(R.string.database_scan_progress));
            progressSnackbar.show();
        }

        @Override
        public void onProgress(DatabaseTaskProgress... progress)
        {
            if (progressSnackbar != null && progress.length > 0) {
                progressSnackbar.setText(progress[0].getMessage());
            }
        }

        @Override
        public void onFinished(DatabaseTaskResult result)
        {
            progressSnackbar.dismiss();
            initListAdapter(MainActivity.this, TopoIndexDatabaseAdapter.TABLE_MAPS, true);
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // clearCollection
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private boolean clearCollection()
    {
        if (databaseService.getStatus() != TopoIndexDatabaseService.STATUS_READY)
        {
            Log.w(TAG, "clearCollection: a task is already running (or pending); ignoring call...");
            return false;
        }
        return databaseService.runClearCollectionTask(MainActivity.this, clearTaskListener);
    }

    private DatabaseTaskListener clearTaskListener = new DatabaseTaskListener()
    {
        @Override
        public void onStarted() { /* EMPTY */ }

        @Override
        public void onProgress(DatabaseTaskProgress... progress) { /* EMPTY */ }

        @Override
        public void onFinished(DatabaseTaskResult result)
        {
            initListAdapter(MainActivity.this, TopoIndexDatabaseAdapter.TABLE_MAPS, true);
        }
    };


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // initDatabase
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void initDatabase()
    {
        initDatabase(this, null);
    }

    private boolean initDatabase(final Context context, final Uri uri)
    {
        if (databaseService.getStatus() != TopoIndexDatabaseService.STATUS_READY)
        {
            Log.w(TAG, "initDatabase: DatabaseInitTask is already running (or pending); ignoring call...");
            return false;
        }

        android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(getString(R.string.database_update_confirm_title));
        dialog.setMessage(getString(R.string.database_update_confirm_message));

        dialog.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                if (databaseService != null) {
                    databaseService.runDatabaseInitTask(context, null, uri, initTaskListener);
                }
            }
        });
        dialog.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { /* EMPTY */ }
        });

        dialog.show();
        return true;
    }

    private DatabaseTaskListener initTaskListener = new DatabaseTaskListener()
    {
        @Override
        public void onStarted()
        {
            progressSnackbar.setText(getString(R.string.database_update_progress, ""));
            progressSnackbar.show();
        }

        @Override
        public void onProgress(DatabaseTaskProgress... progress)
        {
            if (progressSnackbar != null && progress.length > 0) {
                progressSnackbar.setText(progress[0].getMessage());
            }
        }

        @Override
        public void onFinished(DatabaseTaskResult result)
        {
            progressSnackbar.dismiss();
            initListAdapter(MainActivity.this, null, true);
        }
    };

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // DatabaseService
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private TopoIndexDatabaseAdapter database;
    private static TopoIndexDatabaseService databaseService;
    boolean boundToService = false;

    @Override
    protected void onStart()
    {
        super.onStart();
        bindService(new Intent(this, TopoIndexDatabaseService.class),
                databaseServiceConnection, Context.BIND_AUTO_CREATE);

        database = new TopoIndexDatabaseAdapter(MainActivity.this);
        database.open();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        databaseService.removeServiceListener(serviceListener);
        unbindService(databaseServiceConnection);
        boundToService = false;

        if (database != null) {
            database.close();
        }
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
            if (status == TopoIndexDatabaseService.STATUS_READY)
            {
                if (progressSnackbar != null) {
                    progressSnackbar.dismiss();
                }
                showFabs(true);
                updateViews();

            } else {
                if (progressSnackbar != null){
                    progressSnackbar.show();
                }
                DatabaseTaskProgress lastProgress = databaseService.getLastProgress();
                if (lastProgress != null) {
                    onProgress(lastProgress);
                }
                hideFabs();
            }
        }

        @Override
        public void onProgress(DatabaseTaskProgress progress)
        {
            if (progressSnackbar != null) {
                progressSnackbar.setText(progress.getMessage());
            }
            if (progressSnackbarProgress != null) {
                int n = progress.numItems();
                if (n > 0) {
                    progressSnackbarProgress.setIndeterminate(false);
                }
                progressSnackbarProgress.setMax(progress.numItems());
                progressSnackbarProgress.setProgress(progress.itemNumber());
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class TopoIndexPagerAdapter extends FragmentPagerAdapter
    {
        protected ListViewFragment listFragment = null;   // 0
        protected QuadViewFragment quadFragment = null;   // 1

        public TopoIndexPagerAdapter(FragmentManager fragments)
        {
            super(fragments);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position)
        {
            TopoIndexFragment fragment = (TopoIndexFragment) super.instantiateItem(container, position);
            if (position == 1) {
                quadFragment = (QuadViewFragment) fragment;
                quadFragment.setQuadViewFragmentListener(quadFragmentListener);

            } else {
                listFragment = (ListViewFragment) fragment;
                listFragment.setListViewFragmentListener(listFragmentListener);
            }
            return fragment;
        }

        @Override
        public Fragment getItem(int position)
        {
            TopoIndexFragment fragment;
            Bundle args = new Bundle();
            switch (position)
            {
                case 1:
                    fragment = new QuadViewFragment();
                    break;

                case 0:
                default:
                    fragment = new ListViewFragment();
                    break;
            }
            fragment.setArguments(args);
            return fragment;

        }

        @Override
        public int getCount()
        {
            return 2;
        }
    }

    private QuadViewFragment.QuadViewFragmentListener quadFragmentListener = new QuadViewFragment.QuadViewFragmentListener()
    {
        @Override
        public void onViewItem(ContentValues item)
        {
            String fromTable = (pagerAdapter.listFragment != null) ? pagerAdapter.listFragment.getCurrentTable() : TopoIndexDatabaseAdapter.TABLE_MAPS;
            openMapURL(fromTable, TopoIndexDatabaseAdapter.getUrls(item));
        }

        @Override
        public void onBrowseItem(ContentValues item)
        {
            ContentValues[] nearbyMaps = database.findNearbyMaps(item, TopoIndexDatabaseAdapter.MapScale.SCALE_24K);   // TODO: async task
            pagerAdapter.quadFragment.setContentValues(nearbyMaps);
            pagerAdapter.quadFragment.updateViews(MainActivity.this);
            pager.setCurrentItem(1);
        }

        @Override
        public void onScanCollection() {
            scanCollection();
        }

        @Override
        public void onInitDatabase() {
            initDatabase();
        }
    };

    private ListViewFragment.ListViewFragmentListener listFragmentListener = new ListViewFragment.ListViewFragmentListener()
    {
        @Override
        public void onNearbyItem( ContentValues item )
        {
            ContentValues[] nearbyMaps = database.findNearbyMaps(item, TopoIndexDatabaseAdapter.MapScale.SCALE_24K);   // TODO: async task
            pagerAdapter.quadFragment.setContentValues(nearbyMaps);
            pagerAdapter.quadFragment.updateViews(MainActivity.this);
            pager.setCurrentItem(1);
        }

        @Override
        public void onViewItem( ContentValues item )
        {
            String fromTable = (pagerAdapter.listFragment != null) ? pagerAdapter.listFragment.getCurrentTable() : TopoIndexDatabaseAdapter.TABLE_MAPS;
            openMapURL(fromTable, TopoIndexDatabaseAdapter.getUrls(item));
        }

        @Override
        public void onClearFilters() {
            clearFilters(MainActivity.this);
        }

        @Override
        public void onScanCollection() {
            scanCollection();
        }

        @Override
        public void onInitDatabase() {
            initDatabase();
        }
    };

    protected void initListAdapter(Context context, String table)
    {
        initListAdapter(context, table, true);
    }
    protected void initListAdapter(Context context, String table, boolean updateNav)
    {
        if (pagerAdapter.listFragment != null) {
            pagerAdapter.listFragment.setCurrentTable(table);
        } else Log.w(TAG, "initListAdapter: List Fragment is null!");
    }
}
