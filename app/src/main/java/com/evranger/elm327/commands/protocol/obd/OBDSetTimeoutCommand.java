package com.evranger.elm327.commands.protocol.obd;

import com.evranger.elm327.commands.AbstractCommand;

/**
 * Created by Pierre-Etienne Messier <pierre.etienne.messier@gmail.com> on 2015-10-26.
 */
public class OBDSetTimeoutCommand extends AbstractCommand {
    public OBDSetTimeoutCommand(int timeoutInMs) {
        super("AT ST " + Integer.toHexString(0xFF & timeoutInMs).toUpperCase());
    }
    public void doProcessResponse() {}
}
