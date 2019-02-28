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

import com.forrestguice.topoindex.R;
import com.forrestguice.topoindex.database.TopoIndexDatabaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class StatesDialog extends DialogFragment
{
    public static final String TAG = "TopoIndexStates";

    public static final String KEY_SHOWCANCEL = "showCancel";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (savedInstanceState != null) {
            restoreDialogState(savedInstanceState);
        }

        if (showCancelButton)
        {
            builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (dialogListener != null) {
                        dialogListener.onDialogAccepted(getSelection());
                    }
                }
            });
            builder.setNegativeButton(getString(android.R.string.cancel), null);

        } else {
            builder.setNeutralButton(getString(android.R.string.ok), null);
            builder.setNeutralButtonIcon(ContextCompat.getDrawable(getActivity(), R.drawable.ic_back_light));
        }

        final String[] items = getItems();
        builder.setMultiChoiceItems(getItems(), getChecked(), new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked)
            {
                states.put(items[i], isChecked);
                if (dialogListener != null) {
                    dialogListener.onSelectionChanged(getSelection());
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        state.putStringArray(FilterDialog.FILTER_STATE, getSelection());
        state.putBoolean(KEY_SHOWCANCEL, showCancelButton);
        super.onSaveInstanceState(state);
    }

    private void restoreDialogState( @NonNull Bundle state )
    {
        showCancelButton = state.getBoolean(KEY_SHOWCANCEL, showCancelButton);
        setSelection(state.getStringArray(FilterDialog.FILTER_STATE));
    }

    private String[] getItems()
    {
        String[] items = states.keySet().toArray(new String[0]);
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
            checked[i] = states.get(items[i]);
        }
        return checked;
    }

    private HashMap<String, Boolean> states = new HashMap<String, Boolean>();
    public String[] getSelection()
    {
        ArrayList<String> selection = new ArrayList<String>();
        for (String state : states.keySet())
        {
            boolean isSelected = states.get(state);
            if (isSelected) {
                selection.add(state);
            }
        }
        return selection.toArray(new String[0]);
    }

    public void setSelection(String[] selection)
    {
        states.clear();
        for (String state : TopoIndexDatabaseAdapter.VAL_STATES.keySet())
        {
            boolean isSelected = false;
            if (selection != null)
            {
                for (String selected : selection)
                {
                    if (state.equals(selected))
                    {
                        isSelected = true;
                        break;
                    }
                }
            }
            states.put(state, isSelected);
        }
    }

    private boolean showCancelButton = false;
    public void setShowCancelButton(boolean value) {
        showCancelButton = value;
    }

    private StatesDialogListener dialogListener;
    public void setDialogListener( StatesDialogListener listener )
    {
        dialogListener = listener;
    }

    public static abstract class StatesDialogListener
    {
        public void onSelectionChanged( String[] selection ) {}
        public void onDialogAccepted( String[] selection ) {}
    }

}
