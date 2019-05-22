package org.hexpresso.elm327.commands;

import org.hexpresso.elm327.exceptions.BufferFullException;
import org.hexpresso.elm327.exceptions.CanErrorException;
import org.hexpresso.elm327.log.CommLog;
import org.hexpresso.elm327.exceptions.BusInitException;
import org.hexpresso.elm327.exceptions.MisunderstoodCommandException;
import org.hexpresso.elm327.exceptions.NoDataException;
import org.hexpresso.elm327.exceptions.ResponseException;
import org.hexpresso.elm327.exceptions.StoppedException;
import org.hexpresso.elm327.exceptions.UnableToConnectException;
import org.hexpresso.elm327.exceptions.UnknownErrorException;
import org.hexpresso.elm327.exceptions.UnsupportedCommandException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import android.os.SystemClock;
import android.util.Log;

/**
 * The AbstractCommand class represents an ELM327 command.
 * <p/>
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-24.
 */
public abstract class AbstractCommand implements Command {

    protected String mCommand = null;                          // ELM327 command
    protected long mResponseTimeDelay = 1;                     // Time delay before receiving the response, in milliseconds
    protected Response mResponse = new Response();             // Response object

    private long mRunStartTimestamp;                           // Timestamp before sending the command
    private long mRunEndTimestamp;                             // Timestamp after receiving the command response
    private boolean mWithAutoProcessResponse = false;          //
    private boolean mStopReadingAtLineEnd = false;             // If false, stop reading at '>', if true, at '\r'
    private long mTimeout_ms = 1500L;                          // Input timeout
    private boolean mSkip = false;
    /**
     * Error classes to be tested in order
     */
    private final static Class[] ERROR_CLASSES = {
            UnableToConnectException.class,
            BusInitException.class,
            MisunderstoodCommandException.class,
            NoDataException.class,
            StoppedException.class,
            UnknownErrorException.class,
            UnsupportedCommandException.class,
            CanErrorException.class,
            BufferFullException.class
    };

    /**
     * Constructor
     *
     * @param command ELM327 command to send
     */
    public AbstractCommand(String command) {
        mCommand = command;
    }

    protected AbstractCommand() {
    }

    public void setTimeoutMs(long timeout_ms) {
        mTimeout_ms = timeout_ms;
    }

    /**
     *
     * @param in
     * @param out
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    public void execute(InputStream in, OutputStream out) throws IOException, InterruptedException, TimeoutException {

        if (mSkip) {
            Log.d(AbstractCommand.class.getSimpleName(), "Skip execute");
            return;
        }

        // Skip output from previous commands
        flushInput(in);

        // Send the command
        mRunStartTimestamp = System.currentTimeMillis();
        send(out);

        // Wait before trying to receive the command response
        Thread.sleep(mResponseTimeDelay);

        // Receive the response - NOTE: This will throw exception on connection error etc!
        receive(in);
        mRunEndTimestamp = System.currentTimeMillis();
    }

    public void doProcessResponse() {};

    /**
     *
     * @param out
     * @throws IOException
     * @throws InterruptedException
     */
    protected void send(OutputStream out) throws IOException, InterruptedException {
        final String command = mCommand + '\r';
        Log.d(AbstractCommand.class.getSimpleName(), "send command: " + command);
        byte[] commandBytes = command.getBytes();
        out.write(commandBytes);
        out.flush();
        CommLog.getInstance().log("o:".getBytes());
        CommLog.getInstance().log(commandBytes);
    }

    /**
     *
     * @param in
     */
    protected void receive(InputStream in) throws IOException, TimeoutException {
        // Receive the response from the stream
        String rawResponse = readRawData(in);

        // TODO check this
        //rawResponse = rawResponse.replaceAll("(BUS INIT)|(BUSINIT)|(\\.)", "");

    /*
     * Data may have echo or informative text like "INIT BUS..." or similar.
     * The response ends with two carriage return characters. So we need to take
     * everything from the last carriage return before those two (trimmed above).
     */
        //kills multiline.. rawData = rawData.substring(rawData.lastIndexOf(13) + 1);
        //mResponse = mResponse.replaceAll("\\s", "");//removes all [ \t\n\x0B\f\r]

        // Generate the Response object
        mResponse.setRawResponse(rawResponse);
        if (mWithAutoProcessResponse) {
            mResponse.process();
        }

        // Check for errors
        checkForErrors();
    }

    protected String readRawData(InputStream in) throws IOException, TimeoutException {
        StringBuilder res = new StringBuilder();
        String rawResponse = "";
        long runStartTimestamp = System.currentTimeMillis();

        // read until '>' arrives OR end of stream reached
        // TODO : Also, add a default timeout value
        while (true) {
            if (in.available() == 0) {
                if ((runStartTimestamp + mTimeout_ms) < System.currentTimeMillis()) {
                    throw new TimeoutException("readRawData timed out while waiting for input");
                }
                SystemClock.sleep(1);
                continue;
            }
            final byte b = (byte) in.read();
            if (b == 0) {
                continue;
            }
            if (b == -1) // -1 if the end of the stream is reached
            {
                // End of stream reached
                break;
            }

            final char c = (char) b;
            res.append(c);

            if (c == '>' && !mStopReadingAtLineEnd) {
                rawResponse = processResponse(res.toString());
                // read until '>' arrives
                flushInput(in);
                break;
            }
            if (mStopReadingAtLineEnd && c == '\r') {
                rawResponse = processResponse(res.toString());
                flushInput(in);
                break;
            }
        }
        return rawResponse;
    }

    String processResponse(String resStr) throws IOException {
        String rawResponse = resStr;
        Log.d("AbstractCommand", rawResponse);

        CommLog.getInstance().log("i:".getBytes());
        CommLog.getInstance().log(rawResponse.getBytes());
        rawResponse = rawResponse.replaceAll("SEARCHING", "");

        return rawResponse;
    }

    protected void checkForErrors() {
        for (Class<? extends ResponseException> errorClass : ERROR_CLASSES) {
            ResponseException messageError;

            try {
                messageError = errorClass.newInstance();
                messageError.setCommand(this.mCommand);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            if (messageError.isError(mResponse.rawResponse())) {
                throw messageError;
            }
        }
    }

    @Override
    public Response getResponse() {
        return mResponse;
    }

    protected AbstractCommand addResponseFilter(ResponseFilter responseFilter) {
        mResponse.addResponseFilter(responseFilter);
        return this;
    }

    /**
     *
     *
     * @param autoProcessResponse
     * @return
     */
    public AbstractCommand withAutoProcessResponse(boolean autoProcessResponse) {
        mWithAutoProcessResponse = autoProcessResponse;
        return this;
    }

    public void setStopReadingAtLineEnd(boolean tostop) {
        mStopReadingAtLineEnd = tostop;
    }

    public boolean skip(boolean doSkip)
    {
        boolean prevSkip = mSkip;
        mSkip = doSkip;
        return prevSkip;
    }

    protected void flushInput(InputStream in) throws IOException {
        StringBuilder res = new StringBuilder();
        while (in.available() > 0) {
            final byte b = (byte) in.read();
            if (b == -1) // -1 if the end of the stream is reached
                break;
            res.append((char)b);
        }
        if (res.length() > 0) {
            CommLog.getInstance().log("f:".getBytes());
            CommLog.getInstance().log(res.toString().getBytes());
        }
    }
}