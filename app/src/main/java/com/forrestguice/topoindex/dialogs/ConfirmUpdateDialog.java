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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.forrestguice.topoindex.R;

public class ConfirmUpdateDialog extends DialogFragment
{
    public static final String TAG = "TopoIndexConfirm";

    public static final String KEY_URI = "param_uri";
    public static final String KEY_STATES = "param_states";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            states = savedInstanceState.getStringArray(KEY_STATES);
            String uriString = savedInstanceState.getString(KEY_URI);
            if (uriString != null) {
                uri = Uri.parse(uriString);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.database_update_confirm_title));
        builder.setMessage(getString(R.string.database_update_confirm_message));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogListener != null) {
                    dialogListener.onConfirmed();
                }
            }
        });
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState( Bundle outState)
    {
        if (uri != null) {
            outState.putString(KEY_URI, uri.toString());
        }
        outState.putStringArray(KEY_STATES, states);
        super.onSaveInstanceState(outState);
    }

    private String[] states;
    public void setFilter_states(String[] values)
    {
        states = values;
    }
    public String[] getFilter_states()
    {
        return states;
    }

    private Uri uri;
    public void setUri(Uri value)
    {
        uri = value;
    }
    public Uri getUri()
    {
        return uri;
    }

    private ConfirmDialogListener dialogListener;
    public void setDialogListener( ConfirmDialogListener listener )
    {
        dialogListener = listener;
    }

    public static abstract class ConfirmDialogListener
    {
        public void onConfirmed() {}
    }
}
