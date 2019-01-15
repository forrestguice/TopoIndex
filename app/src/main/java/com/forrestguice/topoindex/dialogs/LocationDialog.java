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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.forrestguice.topoindex.R;

public class LocationDialog extends DialogFragment
{
    public static final String TAG = "TopoIndexLocation";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogContent = inflater.inflate(R.layout.layout_dialog_location, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setView(dialogContent);
        AlertDialog dialog = builder.create();
        /**dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                dialogContent.post(new Runnable() {
                    @Override
                    public void run() { // TODO }
                });
            }
        });*/

        initViews(getActivity(), dialogContent);
        return dialog;
    }

    public void initViews(Context context, View dialogContent)
    {
        // TODO
    }

    public boolean automaticMode()
    {
        // TODO
        return false;
    }

    public boolean validateInput()
    {
        // TODO
        return false;
    }

    public double getLatitude()
    {
        // TODO
        return Double.POSITIVE_INFINITY;
    }

    public double getLongitude()
    {
        //
        return Double.POSITIVE_INFINITY;
    }

}
