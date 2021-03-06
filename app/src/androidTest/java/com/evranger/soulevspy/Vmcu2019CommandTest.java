package com.evranger.soulevspy;

import android.util.Pair;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.evranger.soulevspy.obd.commands.Vmcu2019Command;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;
import com.evranger.soulevspy.util.ClientSharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class Vmcu2019CommandTest {
//>o:AT SH 7E2
//    i:AT SH 7E2
//    OK
//
//>o:AT CRA 7EA
//    i:AT CRA 7EA
//            OK

    final String msgOk = "OK \r" +
            ">";

    final String soulEv2020Vmcu2101 = "7EA 10 18 61 01 FF F8 00 00 \r" +
            "7EA 21 09 28 5A 06 06 0C 03 \r" +
            "7EA 22 00 00 A2 01 2E 75 34 \r" +
            "7EA 23 04 08 08 05 00 00 00 \r" +
            ">";

    final String soulEv2020Vmcu2102 = "7EA 10 27 61 02 F8 FF FC 00 \r" +
            "7EA 21 01 01 00 00 00 94 0F \r" +
            "7EA 22 BF 81 D1 39 D4 05 F8 \r" +
            "7EA 23 94 7C 38 20 80 54 14 \r" +
            "7EA 24 22 78 00 00 01 01 01 \r" +
            "7EA 25 00 00 00 07 00 00 00 \r" +
            ">";

    final String soulEv2020Vmcu1A80 = "7EA 10 63 5A 80 20 20 20 20 \r" +
            "7EA 21 20 20 20 20 20 20 1E \r" +
            "7EA 22 09 0D 14 4B 4E 41 4A \r" +
            "7EA 23 33 38 31 31 46 4C 37 \r" +
            "7EA 24 30 30 30 35 34 33 33 \r" +
            "7EA 25 36 36 30 31 2D 30 45 \r" +
            "7EA 26 41 43 30 20 20 20 20 \r" +
            "7EA 27 20 20 20 20 20 20 20 \r" +
            "7EA 28 1E 09 0D 14 53 4B 56 \r" +
            "7EA 29 4C 44 43 35 30 45 53 \r" +
            "7EA 2A 4B 45 4A 35 4D 2D 4E \r" +
            "7EA 2B 53 31 2D 44 30 30 30 \r" +
            "7EA 2C 53 4B 35 38 31 31 32 \r" +
            "7EA 2D 37 00 00 00 00 00 00 \r" +
            "7EA 2E 00 00 00 00 00 00 00 \r" +
            ">";

    final String soulEv2020Vmcu1A80_mobilescan = "7EA 10 63 5A 80 20 20 20 20 \r" +
            "7EA 21 20 20 20 20 20 20 1E \r" +
            "7EA 22 09 0D 14 4B 4E 41 4A \r" +
            "7EA 23 33 38 31 31 46 4C 37 \r" +
            "7EA 24 30 30 30 35 34 33 33 \r" +
            "7EA 25 36 36 30 31 2D 30 45 \r" +
            "7EA 26 41 43 30 20 20 20 20 \r" +
            "7EA 27 20 20 20 20 20 20 20 \r" +
            "7EA 28 1E 09 0D 14 53 4B 56 \r" +
            "7EA 29 4C 44 43 35 30 45 53 \r" +
            "7EA 2A 4B 45 4A 35 4D 2D 4E \r" +
            "7EA 2B 53 31 2D 44 30 30 30 \r" +
            "BUFFER FULL \r" +
            ">";

    // Ioniq EV:
    final String ioniqEv2017Vmcu2101 = "7EA 10 16 61 01 FF E0 00 00 \r" +
            "7EA 21 09 28 5A 3B 06 36 03 \r" +
            "7EA 22 00 00 23 01 57 77 34 \r" +
            "7EA 23 04 08 00 00 00 00 00 \r" +
            "\r" +
            ">";

    final String ioniqEv2017Vmcu2102 = "7EA 10 17 61 02 FF 80 00 00 \r" +
            "7EA 21 01 01 00 00 00 96 1C \r" +
            "7EA 22 AF 7B 96 3A C6 0A 74 \r" +
            "7EA 23 88 AB 39 00 00 00 00 \r" +
            "\r" +
            ">";

    final String ioniqEv2017Vmcu1A80 = "7EA 10 63 5A 80 20 20 20 20 \r" +
            "7EA 21 20 20 20 20 20 20 1E \r" +
            "7EA 22 09 0D 14 4B 4D 48 43 \r" +
            "7EA 23 37 35 31 48 46 48 55 \r" +
            "7EA 24 30 31 37 33 36 36 33 \r" +
            "7EA 25 36 36 30 31 2D 30 45 \r" +
            "7EA 26 32 35 30 20 20 20 20 \r" +
            "7EA 27 20 20 20 20 20 20 20 \r" +
            "7EA 28 1E 09 0D 14 41 45 56 \r" +
            "7EA 29 4C 44 43 35 33 45 41 \r" +
            "\r" +
            ">";

    final String eSoulGeoff2101 = "7EA 10 18 61 01 FF F8 00 00 \r" +
            "7EA 21 09 21 59 04 06 13 03 \r" +
            "7EA 22 00 00 00 00 D9 6E 34 \r" +
            "7EA 23 04 20 20 05 00 00 00 \r" +
            ">";

    final String eSoulGeoff2102 = "7EA 10 27 61 02 F8 FF FC 00 \r" +
            "7EA 21 01 01 00 00 00 8B 22 \r" +
            "7EA 22 C2 82 53 36 4C 0C 6C  \r" +
            "7EA 23 97 7B 35 14 82 55 76 \r" +
            "7EA 24 1E 8C 00 00 01 01 01 \r" +
            "7EA 25 00 00 00 07 00 00 00 \r" +
            ">";

    final String eSoulGeoff1A80 = "7EA 10 63 5A 80 20 20 20 20 \r" +
            "7EA 21 20 20 20 20 20 20 1E \r" +
            "7EA 22 09 0D 14 4B 4E 44 4A \r" +
            "7EA 23 33 33 41 31 36 4C 37 \r" +
            "7EA 24 30 30 32 32 32 33 33 \r" +
            "7EA 25 36 36 30 31 2D 30 45 \r" +
            "7EA 26 41 42 30 20 20 20 20 \r" +
            "7EA 27 20 20 20 20 20 20 20 \r" +
            "7EA 28 1E 09 0D 14 53 4B 56 \r" +
            "7EA 29 4C 44 43 35 30 45 53 \r" +
            "7EA 2A 4B 45 4B 4E 4D 2D 4E \r" +
            "7EA 2B 53 31 2D 44 30 30 30 \r" +
            "7EA 2C 53 4B 4E 39 30 31 32 \r" +
            "7EA 2D 38 00 00 00 00 00 00 \r" +
            "7EA 2E 00 00 00 00 00 00 00 " +
            ">";

    private CurrentValuesSingleton vals;

    @Before
    public void init() {
        vals = CurrentValuesSingleton.reset();
        ClientSharedPreferences prefs = new ClientSharedPreferences(InstrumentationRegistry.getTargetContext());
        vals.setPreferences(prefs);
    }

    @Test
    public void testSoul2020VmcuCommand() {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7E2", msgOk),
                new Pair<String, String>("AT CRA 7EA", msgOk),
                new Pair<String, String>("21 01", soulEv2020Vmcu2101),
                new Pair<String, String>("21 02", soulEv2020Vmcu2102),
                new Pair<String, String>("1A 80", soulEv2020Vmcu1A80)
        );
        Responder responder = new Responder(reqres);

        Vmcu2019Command cmd = new Vmcu2019Command();
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        } catch (Exception e) {
            assertEquals("", e.getMessage());
        }

        assertEquals("D", vals.get(R.string.col_vmcu_gear_state));
