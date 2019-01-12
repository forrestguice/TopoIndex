package com.forrestguice.topoindex;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.forrestguice.topoindex.database.TopoIndexDatabaseInitTask;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    public static final String TAG = "TopoIndexActivity";

    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initDatabase();
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initDatabase()
    {
        Uri indexUri = null;  // TODO
        initDatabase(this, indexUri);
    }

    private TopoIndexDatabaseInitTask initTask;
    private boolean initDatabase(Context context, Uri uri)
    {
        if (initTask != null && !initTask.isCancelled())
        {
            switch(initTask.getStatus())
            {
                case PENDING:
                case RUNNING:
                    Log.w(TAG, "initDatabase: DatabaseInitTask is already running (or pending); ignoring call...");
                    return false;

                case FINISHED:
                default:
                    initTask = null;
                    break;
            }
        }

        initTask = new TopoIndexDatabaseInitTask(context);
        initTask.setTaskListener(initTaskListener);
        initTask.execute(uri);
        return true;
    }

    private Snackbar initSnackbar;
    private TopoIndexDatabaseInitTask.InitTaskListener initTaskListener = new TopoIndexDatabaseInitTask.InitTaskListener()
    {
        @Override
        public void onStarted()
        {
            initSnackbar = Snackbar.make(fab, "Initializing the database . . .", Snackbar.LENGTH_LONG);
            initSnackbar.setAction("Cancel", null);
            initSnackbar.show();
        }

        @Override
        public void onProgress(TopoIndexDatabaseInitTask.InitTaskProgress... progress)
        {
            // TODO
        }

        @Override
        public void onFinished(TopoIndexDatabaseInitTask.InitTaskResult result)
        {
            initSnackbar.setText("Database Initialization " + (result.getResult() ? "succeeded" : "failed"));
            initSnackbar.setAction(null, null);
        }
    };
}
