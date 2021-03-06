package com.evranger.soulevspy;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.evranger.obd.ObdMessageData;
import com.evranger.obd.ObdMessageFilter;
import com.evranger.soulevspy.obd.BatteryChargingMessageFilter;
import com.evranger.soulevspy.obd.EstimatedRangeMessageFilter;
import com.evranger.soulevspy.obd.ParkingBrakeMessageFilter;
import com.evranger.soulevspy.obd.StateOfChargePreciseMessageFilter;
import com.evranger.soulevspy.obd.Status050MessageFilter;
import com.evranger.soulevspy.obd.Status55DMessageFilter;
import com.evranger.soulevspy.obd.TireRotationSpeedMessageFilter;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;
import com.evranger.soulevspy.util.ClientSharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-12.
 */
@RunWith(AndroidJUnit4.class)
public class ObdMessageDataTest {

    private int testIntValue = 0;
    private double testDoubleValue1 = 0.0;
    private double testDoubleValue2 = 0.0;
    private double testDoubleValue3 = 0.0;
    private double testDoubleValue4 = 0.0;
    private boolean testBoolean = false;

    private GregorianCalendar testCalendar = null;
    Status050MessageFilter.WiperSpeed wiperSpeed = Status050MessageFilter.WiperSpeed.OFF;
    Status050MessageFilter.LightsMode lightsMode = Status050MessageFilter.LightsMode.OFF;
    Status050MessageFilter.TurnSignal turnSignal = Status050MessageFilter.TurnSignal.OFF;
    BatteryChargingMessageFilter.ConnectedChargerType chargerType = BatteryChargingMessageFilter.ConnectedChargerType.NONE;

    private CurrentValuesSingleton vals;

    @Before
    public void init() {
        vals = CurrentValuesSingleton.reset();
        ClientSharedPreferences prefs = new ClientSharedPreferences(InstrumentationRegistry.getTargetContext());
        vals.setPreferences(prefs);
    }


    @Test
    public void testMessageNormalData() {
        final String message598 = "598 00 AF 00 00 81 44 17 16";

        ObdMessageData messageData = new ObdMessageData(message598);

        assertEquals("598", messageData.getMessageIdentifier());
        assertEquals(message598, messageData.getRawData());

        ArrayList<String> data = messageData.getData();
        assertEquals(8, data.size());
        assertEquals("00", data.get(0));
        assertEquals("AF", data.get(1));
        assertEquals("00", data.get(2));
        assertEquals("00", data.get(3));
        assertEquals("81", data.get(4));
        assertEquals("44", data.get(5));
        assertEquals("17", data.get(6));
        assertEquals("16", data.get(7));
    }

    @Test
    public void testMessageErrorData() {
        final String message018 = "018 A1 00 01 60 01 00 20 10 <DATA ERROR";

        ObdMessageData messageData = new ObdMessageData(message018);

        assertEquals("018", messageData.getMessageIdentifier());
        assertEquals(message018, messageData.getRawData());

        ArrayList<String> data = messageData.getData();
        assertEquals(data.size(), 8);
        assertEquals("A1", data.get(0));
        assertEquals("00", data.get(1));
        assertEquals("01", data.get(2));
        assertEquals("60", data.get(3));
        assertEquals("01", data.get(4));
        assertEquals("00", data.get(5));
        assertEquals("20", data.get(6));
        assertEquals("10", data.get(7));
    }

