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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

public class QuadViewFragment extends TopoIndexFragment
{
    public static final String TAG = "TopoIndexQuads";
    public static final String KEY_CONTENTVALUES = "contentvalues";

    private View[] gridCards = new View[9];
    private TextView[] gridTitles = new TextView[9];
    private TextView[] gridLines = new TextView[4];

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        if (contentValues != null) {
            for (int i = 0; i < contentValues.length; i++) {
                state.putParcelableArray(KEY_CONTENTVALUES + i, contentValues[i]);
            }
        }
        super.onSaveInstanceState(state);
    }

    private void restoreFromState(Bundle state)
    {
        contentValues = new ContentValues[9][];
        for (int i=0; i<contentValues.length; i++) {
            contentValues[i] = (ContentValues[])state.getParcelableArray(KEY_CONTENTVALUES + i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View contentView = inflater.inflate(R.layout.layout_fragment_topoquad, container, false);
        initViews(getActivity(), contentView);
        updateViews(getActivity());
        return contentView;
    }

    protected void initViews(Context context, View contentView)
    {
        int[] gridIDs = new int[] { R.id.grid1, R.id.grid2, R.id.grid3, R.id.grid4, R.id.grid5, R.id.grid6, R.id.grid7, R.id.grid8, R.id.grid9 };
        for (int i=0; i<gridIDs.length; i++)
        {
            final int j = i;
            View grid = contentView.findViewById(gridIDs[i]);
            grid.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (fragmentListener != null && contentValues[j] != null)
                    {
                        if (j == TopoIndexDatabaseAdapter.GRID_CENTER)
                            fragmentListener.onViewItem(contentValues[j][0]);
                        else fragmentListener.onBrowseItem(contentValues[j][0]);
                    }
                }
            });
            grid.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view)
                {
                    if (fragmentListener != null && contentValues[j] != null && contentValues[j].length > 0) {
                        fragmentListener.onBrowseAndViewItem(contentValues[j][0]);       // TODO: always show index 0?
                        return true;
                    }
                    return false;
                }
            });

            gridCards[i] = grid.findViewById(R.id.mapItem_card);
            gridTitles[i] = (TextView)grid.findViewById(R.id.mapItem_name);
        }

        int[] gridLineIDs = new int[] { R.id.guide1_label, R.id.guide2_label, R.id.guide3_label, R.id.guide4_label };
        for (int i=0; i<gridLineIDs.length; i++) {
            gridLines[i] = contentView.findViewById(gridLineIDs[i]);
        }
    }

    @Override
    public void updateViews( Context context )
    {
        if (contentValues != null)
        {
            int j = 0;
            for (int i = 0; i < gridTitles.length; i++)
            {
                gridTitles[i].setText( (contentValues[i] != null && contentValues[i].length > j) ? contentValues[i][j].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_NAME) : "");
            }

            if (contentValues[TopoIndexDatabaseAdapter.GRID_CENTER] != null)
            {
                gridLines[0].setText(contentValues[TopoIndexDatabaseAdapter.GRID_CENTER][0].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_WEST));
                gridLines[1].setText(contentValues[TopoIndexDatabaseAdapter.GRID_CENTER][0].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LONGITUDE_EAST));
                gridLines[2].setText(contentValues[TopoIndexDatabaseAdapter.GRID_CENTER][0].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_NORTH));
                gridLines[3].setText(contentValues[TopoIndexDatabaseAdapter.GRID_CENTER][0].getAsString(TopoIndexDatabaseAdapter.KEY_MAP_LATITUDE_SOUTH));

            } else {
                for (int i = 0; i < gridLines.length; i++) {
                    gridLines[i].setText("");
                }
            }

            for (int i=0; i<contentValues.length; i++)
            {
                View card = gridCards[i];
                if (card != null)
                {
                    if (quadIsCollected(contentValues[i])) {
                        card.setBackgroundColor(Color.WHITE);  // TODO
                    } else {
                        card.setBackgroundColor(Color.LTGRAY);  // TODO
                    }
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

    private boolean quadIsCollected(ContentValues[] entries)
    {
        if (entries == null || entries.length == 0) {
            return false;
        }

        for (ContentValues entry : entries)
        {
            Boolean inCollection = entry.getAsBoolean(TopoIndexDatabaseAdapter.KEY_MAP_ISCOLLECTED);
            if (inCollection != null && inCollection) {
                return true;
            }
        }
        return false;
    }

    private ContentValues[][] contentValues;
    public void setContentValues( ContentValues[][] values )
    {
        if (values.length == 9) {
            this.contentValues = values;
        }
    }

    /**
     * QuadViewFragmentListener
     */
    public static abstract class QuadViewFragmentListener extends TopoIndexFragmentListener
    {
        public void onViewItem(ContentValues values) {}
        public void onBrowseItem(ContentValues values) {}
        public void onBrowseAndViewItem(ContentValues values) {}
    }

    protected QuadViewFragmentListener fragmentListener;
    public void setQuadViewFragmentListener( QuadViewFragmentListener listener ) {
        fragmentListener = listener;
    }
}
