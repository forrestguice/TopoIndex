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

package com.forrestguice.topoindex.fragments;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.topoindex.AppSettings;

import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;
import com.forrestguice.topoindex.dialogs.AboutDialog;
import com.forrestguice.topoindex.dialogs.FilterDialog;
import com.forrestguice.topoindex.dialogs.StatesDialog;

import java.text.DecimalFormat;

import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_CELLID;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_DATE;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_NAME;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_SCALE;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_SCANID;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_SERIES;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_STATE;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_URL2;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_VERSION;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_ROWID;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_URL;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_URL1;
import static com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED;

/**
 * ListViewFragment
 */
public class ListViewFragment extends TopoIndexFragment
{
    public static final String TAG = "TopoIndexList";
    public static final String KEY_TABLE_CURRENT = "currentTable";

    protected ListView listView;
    private TopoIndexDatabaseCursorAdapter adapter;
    private Cursor adapterCursor;

    protected ProgressBar progressBar;
    protected TextView listCount, listTitle;
    protected TextView emptyListTitle, emptyListMessage0, emptyListMessage1;
    protected TextView filterDesc, filterDescState, filterDescScale;
    protected View filterDescLayout;

    private int resID_background_collected0 = R.drawable.background_grid_collected0;
    private int resID_background_notCollected0 = R.drawable.background_grid_notcollected0;

    private FloatingActionButton fabFilters;
    private FloatingActionButton[] fabs = new FloatingActionButton[0];

    private TopoIndexDatabaseAdapter database;
    private String currentTable = TopoIndexDatabaseAdapter.TABLE_MAPS;
    public void setCurrentTable( String table )
    {
        if (table != null) {
            currentTable = table;
        }
        initListAdapter(getActivity(), currentTable);
    }
    public String getCurrentTable()
    {
        return currentTable;
    }

