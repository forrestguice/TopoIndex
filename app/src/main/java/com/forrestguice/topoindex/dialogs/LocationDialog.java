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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.forrestguice.topoindex.AppSettings;
import com.forrestguice.topoindex.R;

public class LocationDialog extends DialogFragment
{
    public static final String TAG = "TopoIndexLocation";

    private Switch switch_automatic;

    private TextView label_latitude;
    private EditText edit_latitude;
    private String latitude;

    private TextView label_longitude;
    private EditText edit_longitude;
    private String longitude;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final Activity myParent = getActivity();
        LayoutInflater inflater = myParent.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogContent = inflater.inflate(R.layout.layout_dialog_location, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(myParent);
        builder.setTitle(myParent.getString(R.string.location_dialog_title));
        builder.setView(dialogContent);

        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", (DialogInterface.OnClickListener)null);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Apply", (DialogInterface.OnClickListener)null);

        dialog.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (validateInput()) {
                            if (dialogListener != null) {
                                dialogListener.onOk(LocationDialog.this);
                            }
                            dialog.dismiss();
                        }
                    }
                });

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (dialogListener != null) {
                            dialogListener.onCancel(LocationDialog.this);
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        initViews(getActivity(), dialogContent);
        if (savedInstanceState != null)
        {
            edit_latitude.setText(savedInstanceState.getString(AppSettings.KEY_LOCATION_LAT));
            edit_longitude.setText(savedInstanceState.getString(AppSettings.KEY_LOCATION_LON));
        }

        return dialog;
    }

    public void initViews(Context context, View dialogContent)
    {
        label_latitude = (TextView)dialogContent.findViewById(R.id.location_latitude_label);
        edit_latitude = (EditText)dialogContent.findViewById(R.id.location_latitude);
        edit_latitude.setText(latitude);

        label_longitude = (TextView)dialogContent.findViewById(R.id.location_longitude_label);
        edit_longitude = (EditText)dialogContent.findViewById(R.id.location_longitude);
        edit_longitude.setText(longitude);

        switch_automatic = (Switch)dialogContent.findViewById(R.id.location_mode);
        switch_automatic.setOnCheckedChangeListener(onModeChanged);
    }

    private CompoundButton.OnCheckedChangeListener onModeChanged = new CompoundButton.OnCheckedChangeListener()
    {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean automatic)
        {
            label_latitude.setEnabled(!automatic);
            edit_latitude.setEnabled(!automatic);

            label_longitude.setEnabled(!automatic);
            edit_longitude.setEnabled(!automatic);
        }
    };

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(AppSettings.KEY_LOCATION_LAT, edit_latitude.getText().toString());
        outState.putString(AppSettings.KEY_LOCATION_LON, edit_longitude.getText().toString());
    }

    public boolean automaticMode()
    {
        return switch_automatic.isChecked();
    }
    public void setAutomaticMode( boolean automaticMode )
    {
        if (switch_automatic != null) {
            switch_automatic.setChecked(automaticMode);
        }
    }

    public boolean validateInput()
    {
        boolean isValid = true;

        try {
            Double.parseDouble(edit_latitude.getText().toString());

        } catch (NumberFormatException e) {
            isValid = false;
            edit_latitude.setError("Invalid latitude!");  // TODO: strings
        }

        try {
            Double.parseDouble(edit_longitude.getText().toString());

        } catch (NumberFormatException e) {
            isValid = false;
            edit_latitude.setError("Invalid longitude!");  // TODO: strings
        }

        return isValid;
    }

    public double getLatitude()
    {
        try {
            return Double.parseDouble(edit_latitude.getText().toString());
        } catch (NumberFormatException e) {
            return Double.POSITIVE_INFINITY;
        }
    }
    public void setLatitude(double latitude)
    {
        this.latitude = Double.toString(latitude);  // TODO: format
        if (edit_latitude != null) {
            edit_latitude.setText(this.latitude);
        }
    }

    public double getLongitude()
    {
        try {
            return Double.parseDouble(edit_longitude.getText().toString());
        } catch (NumberFormatException e) {
            return Double.POSITIVE_INFINITY;
        }
    }
    public void setLongitude(double longitude)
    {
        this.longitude = Double.toString(longitude);  // TODO: format
        if (edit_longitude != null) {
            edit_longitude.setText(this.longitude);
        }
    }

    public static abstract class LocationDialogListener
    {
        public void onOk(LocationDialog dialog) {}
        public void onCancel(LocationDialog dialog) {}
    }

    private LocationDialogListener dialogListener = null;
    public void setDialogListener( LocationDialogListener listener ) {
        this.dialogListener = listener;
    }

}
