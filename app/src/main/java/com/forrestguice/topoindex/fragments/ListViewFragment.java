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
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.forrestguice.topoindex.AppSettings;

import com.forrestguice.topoindex.MainActivity;
import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;
import com.forrestguice.topoindex.dialogs.AboutDialog;
import com.forrestguice.topoindex.dialogs.MapItemDialog;

import java.text.DecimalFormat;

/**
 * ListViewFragment
 */
public class ListViewFragment extends TopoIndexFragment
{
    public static final String TAG = "TopoIndexList";

    public static final String TAG_DIALOG_MAPITEM = "mapitem";

    public static final String KEY_TABLE_CURRENT = "currentTable";

    protected ListView listView;
    private TopoIndexDatabaseCursorAdapter adapter;
    private Cursor adapterCursor;

    protected ProgressBar progressBar;
    protected TextView listCount, listTitle;
    protected TextView emptyListTitle, emptyListMessage0, emptyListMessage1;

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
        FragmentManager fragments = getChildFragmentManager();
        restoreMapItemDialog(fragments);
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

        View emptyView = content.findViewById(R.id.list_maps_empty);
        if (emptyView != null) {
            listView.setEmptyView(emptyView);
        }

        progressBar = (ProgressBar) content.findViewById(R.id.progress_list_maps);
    }

    private void initListAdapter(Context context, final String table)
    {
        initEmptyView(context, table);
        initListTitle(context, table);
        initListClick(context, table);

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
                    boolean clickHandled = false;
                    if (fragmentListener != null) {
                        clickHandled = fragmentListener.onListItemClick(adapterView, view, position, rowID);
                    }
                    if (!clickHandled) {
                        showMapItemDialog(context, adapterView, position);
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

        public void onClearFilters() {}
        public void onNearbyItem( ContentValues item ) {}
        public void onViewItem(ContentValues item) {}
    }

    private ListViewFragmentListener fragmentListener;
    public void setListViewFragmentListener( ListViewFragmentListener listener ) {
        fragmentListener = listener;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Map Item
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void showMapItemDialog(Context context, AdapterView<?> adapterView, int position)
    {
        Cursor cursor = (Cursor)adapterView.getItemAtPosition(position);
        if (cursor != null)
        {
            ContentValues contentValues = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(cursor, contentValues);

            MapItemDialog itemDialog = new MapItemDialog();
            itemDialog.setContentValues( contentValues );
            itemDialog.setMapItemDialogListener(onMapItem);
            itemDialog.show(getChildFragmentManager(), TAG_DIALOG_MAPITEM);
        }
    }

    private void dismissMapItemDialog()
    {
        MapItemDialog dialog = (MapItemDialog) getChildFragmentManager().findFragmentByTag(TAG_DIALOG_MAPITEM);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void restoreMapItemDialog(FragmentManager fragments)
    {
        MapItemDialog dialog = (MapItemDialog) fragments.findFragmentByTag(TAG_DIALOG_MAPITEM);
        if (dialog != null) {
            dialog.setMapItemDialogListener(onMapItem);
        }
    }

    private MapItemDialog.MapItemDialogListener onMapItem = new MapItemDialog.MapItemDialogListener()
    {
        @Override
        public void onNearbyItem(ContentValues item)
        {
            if (fragmentListener != null) {
                fragmentListener.onNearbyItem(item);
            }
            dismissMapItemDialog();
        }

        @Override
        public void onViewItem(ContentValues item)
        {
            if (fragmentListener != null) {
                fragmentListener.onViewItem(item);
            }
        }
    };

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
                return database.getMaps(table, 0, false, AppSettings.getFilters(getActivity()));

            } else {
                table = TopoIndexDatabaseAdapter.TABLE_MAPS_HTMC;
                return database.getMaps_HTMC(0, false);
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
