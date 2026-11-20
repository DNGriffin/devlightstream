package com.limelight.binding.input;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import com.limelight.binding.input.driver.AbstractController;
import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.ControllerPacket;
import com.limelight.preferences.PreferenceConfiguration;
import com.limelight.shadows.ShadowMoonBridge;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Config(sdk = 33, application = Application.class, shadows = ShadowMoonBridge.class)
@RunWith(RobolectricTestRunner.class)
public class ControllerStickScaleTest {
    @Test public void oscRemapsOnceAndKeepsButtonsTriggersAndSticksIndependent() {
        try (var controller = Robolectric.buildActivity(Activity.class).setup()) {
            Activity activity = controller.get();
            PreferenceManager.getDefaultSharedPreferences(activity).edit().clear()
                    .putInt("seekbar_left_stick_deadzone",5).putInt("seekbar_left_stick_minimum",20)
                    .putInt("seekbar_left_stick_scale",50).putString("list_left_stick_curve","linear")
                    .putInt("seekbar_right_stick_deadzone",0).putInt("seekbar_right_stick_minimum",0)
                    .putInt("seekbar_right_stick_scale",25).putString("list_right_stick_curve","quadratic").commit();
            NvConnection connection = mock(NvConnection.class);
            ControllerHandler handler = new ControllerHandler(activity,connection,null,PreferenceConfiguration.readPreferences(activity));
            try {
                // 52.5% travel -> 35% output, while right 50% -> .5^2 * 25% = 6.25%.
                for (int i=0;i<2;i++) handler.reportOscState(ControllerPacket.A_FLAG,(short)17202,(short)0,
                        (short)-16383,(short)0,(byte)31,(byte)200);
                verify(connection,times(2)).sendControllerInput(eq((short)0),anyShort(),eq(ControllerPacket.A_FLAG),
                        eq((byte)31),eq((byte)200),eq((short)11468),eq((short)0),eq((short)-2048),eq((short)0));
                handler.reportOscState(0,(short)0,(short)0,(short)0,(short)0,(byte)0,(byte)0);
                verify(connection).sendControllerInput(eq((short)0),anyShort(),eq(0),eq((byte)0),eq((byte)0),
                        eq((short)0),eq((short)0),eq((short)0),eq((short)0));
            } finally { handler.destroy(); }
        }
    }

    @Test public void usbHasNoLegacyDeadzoneOrDoubleScaling() {
        try (var controller = Robolectric.buildActivity(Activity.class).setup()) {
            Activity activity=controller.get();
            PreferenceConfiguration config=PreferenceConfiguration.readPreferences(activity);
            config.deadzonePercentage=20; // Deliberately larger than new input deadzone.
            config.leftStickDeadzonePercentage=5; config.leftStickMinimumPercentage=20;
            config.leftStickScalePercentage=50; config.leftStickCurve="linear";
            config.rightStickScalePercentage=0;
            NvConnection connection=mock(NvConnection.class);
            ControllerHandler handler=new ControllerHandler(activity,connection,null,config);
            try {
                AbstractController device=mock(AbstractController.class);
                when(device.getControllerId()).thenReturn(7);
                handler.deviceAdded(device);
                handler.reportControllerState(7,ControllerPacket.B_FLAG,.10f,0,1,0,0,0);
                // At 10%, output is 20 + 30*(10-5)/95 = 21.579%, not zero.
                verify(connection).sendControllerInput(anyShort(),anyShort(),eq(ControllerPacket.B_FLAG),
                        eq((byte)0),eq((byte)0),eq((short)7070),eq((short)0),eq((short)0),eq((short)0));
            } finally { handler.destroy(); }
        }
    }

    @Test public void preferencesPersistAndClampEachStickIndependently() {
        try (var controller = Robolectric.buildActivity(Activity.class).setup()) {
            SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(controller.get());
            prefs.edit().clear().putInt("seekbar_left_stick_deadzone",5).putInt("seekbar_left_stick_minimum",20)
                    .putInt("seekbar_left_stick_scale",50).putString("list_left_stick_curve","cubic")
                    .putInt("seekbar_right_stick_deadzone",999).putInt("seekbar_right_stick_minimum",80)
                    .putInt("seekbar_right_stick_scale",30).commit();
            PreferenceConfiguration config=PreferenceConfiguration.readPreferences(controller.get());
            assertEquals(5,config.leftStickDeadzonePercentage); assertEquals(20,config.leftStickMinimumPercentage);
            assertEquals(50,config.leftStickScalePercentage); assertEquals("cubic",config.leftStickCurve);
            assertEquals(99,config.rightStickDeadzonePercentage); assertEquals(30,config.rightStickMinimumPercentage);
        }
    }
}
