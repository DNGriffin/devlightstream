package com.limelight.preferences;

import android.app.Application;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import com.limelight.R;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@Config(sdk=33, application=Application.class, shadows=com.limelight.shadows.ShadowMoonBridge.class)
@RunWith(RobolectricTestRunner.class)
public class DcStickSettingsTest {
    @Test public void tuningControlsInflateShowValuesAndPersistIndependentCurves() {
        try (var controller=Robolectric.buildActivity(AppCompatActivity.class)) {
            controller.get().setTheme(R.style.SettingsTheme);
            controller.setup();
            AppCompatActivity activity=controller.get();
            PreferenceManager.getDefaultSharedPreferences(activity).edit().clear().commit();
            PreferenceManager manager=new PreferenceManager(activity);
            PreferenceScreen screen=manager.inflateFromResource(activity,R.xml.preferences,null);
            for (String side:new String[]{"left","right"}) {
                StickPercentagePreference deadzone=screen.findPreference("seekbar_"+side+"_stick_deadzone");
                StickPercentagePreference minimum=screen.findPreference("seekbar_"+side+"_stick_minimum");
                StickPercentagePreference maximum=screen.findPreference("seekbar_"+side+"_stick_scale");
                assertNotNull(deadzone); assertNotNull(minimum); assertNotNull(maximum);
                assertEquals("5%",deadzone.getSummary()); assertEquals("0%",minimum.getSummary());
                assertEquals("100%",maximum.getSummary());
                ListPreference curve=screen.findPreference("list_"+side+"_stick_curve");
                assertEquals(5,curve.getEntries().length);
                curve.setValue(side.equals("left") ? "cubic" : "sqrt");
            }
            PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putInt("seekbar_left_stick_minimum",20).putInt("seekbar_left_stick_scale",50).commit();
            assertEquals("20%",screen.findPreference("seekbar_left_stick_minimum").getSummary());
            assertEquals("50%",screen.findPreference("seekbar_left_stick_scale").getSummary());
            PreferenceConfiguration config=PreferenceConfiguration.readPreferences(activity);
            assertEquals("cubic",config.leftStickCurve); assertEquals("sqrt",config.rightStickCurve);
        }
    }
}
