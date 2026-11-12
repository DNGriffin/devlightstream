package com.limelight.binding.input;

import android.app.Activity;
import android.app.Application;

import androidx.preference.PreferenceManager;

import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.ControllerPacket;
import com.limelight.preferences.PreferenceConfiguration;
import com.limelight.shadows.ShadowMoonBridge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Config(sdk = 33, application = Application.class, shadows = ShadowMoonBridge.class)
@RunWith(RobolectricTestRunner.class)
public class ControllerStickScaleTest {
    @Test
    public void defaultsPreserveTheOriginalPacket() {
        sendAndVerify(null, null, (short) 32766, (short) -16000, (short) -32766, (short) 8000);
    }

    @Test
    public void eachStickScalesBothAxesIndependently() {
        sendAndVerify(50, 25, (short) 16383, (short) -8000, (short) -8191, (short) 2000);
    }

    @Test
    public void zeroSendsNeutralForOnlyThatStick() {
        sendAndVerify(100, 0, (short) 32766, (short) -16000, (short) 0, (short) 0);
    }

    private void sendAndVerify(Integer leftScale, Integer rightScale, short leftX, short leftY,
                               short rightX, short rightY) {
        try (var activityController = Robolectric.buildActivity(Activity.class).setup()) {
            Activity activity = activityController.get();
            if (leftScale != null && rightScale != null) {
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                        .putInt("seekbar_left_stick_scale", leftScale)
                        .putInt("seekbar_right_stick_scale", rightScale)
                        .apply();
            }

            NvConnection connection = mock(NvConnection.class);
            ControllerHandler handler = new ControllerHandler(
                    activity, connection, null, PreferenceConfiguration.readPreferences(activity));
            try {
                int buttons = ControllerPacket.A_FLAG;
                byte leftTrigger = 31;
                byte rightTrigger = (byte) 200;
                handler.reportOscState(buttons, (short) 32766, (short) -16000,
                        (short) -32766, (short) 8000, leftTrigger, rightTrigger);
                verify(connection).sendControllerInput(eq((short) 0), anyShort(), eq(buttons),
                        eq(leftTrigger), eq(rightTrigger), eq(leftX), eq(leftY), eq(rightX), eq(rightY));
            } finally {
                handler.destroy();
            }
        }
    }
}