    @Test
    public void testEstimatedRange() {
        EstimatedRangeMessageFilter filter = new EstimatedRangeMessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                assertTrue(messageFilter instanceof EstimatedRangeMessageFilter);

                int estimatedRange = ((EstimatedRangeMessageFilter) messageFilter).getEstimatedRangeKm();
                assertEquals(testIntValue, estimatedRange);

                double extraRange = ((EstimatedRangeMessageFilter) messageFilter).getAdditionalRangeWithClimateOffKm();
                assertEquals(0.0, extraRange);
            }
        });

        // Send the test data
        testIntValue = 511;
        filter.receive("200 00 80 FF 00 00 00 00 00");

        testIntValue = 0;
        filter.receive("200 00 00 00 00 00 00 00 00");

        testIntValue = 104;
        filter.receive("200 00 12 34 00 00 00 00 00");
    }

    @Test
    public void testTireRotationSpeed() {
        TireRotationSpeedMessageFilter filter = new TireRotationSpeedMessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                assertTrue(messageFilter instanceof TireRotationSpeedMessageFilter);

                double speed = ((TireRotationSpeedMessageFilter) messageFilter).getLeftFrontSpeedKmH();
                assertEquals(testDoubleValue1, speed);

                speed = ((TireRotationSpeedMessageFilter) messageFilter).getRightFrontSpeedKmH();
                assertEquals(testDoubleValue2, speed);

                speed = ((TireRotationSpeedMessageFilter) messageFilter).getLeftBackSpeedKmH();
                assertEquals(testDoubleValue3, speed);

                speed = ((TireRotationSpeedMessageFilter) messageFilter).getRightBackSpeedKmH();
                assertEquals(testDoubleValue4, speed);
            }
        });

        testDoubleValue1 = 100.0;
        testDoubleValue2 = 0.0;
        testDoubleValue3 = 2184.5;
        testDoubleValue4 = 444.333333333333333;
        filter.receive("4B0 B8 0B 00 00 FF FF 12 34");
    }

    @Test
    public void testParkingBrake() {
        ParkingBrakeMessageFilter filter = new ParkingBrakeMessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                boolean status = ((ParkingBrakeMessageFilter) messageFilter).getIsParkingBrakeOn();
                assertEquals(testBoolean, status);
            }
        });

        testBoolean = false;
        filter.receive("433 01 00");

        testBoolean = true;
        filter.receive("433 00 00 08 21 00 00 3C 00");
    }

    @Test
    public void testStateOfChargePrecise() {
        StateOfChargePreciseMessageFilter filter = new StateOfChargePreciseMessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                assertTrue(messageFilter instanceof StateOfChargePreciseMessageFilter);

                double stateOfCharge = ((StateOfChargePreciseMessageFilter) messageFilter).getSOC();
                assertEquals(testDoubleValue1, stateOfCharge);
            }
        });

        // Send the test data
        testDoubleValue1 = 0.0;
        filter.receive("598 00 00 00 00 00 00 00 00");

        testDoubleValue1 = 100.0;
        filter.receive("598 00 00 00 00 00 64 00 00");

        testDoubleValue1 = 68.50390625;
        filter.receive("598 00 AF 00 00 81 44 17 16");
    }

    @Test
    public void testStatus050() {
        Status050MessageFilter filter = new Status050MessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                assertTrue(messageFilter instanceof Status050MessageFilter);

                Status050MessageFilter f = (Status050MessageFilter) messageFilter;

                assertEquals(lightsMode, f.getLightsMode());
                assertEquals(turnSignal, f.getTurnSignalStatus());
                assertEquals(wiperSpeed, f.getWiperSpeedStatus());
            }
        });

        // Send the test data
        lightsMode = Status050MessageFilter.LightsMode.OFF;
        turnSignal = Status050MessageFilter.TurnSignal.OFF;
        wiperSpeed = Status050MessageFilter.WiperSpeed.OFF;
        filter.receive("050 00 00 00 00");

        lightsMode = Status050MessageFilter.LightsMode.AUTOMATIC;
        turnSignal = Status050MessageFilter.TurnSignal.LEFT;
        wiperSpeed = Status050MessageFilter.WiperSpeed.INTER_3;
        filter.receive("050 00 23 22 00 00 00 00 00");

        lightsMode = Status050MessageFilter.LightsMode.PARKING;
        turnSignal = Status050MessageFilter.TurnSignal.RIGHT;
        wiperSpeed = Status050MessageFilter.WiperSpeed.FAST;
        filter.receive("050 00 01 14 00 00 00 00 00");
    }

    @Test
    public void testBatteryCharging() {
        BatteryChargingMessageFilter filter = new BatteryChargingMessageFilter();

        filter.addObdMessageFilterListener(new ObdMessageFilter.ObdMessageFilterListener() {
            @Override
            public void onMessageReceived(ObdMessageFilter messageFilter) {
                assertTrue(messageFilter instanceof BatteryChargingMessageFilter);

                BatteryChargingMessageFilter f = (BatteryChargingMessageFilter) messageFilter;

                assertEquals(testBoolean, f.getIsCharging());
                assertEquals(chargerType, f.getConnectedChargerType());
                assertEquals(testDoubleValue1, f.getChargingPowerKW());
            }
        });

        testBoolean = false;
        chargerType = BatteryChargingMessageFilter.ConnectedChargerType.NONE;
        testDoubleValue1 = 0.0;
        filter.receive("581 00 00 00 00 00 00 00 00");

        testBoolean = true;
        chargerType = BatteryChargingMessageFilter.ConnectedChargerType.J1772;
        testDoubleValue1 = 6.6015625;
        filter.receive("581 00 00 00 09 00 0E 9A 06");
    }

    @Test
    public void test55DMessageFilter() {
        Status55DMessageFilter filter = new Status55DMessageFilter();

        filter.receive("55D 55 80 36 DC 1D 50 00 00");


        filter.receive("55D 4B 80 34 DC 1D 50 00 00 <DATA ERROR");

        filter.receive("55D 10 81 3D DC 1D 50 00 00");
    }

    @Test
    public void testMessageFromCheapClone() {
        final String message598 = "00 00 06 53 00 12 61 01 1F 00 00 00";

        ObdMessageData messageData = new ObdMessageData(message598);

        assertEquals("653", messageData.getMessageIdentifier());
        assertEquals(message598, messageData.getRawData());

        ArrayList<String> data = messageData.getData();
        assertEquals(data.size(), 8);
        assertEquals("00", data.get(0));
        assertEquals("12", data.get(1));
        assertEquals("61", data.get(2));
        assertEquals("01", data.get(3));
        assertEquals("1F", data.get(4));
        assertEquals("00", data.get(5));
        assertEquals("00", data.get(6));
        assertEquals("00", data.get(7));
    }
}
