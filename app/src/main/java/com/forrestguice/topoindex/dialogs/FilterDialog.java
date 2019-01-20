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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.forrestguice.topoindex.R;

public class FilterDialog extends BottomSheetDialogFragment
{
    public static final String TAG = "TopoIndexFilter";

    public static final String FILTER_NAME = "nameFilter";
    public static final String FILTER_STATE = "stateFilter";
    public static final String FILTER_SCALE = "scaleFilter";

    private EditText edit_filterName;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View dialogContent = inflater.inflate(R.layout.layout_dialog_filters, container, false);
        initViews(getActivity(), dialogContent);
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
                    BottomSheetBehavior.from(layout).setSkipCollapsed(false);
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
        if (edit_filterName != null) {
            edit_filterName.setText(state.getString(FILTER_NAME, ""));
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        if (edit_filterName != null) {
            state.putString(FILTER_NAME, edit_filterName.getText().toString());
        }
        super.onSaveInstanceState(state);
    }

    public void initViews(Context context, final View dialogContent)
    {
        edit_filterName = (EditText) dialogContent.findViewById(R.id.edit_filter_name);
        if (edit_filterName != null)
        {
            edit_filterName.setText(initialFilterName);
            edit_filterName.setOnEditorActionListener(new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent)
                {
                    if (action == EditorInfo.IME_ACTION_DONE)
                    {
                        if (dialogListener != null) {
                            dialogListener.onFilterChanged(FilterDialog.this, FILTER_NAME);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private String initialFilterName;
    public void setFilter_name(String value)
    {
        initialFilterName = value;
        if (edit_filterName != null) {
            edit_filterName.setText(value);
        }
    }
    public String getFilter_name()
    {
        if (edit_filterName != null)
            return edit_filterName.getText().toString();
        else return "";
    }

    public void setFilter_state(String value)
    {
        // TODO
    }
    public String getFilter_state()
    {
        return "";  // TODO
    }

    public void setFilter_scale(String value)
    {
        // TODO
    }
    public String getFilter_scale()
    {
        return "";  // TODO
    }

    private FilterDialogListener dialogListener;
    public void setFilterDialogListener( FilterDialogListener listener )
    {
        this.dialogListener = listener;
    }

    /**
     * FilterDialogListener
     */
    public static abstract class FilterDialogListener
    {
        public void onFilterChanged( FilterDialog dialog, String filterName ) {}
    }

}