//        assertEquals(false, vals.get(R.string.col_vmcu_eco_off_switch));
//        assertEquals(false, vals.get(R.string.col_vmcu_brake_lamp_on_switch));
//        assertEquals(true, vals.get(R.string.col_vmcu_brake_off_switch));
//        assertEquals(false, vals.get(R.string.col_vmcu_ldc_inhibit));
//        assertEquals(false, vals.get(R.string.col_vmcu_fault_flag_of_mcu));
//        assertEquals(false, vals.get(R.string.col_vmcu_warning_flag_of_mcu));
//        assertEquals(false, vals.get(R.string.col_vmcu_radiator_fan_request_of_motor));
//        assertEquals(true, vals.get(R.string.col_vmcu_ignition_1));
//        assertEquals(33.0, vals.get(R.string.col_vmcu_accel_pedal_depth_pct));
        assertTrue(0.0001 > 6.727057920000001 - (Double)vals.get(R.string.col_vmcu_vehicle_speed_kph));
        assertEquals(14.46, vals.get(R.string.col_vmcu_aux_battery_V));
        assertEquals(84.0, vals.get(R.string.col_vmcu_aux_battery_SOC_pct));
//        assertEquals(380, vals.get(R.string.col_vmcu_inverter_input_V));
//        assertEquals(2564, vals.get(R.string.col_vmcu_motor_actual_speed_rpm));
//        assertEquals(411, vals.get(R.string.col_vmcu_motor_torque_command_Nm));
//        assertEquals(505, vals.get(R.string.col_vmcu_estimated_motor_torque_Nm));
//        assertEquals(14, vals.get(R.string.col_vmcu_temp_motor_C));
//        assertEquals(31, vals.get(R.string.col_vmcu_temp_mcu_C));
//        assertEquals(19, vals.get(R.string.col_vmcu_temp_heatsink_C));
        assertEquals("KNAJ3811FL7000543", vals.get("VIN"));
    }


    @Test
    public void testSoul2020VmcuCommandMobilescan() {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7E2", msgOk),
                new Pair<String, String>("AT CRA 7EA", msgOk),
                new Pair<String, String>("21 01", soulEv2020Vmcu2101),
                new Pair<String, String>("21 02", soulEv2020Vmcu2102),
                new Pair<String, String>("1A 80", soulEv2020Vmcu1A80_mobilescan)
        );
        Responder responder = new Responder(reqres);

        Vmcu2019Command cmd = new Vmcu2019Command();
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
//            cmd.doProcessResponse();
        } catch (Exception e) {
            assertEquals("Error running 1A 80, response: 7EA 10 63 5A 80 20 20 20 20 \r" +
                    "7EA 21 20 20 20 20 20 20 1E \r" +
                    "7EA 22 09 0D 14 4B 4E 41 4A \r" +
                    "7EA 23 33 38 31 31 46 4C 37 \r" +
                    "7EA 24 30 30 30 35 34 33 33 \r" +
                    "7EA 25 36 36 30 31 2D 30 45 \r" +
                    "7EA 26 41 43 30 20 20 20 20 \r" +
                    "7EA 27 20 20 20 20 20 20 20 \r" +
                    "7EA 28 1E 09 0D 14 53 4B 56 \r" +
                    "7EA 29 4C 44 43 35 30 45 53 \r" +
                    "7EA 2A 4B 45 4A 35 4D 2D 4E \r" +
                    "7EA 2B 53 31 2D 44 30 30 30 \r" +
                    "BUFFER FULL \r" +
                    ">", e.getMessage());
        }
        cmd.doProcessResponse();

        assertEquals("KNAJ3811FL7000543", vals.get("VIN"));
    }

    @Test
    public void testIoniq2017VmcuCommand() {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7E2", msgOk),
                new Pair<String, String>("AT CRA 7EA", msgOk),
                new Pair<String, String>("21 01", ioniqEv2017Vmcu2101),
                new Pair<String, String>("21 02", ioniqEv2017Vmcu2102),
                new Pair<String, String>("1A 80", ioniqEv2017Vmcu1A80)
        );
        Responder responder = new Responder(reqres);

        Vmcu2019Command cmd = new Vmcu2019Command();
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        } catch (Exception e) {
            assertEquals("", e.getMessage());
        }

        assertEquals("D", vals.get(R.string.col_vmcu_gear_state));
        assertTrue(0.0001 > 4.683191041788755 - (Double)vals.get(R.string.col_vmcu_vehicle_speed_kph));
        assertEquals(14.763, vals.get(R.string.col_vmcu_aux_battery_V));
        assertEquals("KMHC751HFHU017366", vals.get("VIN"));
    }

    @Test
    public void testGeoffsSoul2020VmcuCommand() {
        List<Pair<String, String>> reqres = Arrays.asList(
                new Pair<String, String>("AT SH 7E2", msgOk),
                new Pair<String, String>("AT CRA 7EA", msgOk),
                new Pair<String, String>("21 01", eSoulGeoff2101),
                new Pair<String, String>("21 02", eSoulGeoff2102),
                new Pair<String, String>("1A 80", eSoulGeoff1A80)
        );
        Responder responder = new Responder(reqres);

        Vmcu2019Command cmd = new Vmcu2019Command();
        try {
            cmd.execute(responder.getInput(), responder.getOutput());
            cmd.doProcessResponse();
        } catch (Exception e) {
            assertEquals("", e.getMessage());
        }

        assertEquals("KNDJ33A16L7002223", vals.get("VIN"));
    }

}
