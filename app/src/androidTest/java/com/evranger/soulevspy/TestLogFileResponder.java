package com.evranger.soulevspy;

import android.test.AndroidTestCase;
import android.util.Pair;

import java.io.InputStream;
import java.util.List;

public class TestLogFileResponder extends AndroidTestCase {

    public void testLogFileResponder() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("testLogFileResponder.log") ;
        LogFileResponder lfr = new LogFileResponder(is);

        List rl = lfr.getResponseList();

        assertEquals(new Pair("AT I", "AT I\r" +
                "ELM327 v1.3a\r" +
                "\r" +
                ">"), rl.get(1));
        assertEquals(new Pair("AT D", "AT D\r" +
                "OK\r" +
                "\r" +
                ">"), rl.get(2));
        assertEquals(new Pair("AT Z", "AT Z\r" +
                "\r" +
                "\r" +
                "ELM327 v1.3a\r" +
                "\r" +
                ">"), rl.get(3));

        // TODO: interim comm

        assertEquals(new Pair("AT RV", "14.1V\r" +
                "\r" +
                ">"),
                rl.get(20));

        // TODO: interim comm

        assertEquals(new Pair("AT CRA 653", "OK\r" +
                "\r" +
                ">"),
                rl.get(43));
        assertEquals(new Pair("AT MA" , "653 00 1E 00 00 00 77 00 00\r"),
                rl.get(44));
        assertEquals(new Pair(" ", "\r" +
                ">"),
                rl.get(45));
        assertEquals(new Pair("AT AR", "OK\r" +
                "\r" +
                ">"),
                rl.get(46));
        assertEquals(new Pair(" ", ""),
                rl.get(47));
        assertEquals(new Pair("AT CRA 594", "OK\r" +
                "\r" +
                ">"),
                rl.get(48));

        int i = 0;
    }
}