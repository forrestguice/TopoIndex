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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.forrestguice.topoindex.R;

import org.w3c.dom.Text;

public class FilterDialog extends BottomSheetDialogFragment
{
    public static final String TAG = "TopoIndexFilter";

    public static final String TAG_DIALOG_STATES = "statesDialog";

    public static final String FILTER_NAME = "nameFilter";
    public static final String FILTER_STATE = "stateFilter";
    public static final String FILTER_SCALE = "scaleFilter";

    private EditText edit_filterName;
    private TextView label_filterState, text_filterState;

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
        if (edit_filterName != null) {
            edit_filterName.setText(state.getString(FILTER_NAME, ""));
        }
        filterStates = state.getStringArray(FILTER_STATE);
        if (text_filterState != null) {
            text_filterState.setText(getFilter_stateDisplay());
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle state )
    {
        if (edit_filterName != null) {
            state.putString(FILTER_NAME, edit_filterName.getText().toString());
        }
        state.putStringArray(FILTER_STATE, filterStates);
        super.onSaveInstanceState(state);
    }

    private View.OnClickListener onStatesFilterClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View view) {
            showStatesDialog(appCompatActivity);
        }
    };

    public void initViews(final Context context, final View dialogContent)
    {
        text_filterState = (TextView) dialogContent.findViewById(R.id.text_filter_state);
        if (text_filterState != null)
        {
            text_filterState.setText(getFilter_stateDisplay());
            text_filterState.setOnClickListener(onStatesFilterClicked);
        }

        label_filterState = (TextView) dialogContent.findViewById(R.id.label_filter_state);
        if (label_filterState != null) {
            label_filterState.setOnClickListener(onStatesFilterClicked);
        }

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

                        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (inputMethodManager != null) {
                            inputMethodManager.hideSoftInputFromWindow(edit_filterName.getWindowToken(), 0);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void showStatesDialog(AppCompatActivity activity)
    {
        if (activity != null)
        {
            StatesDialog statesDialog = new StatesDialog();
            statesDialog.setSelection(filterStates);

            statesDialog.setDialogListener(new StatesDialog.StatesDialogListener() {
                @Override
                public void onSelectionChanged(String[] selection)
                {
                    filterStates = selection;
                    if (text_filterState != null) {
                        text_filterState.setText(getFilter_stateDisplay());
                    }
                    if (dialogListener != null) {
                        dialogListener.onFilterChanged(FilterDialog.this, FILTER_STATE);
                    }
                }
            });

            statesDialog.show(activity.getSupportFragmentManager(), TAG_DIALOG_STATES);
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

    private String[] filterStates;                 // TODO: preserve on orientation change
    public void setFilter_state(String[] states)
    {
        filterStates = states;
    }
    public String[] getFilter_state()
    {
        return filterStates;
    }

    public String getFilter_stateDisplay()
    {
        StringBuilder statesDisplay = new StringBuilder();
        for (int i=0; i<filterStates.length; i++)
        {
            statesDisplay.append(filterStates[i]);
            if (i != filterStates.length-1) {
                statesDisplay.append(", ");
            }
        }
        return statesDisplay.toString();
    }

    public void setFilter_scale(String value)
    {
        // TODO
    }
    public String getFilter_scale()
    {
        return "";  // TODO
    }

    private AppCompatActivity appCompatActivity;
    public void setAppCompatActivity(AppCompatActivity activity)
    {
        this.appCompatActivity = activity;
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
