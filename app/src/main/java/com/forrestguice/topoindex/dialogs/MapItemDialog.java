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

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

public class MapItemDialog extends BottomSheetDialogFragment
{
    public static final String TAG = "TopoIndexItem";

    public static final String KEY_CONTENTVALUES = "contentvalues";

    private TextView text_name, text_series, text_state, text_date, text_scale, text_nwcorner, text_secorner, text_gdaid, text_scanid, text_cellid;
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
        contentValues = state.getParcelable(KEY_CONTENTVALUES);
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        state.putParcelable(KEY_CONTENTVALUES, contentValues);
        super.onSaveInstanceState(state);
    }

    private void initViews(final Context context, final View dialogContent)
    {
        text_name = (TextView)dialogContent.findViewById(R.id.mapItem_name);
        text_series = (TextView)dialogContent.findViewById(R.id.mapItem_series);
        text_state = (TextView)dialogContent.findViewById(R.id.mapItem_state);
        text_date = (TextView)dialogContent.findViewById(R.id.mapItem_date);
        text_scale = (TextView)dialogContent.findViewById(R.id.mapItem_scale);
        text_nwcorner = (TextView)dialogContent.findViewById(R.id.mapItem_nwcorner);
        text_secorner = (TextView)dialogContent.findViewById(R.id.mapItem_secorner);
        text_gdaid = (TextView)dialogContent.findViewById(R.id.mapItem_gdaitemid);
        text_scanid = (TextView)dialogContent.findViewById(R.id.mapItem_scanid);
        text_cellid = (TextView)dialogContent.findViewById(R.id.mapItem_cellid);

        button_view = (Button)dialogContent.findViewById(R.id.view_button);
        button_view.setOnClickListener(onViewButtonClick);

        button_nearby = (Button)dialogContent.findViewById(R.id.nearby_button);
        button_nearby.setOnClickListener(onNearbyButtonClick);
    }

    private void updateViews(Context context)
    {
        if (contentValues != null)
        {
            text_name.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME));
            text_series.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SERIES));
            text_state.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_STATE));
            text_date.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_DATE));
            text_scale.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCALE));

            AppSettings.Location nwCorner = getNorthwestCorner(contentValues);
            text_nwcorner.setText(nwCorner != null ? nwCorner.toString() : "");

            AppSettings.Location seCorner = getSoutheastCorner(contentValues);
            text_secorner.setText(seCorner != null ? seCorner.toString() : "");

            text_gdaid.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_GDAITEMID));
            text_scanid.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_SCANID));
            text_cellid.setText(contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_CELLID));
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
            if (contentValues != null)
            {
                String[] urls = new String[] { contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_URL),
                                               contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_URL1),
                                               contentValues.getAsString(TopoIndexDatabaseAdapter.KEY_MAP_URL2) };

                if (dialogListener != null) {
                    dialogListener.onViewItem(contentValues, urls);
                }
            }
        }
    };

    private View.OnClickListener onNearbyButtonClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            if (contentValues != null)
            {
                if (dialogListener != null) {
                    dialogListener.onNearbyItem(contentValues);
                }
            }
        }
    };

    private ContentValues contentValues;
    public void setContentValues(ContentValues values)
    {
        contentValues = values;
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
        public void onViewItem(ContentValues values, String[] urls) {}
        public void onNearbyItem(ContentValues values) {}
    }

}
