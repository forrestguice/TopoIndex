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
import android.widget.TextView;

import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

public class QuadItemDialog extends BottomSheetDialogFragment
{
    public static final String TAG = "TopoIndexItem";

    public static final String KEY_CONTENTVALUES = "contentvalues";

    public static final int GRID_NORTHWEST = 0;
    public static final int GRID_NORTH = 1;
    public static final int GRID_NORTHEAST = 2;
    public static final int GRID_WEST = 3;
    public static final int GRID_CENTER = 4;
    public static final int GRID_EAST = 5;
    public static final int GRID_SOUTHWEST = 6;
    public static final int GRID_SOUTH = 7;
    public static final int GRID_SOUTHEAST = 8;

    private TextView[] gridTitles = new TextView[9];
    private TextView[] gridLines = new TextView[4];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_quaditem, container, false);
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
                    BottomSheetBehavior.from(layout).setState(BottomSheetBehavior.STATE_EXPANDED);
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
        contentValues = (ContentValues[])state.getParcelableArray(KEY_CONTENTVALUES);
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        state.putParcelableArray(KEY_CONTENTVALUES, contentValues);
        super.onSaveInstanceState(state);
    }

    private void initViews(final Context context, final View dialogContent)
    {
        int[] gridIDs = new int[] { R.id.grid1, R.id.grid2, R.id.grid3, R.id.grid4, R.id.grid5, R.id.grid6, R.id.grid7, R.id.grid8, R.id.grid9 };
        for (int i=0; i<gridIDs.length; i++)
        {
            final int j = i;
            View grid = dialogContent.findViewById(gridIDs[i]);
            grid.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (dialogListener != null && contentValues[j] != null)
                    {
                        if (j == GRID_CENTER)
                            dialogListener.onViewItem(contentValues[j]);
                        else dialogListener.onBrowseItem(contentValues[j]);
                    }
                }
            });

            gridTitles[i] = (TextView)grid.findViewById(R.id.mapItem_name);
        }

        int[] gridLineIDs = new int[] { R.id.guide1_label, R.id.guide2_label, R.id.guide3_label, R.id.guide4_label };
        for (int i=0; i<gridLineIDs.length; i++) {
            gridLines[i] = dialogContent.findViewById(gridLineIDs[i]);
        }
    }

    private void updateViews(Context context)
    {
        if (contentValues != null)
        {
            for (int i = 0; i < gridTitles.length; i++)
            {
                gridTitles[i].setText( (contentValues[i] != null) ? contentValues[i].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME) : "");
            }

            if (contentValues[GRID_CENTER] != null)
            {
                gridLines[0].setText(contentValues[GRID_CENTER].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST));
                gridLines[1].setText(contentValues[GRID_CENTER].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST));
                gridLines[2].setText(contentValues[GRID_CENTER].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH));
                gridLines[3].setText(contentValues[GRID_CENTER].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH));

            } else {
                for (int i = 0; i < gridLines.length; i++) {
                    gridLines[i].setText("");
                }
            }

        } else {
            for (int i = 0; i < gridTitles.length; i++) {
                gridTitles[i].setText("");
            }
            for (int i = 0; i < gridLines.length; i++) {
                gridLines[i].setText("");
            }
        }
    }

    private ContentValues[] contentValues;
    public void setContentValues( ContentValues[] values )
    {
        if (values.length == 9) {
            this.contentValues = values;
        }
    }

    private QuadItemDialogListener dialogListener;
    public void setQuadItemDialogListener( QuadItemDialogListener listener )
    {
        this.dialogListener = listener;
    }

    /**
     * QuadItemDialogListener
     */
    public static abstract class QuadItemDialogListener
    {
        public void onViewItem(ContentValues values) {}
        public void onBrowseItem(ContentValues values) {}
    }

}
