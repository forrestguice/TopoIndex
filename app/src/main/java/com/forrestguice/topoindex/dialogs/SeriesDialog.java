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
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class SeriesDialog extends DialogFragment
{
    public static final String TAG = "TopoIndexStates";

    public static final String KEY_BUNDLE = "param_bundle";
    public static final String KEY_SHOWALL = "showSelectAll";
    public static final String KEY_SHOWCANCEL = "showCancel";
    public static final String KEY_ATLEASTONE = "atLeastOne";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState != null) {
            restoreDialogState(savedInstanceState);
        }

        if (showCancelButton || showSelectAll || showClear)
        {
            builder.setPositiveButton(getString(android.R.string.ok), null);
            if (showCancelButton) {
                builder.setNegativeButton(getString(android.R.string.cancel), null);
            }
            if (showSelectAll || showClear) {
                builder.setNeutralButton(getString(showSelectAll ? R.string.select_all : R.string.select_none), null);
            }
        } else {
            builder.setNeutralButton(getString(android.R.string.ok), null);
            builder.setNeutralButtonIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_back_light));
        }

        final String[] items = getItems();
        builder.setMultiChoiceItems(getItems(), getChecked(), new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked)
            {
                series.put(items[i], isChecked);
                if (dialogListener != null) {
                    dialogListener.onSelectionChanged(getSelection());
                }
            }
        });

        final Dialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface)
            {
                Button okButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String[] selection = getSelection();
                        if (requireAtLeastOne && selection.length == 0)
                        {
                            Toast.makeText(getActivity(), getString(R.string.select_at_least_one), Toast.LENGTH_SHORT).show();

                        } else {
                            if (dialogListener != null) {
                                dialogListener.onDialogAccepted(selection);
                            }
                            dialog.dismiss();
                        }
                    }
                });

                if (showSelectAll || showClear)
                {
                    Button neutralButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    neutralButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (showSelectAll) {
                                selectAll();
                            } else {
                                clearSelection();
                            }
                        }
                    });
                }
            }
        });

        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        state.putBundle(KEY_BUNDLE, bundle);
        state.putBoolean(KEY_ATLEASTONE, requireAtLeastOne);
        state.putStringArray(FilterDialog.FILTER_SERIES, getSelection());
        state.putBoolean(KEY_SHOWCANCEL, showCancelButton);
        state.putBoolean(KEY_SHOWALL, showSelectAll);
        super.onSaveInstanceState(state);
    }

    private void restoreDialogState( @NonNull Bundle state )
    {
        bundle = state.getBundle(KEY_BUNDLE);
        requireAtLeastOne = state.getBoolean(KEY_ATLEASTONE, requireAtLeastOne);
        showSelectAll = state.getBoolean(KEY_SHOWALL, showSelectAll);
        showCancelButton = state.getBoolean(KEY_SHOWCANCEL, showCancelButton);
        setSelection(state.getStringArray(FilterDialog.FILTER_SERIES));
    }

    private String[] getItems()
    {
        String[] items = series.keySet().toArray(new String[0]);
        Arrays.sort(items, new Comparator<String>() {
            @Override
            public int compare(String s, String t1)
            {
                return s.compareTo(t1);
            }
        });
        return items;
    }

    private boolean[] getChecked()
    {
        String[] items = getItems();
        boolean[] checked = new boolean[items.length];
        for (int i=0; i<items.length; i++) {
            checked[i] = series.get(items[i]);
        }
        return checked;
    }

    private HashMap<String, Boolean> series = new HashMap<String, Boolean>();
    public String[] getSelection()
    {
        ArrayList<String> selection = new ArrayList<String>();
        for (String seriesID : series.keySet())
        {
            boolean isSelected = series.get(seriesID);
            if (isSelected) {
                selection.add(seriesID);
            }
        }
        return selection.toArray(new String[0]);
    }

    public void setSelection(String[] selection)
    {
        series.clear();
        for (String seriesID : TopoIndexDatabaseAdapter.VAL_MAP_SERIES)
        {
            boolean isSelected = false;
            if (selection != null)
            {
                for (String selected : selection)
                {
                    if (seriesID.equals(selected))
                    {
                        isSelected = true;
                        break;
                    }
                }
            }
            series.put(seriesID, isSelected);
        }
    }

    public void selectAll()
    {
        setSelection(TopoIndexDatabaseAdapter.VAL_STATES.keySet().toArray(new String[0]));
        if (dialogListener != null) {
            dialogListener.onSelectionChanged(getSelection());
        }

        AlertDialog dialog = (AlertDialog)getDialog();
        ListView listView = dialog.getListView();
        if (listView != null)
        {
            for (int i=0; i<listView.getCount(); i++) {
                listView.setItemChecked(i, true);
            }
        }
    }

    public void clearSelection()
    {
        setSelection(new String[0]);
        if (dialogListener != null) {
            dialogListener.onSelectionChanged(getSelection());
        }
        getDialog().dismiss();
    }

    private Bundle bundle;
    public void setBundle(Bundle bundle)
    {
        this.bundle = bundle;
    }
    public Bundle getBundle()
    {
        return bundle;
    }

    public boolean requireAtLeastOne = false;
    public void setRequireAtLeastOne(boolean value)
    {
        requireAtLeastOne = value;
    }

    private boolean showSelectAll = false;
    public void setShowSelectAll(boolean value) {
        showSelectAll = value;
    }

    private boolean showClear = false;
    public void setShowClear(boolean value) {
        showClear = value;
    }

    private boolean showCancelButton = false;
    public void setShowCancelButton(boolean value) {
        showCancelButton = value;
    }

    private SeriesDialogListener dialogListener;
    public void setDialogListener( SeriesDialogListener listener )
    {
        dialogListener = listener;
    }

    public static abstract class SeriesDialogListener
    {
        public void onSelectionChanged( String[] selection ) {}
        public void onDialogAccepted( String[] selection ) {}
    }

}
