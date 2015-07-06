package com.example.android.sunshine.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;

/**
 * Created by Gustavo on 06/07/2015.
 */
public class LocationEditTextPreference extends EditTextPreference {
    public static final String LOG_TAG = LocationEditTextPreference.class.getSimpleName();
    public static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 2;
    private final int mMinLength;

    public LocationEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a =
                context.getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference, 0, 0);
        try {
            mMinLength = a.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            a.recycle();
        }
        Log.v(LOG_TAG, "mMinLength " + mMinLength);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Dialog d = getDialog();
                if (d instanceof android.app.AlertDialog) {
                    ((android.app.AlertDialog) d)
                            .getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(s.length() >= mMinLength);
                } else if (d instanceof android.support.v7.app.AlertDialog) {
                    ((android.support.v7.app.AlertDialog) d)
                            .getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(s.length() >= mMinLength);
                }
            }
        });
    }
}