    @Override
    public void onCreate(Bundle savedState)
    {
        super.onCreate(savedState);
        if (savedState != null) {
            currentTable = savedState.getString(ListViewFragment.KEY_TABLE_CURRENT, currentTable);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View contentView = inflater.inflate(R.layout.layout_fragment_topolist, container, false);
        initViews(getActivity(), contentView);
        return contentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        database = new TopoIndexDatabaseAdapter(getActivity());
        database.open();
        initListAdapter(getActivity(), currentTable);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (database != null)
        {
            if (adapterCursor != null) {
                adapterCursor.close();
            }
            database.close();
        }
    }

    @Override
    public void onSaveInstanceState( Bundle outState)
    {
        outState.putString(ListViewFragment.KEY_TABLE_CURRENT, currentTable);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews*
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initViews(Context context, View content)
    {
        listView = (ListView) content.findViewById(R.id.list_maps);
        listCount = (TextView) content.findViewById(R.id.text_resultcount);
        listTitle = (TextView) content.findViewById(R.id.title_maps);
        emptyListTitle = (TextView) content.findViewById(R.id.list_maps_empty_title);
        emptyListMessage0 = (TextView) content.findViewById(R.id.list_maps_empty_message);
        emptyListMessage1 = (TextView) content.findViewById(R.id.list_maps_empty_message1);

        filterDesc = (TextView) content.findViewById(R.id.filterdesc_name);
        filterDescState = (TextView) content.findViewById(R.id.filterdesc_states);
        filterDescScale = (TextView) content.findViewById(R.id.filterdesc_scale);
        filterDescLayout = content.findViewById(R.id.footer_maps_layout);

        View emptyView = content.findViewById(R.id.list_maps_empty);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }

        progressBar = (ProgressBar) content.findViewById(R.id.progress_list_maps);

        fabFilters = (FloatingActionButton) content.findViewById(R.id.fab_filters);
        fabFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (fragmentListener != null) {
                    fragmentListener.onShowFilters();
                }
            }
        });

        /**fabFiltersClear = (FloatingActionButton) content.findViewById(R.id.fab_filters_clear);
        fabFiltersClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                boolean cleared = false;
                if (fragmentListener != null) {
                    cleared = fragmentListener.onClearFilters();
                }
                if (cleared) {
                    fabFiltersClear.hide();
                }
            }
        });*/

        fabs = new FloatingActionButton[] { fabFilters };
    }

    private void initListAdapter(Context context, final String table)
    {
        initEmptyView(context, table);
        initListTitle(context, table);
        initListClick(context, table);
        initListFooter(context, table);

        ListAdapterTask task = new ListAdapterTask();
        task.execute(table);
    }

    private void initListClick(final Context context, final String table)
    {
        if (listView != null)
        {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowID)
                {
                    if (fragmentListener != null) {
                        fragmentListener.onListItemClick(adapterView, view, position, rowID);
                    }
                }
            });
        }
    }

    private void initListTitle(Context context, String table)
    {
        if (listTitle != null)
        {
            if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC))
                listTitle.setText(getString(R.string.nav_item_usgs_htmc));
            else if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS_USTOPO))
                listTitle.setText(getString(R.string.nav_item_usgs_ustopo));
            else listTitle.setText(getString(R.string.nav_item_locallist));
        }
    }

    private void initListFooter(Context context, String table)
    {
        if (filterDescScale != null)
        {
            String filterByScale = AppSettings.getFilter_byScale(context);
            TopoIndexDatabaseAdapter.MapScale mapScale = TopoIndexDatabaseAdapter.MapScale.findValue(filterByScale);
            filterDescScale.setText(mapScale == TopoIndexDatabaseAdapter.MapScale.SCALE_ANY ? filterByScale : mapScale.toString());
            filterDescScale.setVisibility( filterByScale.isEmpty() ? View.GONE : View.VISIBLE );
        }

        if (filterDescState != null)
        {
            String[] filterByState = AppSettings.getFilter_byState(context);
            filterDescState.setText(FilterDialog.getStateDisplay(filterByState, ""));
            filterDescState.setVisibility( filterByState.length == 0 ? View.GONE : View.VISIBLE );
        }

        if (filterDesc != null)
        {
            String filterByName = AppSettings.getFilter_byName(context);
            filterDesc.setText(filterByName);
        }

        if (filterDescLayout != null) {
            filterDescLayout.setVisibility( AppSettings.hasNoFilters(context) ? View.GONE : View.VISIBLE );
        }
    }

    private void initEmptyView(Context context, final String table)
    {
        if (emptyListTitle != null)
        {
            if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS))
                emptyListTitle.setText(getString(R.string.list_empty_maps));
            else emptyListTitle.setText(getString(R.string.list_empty_index));
        }

        if (emptyListMessage0 != null)
        {
            if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS)) {
                emptyListMessage0.setText(AboutDialog.fromHtml(getString(R.string.list_empty_message_scan)));
            } else {
                emptyListMessage0.setText(AboutDialog.fromHtml(getString(R.string.list_empty_message_update)));
            }

            //emptyListMessage0.setVisibility(database.hasMaps(table) ? View.GONE : View.VISIBLE);
            emptyListMessage0.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (fragmentListener != null) {
                        if (table.equals(TopoIndexDatabaseAdapter.TABLE_MAPS))
                            fragmentListener.onScanCollection();
                        else fragmentListener.onInitDatabase();
                    }
                }
            });
        }

        if (emptyListMessage1 != null)
        {
            emptyListMessage1.setVisibility(AppSettings.hasNoFilters(context) ? View.GONE : View.VISIBLE);
            emptyListMessage1.setText(AboutDialog.fromHtml(getString(R.string.list_empty_message_clear)));
            emptyListMessage1.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view) {
                    if (fragmentListener != null) {
                        fragmentListener.onClearFilters();
                    }
                }
            });
        }
    }

    /**
     * ListViewFragmentListener
     */
    public static abstract class ListViewFragmentListener extends TopoIndexFragmentListener
    {
        /**
         * @return true click consumed, false propagate click
         */
        public boolean onListItemClick(AdapterView<?> adapterView, View view, int position, long rowID) { return false; }

        public boolean onShowFilters() { return false; }
        public boolean onClearFilters() { return false; }
        public void onNearbyItem( ContentValues item ) {}
        public boolean onViewItem(ContentValues item) { return false; }
    }

    private ListViewFragmentListener fragmentListener;
    public void setListViewFragmentListener( ListViewFragmentListener listener ) {
        fragmentListener = listener;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Floating Action Buttons
    /////////////////////////////////////////////////////////////////////

    public void showFabs(boolean withDelay)
    {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                fabFilters.setEnabled(true);
                if (!fabFilters.isShown()) {
                    fabFilters.show();
                }
            }
        }, withDelay ? 750 : 0);

        /**new Handler().postDelayed(new Runnable() {
            public void run() {
                fabFiltersClear.setEnabled(true);
                if (AppSettings.hasNoFilters(getActivity()))
                    fabFiltersClear.hide();
                else fabFiltersClear.show();
            }
        }, withDelay ? 1250 : 0);*/
    }

    public void hideFabs()
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * ListAdapterTask
     */
    private class ListAdapterTask extends AsyncTask<String, Void, Cursor>
    {
        private String table;

        @Override
        protected void onPreExecute()
        {
            progressBar.setVisibility(View.VISIBLE);
            listCount.setText("");
        }

        @Override
        protected Cursor doInBackground(String... tables)
        {
            if (tables.length > 0 && tables[0] != null)
            {
                table = tables[0];
                return database.getMaps(table, 0, TopoIndexDatabaseAdapter.QUERY_MAPS_MINENTRY, AppSettings.getFilters(getActivity()));

            } else {
                table = TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC;
                return database.getMaps(TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC, 0, TopoIndexDatabaseAdapter.QUERY_MAPS_MINENTRY, AppSettings.getFilters(getActivity()));
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor)
        {
            progressBar.setVisibility(View.GONE);

            if (adapterCursor != null) {
                adapterCursor.close();
            }
            adapterCursor = cursor;

            adapter = new TopoIndexDatabaseCursorAdapter(getActivity(), cursor);
            listCount.setText( new DecimalFormat("#,###,###").format(adapter.getCount()) );

            currentTable = table;
            if (listView != null) {
                listView.setAdapter(adapter);
            }
            //showFabs(false); // TODO
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * DatabaseCursorAdapter
     */
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
            itemName.setText(cursor.getString(cursor.getColumnIndex(KEY_MAP_NAME)));

            TextView itemState = (TextView)view.findViewById(R.id.mapItem_state);
            setText(itemState, KEY_MAP_STATE, cursor);

            TextView itemDate = (TextView)view.findViewById(R.id.mapItem_date);
            setText(itemDate, KEY_MAP_DATE, cursor);

            TextView itemSeries = (TextView)view.findViewById(R.id.mapItem_series);
            setText(itemSeries, KEY_MAP_SERIES, cursor);

            TextView itemScale = (TextView)view.findViewById(R.id.mapItem_scale);
            int scaleIndex = cursor.getColumnIndex(KEY_MAP_SCALE);
            if (scaleIndex != -1)
            {
                String scaleValue = cursor.getString(scaleIndex);
                TopoIndexDatabaseAdapter.MapScale scale = TopoIndexDatabaseAdapter.MapScale.findValue(scaleValue);
                itemScale.setText((scale == TopoIndexDatabaseAdapter.MapScale.SCALE_ANY) ? scaleValue : scale.toString());
            }

            View card = view.findViewById(R.id.mapItem_card);
            if (card != null)
            {
                boolean inCollection = false;
                int inCollectionIndex = cursor.getColumnIndex(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED);
                if (inCollectionIndex != -1)
                {
                    String boolValue = cursor.getString(inCollectionIndex);
                    if (boolValue != null)
                        inCollection = (boolValue.toLowerCase().equals("true"));
                    else inCollection = false;
                }
                card.setBackgroundResource( inCollection ? resID_background_collected0 : resID_background_notCollected0 );
            }
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
