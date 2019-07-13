package com.evranger.elm327.commands;

import com.evranger.elm327.commands.filters.RegularExpressionResponseFilter;
import com.evranger.elm327.commands.filters.RemoveSpacesResponseFilter;
import com.evranger.elm327.log.CommLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

/**
 * Created by henrik on 28/06/2017.
 */

public class StopAfterDataCommand extends AbstractCommand {
    long mTimeout = 0;
    String mFilter = new String();

    public StopAfterDataCommand(long timeout, String filter) {
        setTimeoutMs(timeout);
        mTimeout = timeout;
        mFilter = filter;
        if (filter.length() == 3) {
            addResponseFilter(new RegularExpressionResponseFilter("^" + filter + "(.*)$"));
            addResponseFilter(new RemoveSpacesResponseFilter());
        }
    }


    @Override
    public void execute(InputStream in, OutputStream out) throws IOException, InterruptedException, TimeoutException {
        long runStartTimestamp = System.currentTimeMillis();
        String rawResponse = new String();

        // Clean up after last iteration
        mResponse.setRawResponse(rawResponse);
        mResponse.process();

        mCommand = "AT CRA " + mFilter;
        send(out);
        String str = readRawData(in);

        mCommand = "AT MA";
        send(out);

        TimeoutException caughtException = null;
        try {
            setNumberOfLinesToRead(1);
            while ((mResponse.getLines() == null || mResponse.getLines().size() == 0) && (System.currentTimeMillis() < (runStartTimestamp + mTimeout))) {
                // Wait before trying to receive the monitor messages
                //            Thread.sleep(mResponseTimeDelay);

                // Receive the response - NOTE: This will throw exception on connection error etc!
                String raw = readRawData(in);
                mResponse.setRawResponse(raw);
                mResponse.process();
                rawResponse = raw;
            }
        } catch (TimeoutException e) {
            caughtException = e;
        }

        byte[] commandBytes = " ".getBytes();
        out.write(commandBytes);
        out.flush();
        CommLog.getInstance().log("o:".getBytes());
        CommLog.getInstance().log(commandBytes);// Stop monitoring

        setNumberOfLinesToRead(0);
        mResponse.setRawResponse(rawResponse);

        // Wait before trying to receive the command response
        Thread.sleep(mResponseTimeDelay);

        rawResponse += readRawData(in);
        flushInput(in);

        runStartTimestamp = System.currentTimeMillis();
        String ar_response = "";
        while ((!ar_response.endsWith(">")) && (System.currentTimeMillis() < (runStartTimestamp+mTimeout))) {
            mCommand = "AT AR";
            send(out);
            ar_response = readRawData(in);
            rawResponse += ar_response;
        }
        mResponse.setRawResponse(rawResponse);
        if (caughtException != null) {
            throw caughtException;
        }
        checkForErrors();
    }
}
