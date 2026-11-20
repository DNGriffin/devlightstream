package com.limelight.binding.input;

import org.junit.Test;
import static org.junit.Assert.*;

public class StickResponseTest {
    private static final String[] CURVES = {"linear", "quadratic", "cubic", "sqrt", "smoothstep"};
    private static final double UNIT = StickResponse.FULL_SCALE;
    private static double radius(int packed) {
        return Math.hypot(StickResponse.x(packed), StickResponse.y(packed)) / UNIT;
    }
    private static short input(double value) { return (short) Math.round(value * UNIT); }

    @Test public void requestedRangeUsesAllTravel() {
        StickResponse response = new StickResponse(5, 20, 50, "linear");
        assertEquals(0, response.apply(input(.049), (short) 0));
        assertEquals(.20, radius(response.apply(input(.0501), (short) 0)), .0001);
        assertEquals(.35, radius(response.apply(input(.525), (short) 0)), .0001);
        assertEquals(.50, radius(response.apply(input(1), (short) 0)), .0001);
    }

    @Test public void allCurvesAreMonotonicAndUseSameEndpoints() {
        for (String curve : CURVES) {
            StickResponse response = new StickResponse(5, 20, 50, curve);
            double previous = 0;
            int distinct = 0;
            for (int raw = 0; raw <= StickResponse.FULL_SCALE; raw++) {
                double current = radius(response.apply((short) raw, (short) 0));
                assertTrue(curve, current >= previous);
                assertTrue(curve, current <= .50004);
                if (current > previous) distinct++;
                previous = current;
            }
            assertEquals(curve, .5, previous, .00004);
            assertTrue(curve + " retains thousands of output steps", distinct > 7000);
        }
    }

    @Test public void curveShapesDifferAtQuarterTravel() {
        double t = .05 + .95 * .25;
        assertEquals(.20 + .30 * .25, radius(new StickResponse(5,20,50,"linear").apply(input(t),(short)0)), .0001);
        assertEquals(.20 + .30 * .0625, radius(new StickResponse(5,20,50,"quadratic").apply(input(t),(short)0)), .0001);
        assertEquals(.20 + .30 * .015625, radius(new StickResponse(5,20,50,"cubic").apply(input(t),(short)0)), .0001);
        assertEquals(.20 + .30 * .5, radius(new StickResponse(5,20,50,"sqrt").apply(input(t),(short)0)), .0001);
        assertEquals(.20 + .30 * .15625, radius(new StickResponse(5,20,50,"smoothstep").apply(input(t),(short)0)), .0001);
    }

    @Test public void directionsAndCircularCapSurviveEveryQuadrant() {
        for (String curve : CURVES) {
            StickResponse response = new StickResponse(5,20,50,curve);
            for (int xSign : new int[]{-1,1}) for (int ySign : new int[]{-1,1}) {
                int result = response.apply(input(.6*xSign),input(.8*ySign));
                assertEquals(.5, radius(result), .00005);
                assertEquals(.75 * xSign / ySign, (double)StickResponse.x(result)/StickResponse.y(result), .0002);
                assertEquals(.5, radius(response.apply(input(xSign), input(ySign))), .00005);
            }
        }
    }

    @Test public void neutralBoundsAndInvalidSettingsAreSafe() {
        for (String curve : CURVES) {
            assertEquals(0, new StickResponse(0,20,50,curve).apply((short)0,(short)0));
            assertEquals(0, new StickResponse(5,20,0,curve).apply((short)32767,(short)-32768));
            assertEquals(.5, radius(new StickResponse(5,90,50,curve).apply(input(.2),(short)0)), .00004);
            assertEquals(.5, radius(new StickResponse(99,20,50,curve).apply((short)-32768,(short)0)), .00004);
        }
        assertEquals(.35, radius(new StickResponse(5,20,50,"unknown").apply(input(.525),(short)0)), .0001);
        assertEquals(1, radius(new StickResponse(-3,-2,200,null).apply((short)32767,(short)32767)), .00005);
        assertEquals(0, new StickResponse(5,20,50,"linear").apply((short)1638,(short)0));
    }
}
