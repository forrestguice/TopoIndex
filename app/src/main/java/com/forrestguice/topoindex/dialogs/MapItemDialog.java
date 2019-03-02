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

package com.forrestguice.topoindex.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.util.ArrayList;

public class MapItemDialog extends BottomSheetDialogFragment
{
    public static final String TAG = "TopoIndexItem";

    public static final String KEY_CONTENTVALUES = "contentvalues";
    public static final String KEY_CONTENTVALUES_COUNT = "contentvalues_count";

    private Spinner header;
    private TextView text_nwcorner, text_secorner;
    private Button button_view, button_nearby;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_mapitem, container, false);
        initViews(getActivity(), dialogContent);
        updateViews(getActivity());
        return dialogContent;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        BottomSheetDialog dialog = (BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialog)
            {
                BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
                View layout = bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);
                if (layout != null)
                {
                    BottomSheetBehavior.from(layout).setHideable(true);
                    BottomSheetBehavior.from(layout).setSkipCollapsed(true);
                    BottomSheetBehavior.from(layout).setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        if (savedInstanceState != null) {
            restoreFromState(savedInstanceState);
        }

        return dialog;
    }

    private void restoreFromState(Bundle state)
    {
        contentValues = new ContentValues[state.getInt(KEY_CONTENTVALUES_COUNT)];
        for (int i=0; i<contentValues.length; i++) {
            contentValues[i] = state.getParcelable(KEY_CONTENTVALUES + i);
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        state.putInt(KEY_CONTENTVALUES_COUNT, contentValues.length);
        for (int i=0; i<contentValues.length; i++) {
            state.putParcelable(KEY_CONTENTVALUES + i, contentValues[i]);
        }
        super.onSaveInstanceState(state);
    }

    private void initViews(final Context context, final View dialogContent)
    {
        header = (Spinner)dialogContent.findViewById(R.id.mapItem_header);
        header.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                updateViews(context);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        initHeaderAdapter(contentValues);

        text_nwcorner = (TextView)dialogContent.findViewById(R.id.mapItem_nwcorner);
        text_secorner = (TextView)dialogContent.findViewById(R.id.mapItem_secorner);

        button_view = (Button)dialogContent.findViewById(R.id.view_button);
        button_view.setOnClickListener(onViewButtonClick);

        button_nearby = (Button)dialogContent.findViewById(R.id.nearby_button);
        button_nearby.setOnClickListener(onNearbyButtonClick);
    }

    private void updateViews(Context context)
    {
        ContentValues selectedValues = (ContentValues)header.getSelectedItem();
        if (selectedValues != null)
        {
            AppSettings.Location nwCorner = getNorthwestCorner(selectedValues);
            text_nwcorner.setText(nwCorner != null ? nwCorner.toString() : "");

            AppSettings.Location seCorner = getSoutheastCorner(selectedValues);
            text_secorner.setText(seCorner != null ? seCorner.toString() : "");

            Boolean hasMap = selectedValues.getAsBoolean(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED);
            button_view.setText(context.getString(hasMap != null && hasMap ? R.string.action_view : R.string.action_download));
        }
    }

    public static AppSettings.Location getNorthwestCorner(ContentValues values)
    {
        String latString = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH);
        String lonString = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST);
        if (latString != null && lonString != null)
        {
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(lonString);
            return new AppSettings.Location(latitude, longitude);

        } else return null;
    }

    public static AppSettings.Location getSoutheastCorner(ContentValues values)
    {
        String latString = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH);
        String lonString = values.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST);
        if (latString != null && lonString != null)
        {
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(lonString);
            return new AppSettings.Location(latitude, longitude);

        } else return null;
    }

    private View.OnClickListener onViewButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            ContentValues selectedValues = (ContentValues)header.getSelectedItem();
            if (selectedValues != null)
            {
                if (dialogListener != null) {
                    dialogListener.onViewItem(selectedValues);
                }
            }
        }
    };

    private View.OnClickListener onNearbyButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            ContentValues selectedValues = (ContentValues)header.getSelectedItem();
            if (selectedValues != null)
            {
                if (dialogListener != null) {
                    dialogListener.onNearbyItem(selectedValues);
                }
            }
        }
    };

    private void initHeaderAdapter(ContentValues[] values)
    {
        Activity activity = getActivity();
        if (header != null && activity != null) {
            MapItemDialogHeaderAdapter adapter = new MapItemDialogHeaderAdapter(activity, R.layout.map_list_item1, values);
            header.setAdapter(adapter);
            header.setSelection( (initialSelection < 0 || initialSelection >= adapter.getCount())
                    ? TopoIndexDatabaseAdapter.findFirstCollectedMap(values)
                    : initialSelection);
        }
    }

    private ContentValues[] contentValues;
    public void setContentValues(ContentValues[] values)
    {
        contentValues = values;
    }

    private int initialSelection = 0;
    public void setInitialPosition(int i) {
        initialSelection = i;
    }

    private MapItemDialogListener dialogListener;
    public void setMapItemDialogListener( MapItemDialogListener listener )
    {
        this.dialogListener = listener;
    }

    /**
     * MapItemDialogListener
     */
    public static abstract class MapItemDialogListener
    {
        public void onViewItem(ContentValues values) {}
        public void onNearbyItem(ContentValues values) {}
    }

    /**
     * MapItemDialogHeaderAdapter
     */
    public static class MapItemDialogHeaderAdapter extends ArrayAdapter<ContentValues>
    {
        private int layoutResID = R.layout.map_list_item1;
        private ContentValues[] contentValues;

        public MapItemDialogHeaderAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
            layoutResID = resource;
        }

        public MapItemDialogHeaderAdapter(@NonNull Context context, int resource, ContentValues[] values)
        {
            super(context, resource, values);
            layoutResID = resource;
            contentValues = values;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return getItemView(position, convertView, parent, true);
        }

        @Override
        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            return getItemView(position, convertView, parent, false);
        }

        private View getItemView(int i, View convertView, @NonNull ViewGroup parent, boolean colorize)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View view = layoutInflater.inflate(layoutResID, parent, false);

            View card = view.findViewById(R.id.mapItem_card);
            TextView text_name = (TextView)view.findViewById(R.id.mapItem_name);
            TextView text_series = (TextView)view.findViewById(R.id.mapItem_series);
            TextView text_state = (TextView)view.findViewById(R.id.mapItem_state);
            TextView text_date = (TextView)view.findViewById(R.id.mapItem_date);
            TextView text_scale = (TextView)view.findViewById(R.id.mapItem_scale);
            TextView text_gdaid = (TextView)view.findViewById(R.id.mapItem_gdaitemid);
            TextView text_scanid = (TextView)view.findViewById(R.id.mapItem_scanid);

            if (contentValues != null && contentValues.length > i)
            {
                text_name.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME));
                text_series.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SERIES));
                text_state.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_STATE));
                text_date.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_DATE));
                text_gdaid.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID));
                text_scanid.setText(contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCANID));

                String mapScaleValue = contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCALE);
                TopoIndexDatabaseAdapter.MapScale mapScale = TopoIndexDatabaseAdapter.MapScale.findValue(mapScaleValue);
                text_scale.setText(mapScale == TopoIndexDatabaseAdapter.MapScale.SCALE_ANY ? "" : mapScale.toString());

                if (colorize)
                {
                    Boolean isCollected = contentValues[i].getAsBoolean(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED);
                    if (isCollected != null && isCollected) {
                        card.setBackgroundColor(Color.TRANSPARENT);  // TODO
                    } else {
                        card.setBackgroundColor(Color.LTGRAY);  // TODO
                    }
                }

            } else {
                text_name.setText("");
                text_series.setText("");
                text_state.setText("");
                text_date.setText("");
                text_scale.setText("");
                text_gdaid.setText("");
                text_scanid.setText("");
            }

            return view;
        }

    }

}
