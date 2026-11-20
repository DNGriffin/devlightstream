package com.limelight.preferences;

import android.content.Context;
import android.util.AttributeSet;
import androidx.appcompat.app.AlertDialog;

/** Shows the stored tuning value in the list; the dialog retains the detailed help. */
public class StickPercentagePreference extends SeekBarPreference {
    public StickPercentagePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        int defaultPercentage = attrs.getAttributeIntValue(
                "http://schemas.android.com/apk/res/android", "defaultValue", 0);
        setSummaryProvider(preference -> getPersistedInt(defaultPercentage) + "%");
    }

    @Override
    protected AlertDialog getDialog() {
        AlertDialog dialog = super.getDialog();
        dialog.setOnDismissListener(ignored -> notifyChanged());
        return dialog;
    }
}
