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
    public static final String KEY_BUNDLE = "param_bundle";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            bundle = savedInstanceState.getBundle(KEY_BUNDLE);
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
        outState.putBundle(KEY_BUNDLE, bundle);
        super.onSaveInstanceState(outState);
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
