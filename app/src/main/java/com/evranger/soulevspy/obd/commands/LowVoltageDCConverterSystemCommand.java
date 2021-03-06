package com.evranger.soulevspy.obd.commands;

import com.evranger.elm327.commands.AbstractMultiCommand;
import com.evranger.elm327.commands.filters.RegularExpressionResponseFilter;
import com.evranger.obd.ObdMessageData;
import com.evranger.soulevspy.R;
import com.evranger.soulevspy.obd.values.CurrentValuesSingleton;

import java.util.List;

/**
 * Created by henrik on 30/06/2017.
 */

public class LowVoltageDCConverterSystemCommand extends AbstractMultiCommand {
    private BasicCommand mCmd2101 = null;

    public LowVoltageDCConverterSystemCommand() {
        mCmd2101 = new BasicCommand("21 01");
        addCommand(new BasicCommand("AT SH 7DF")); 
        addCommand(new BasicCommand("AT CRA 7CD"));
        addCommand(mCmd2101);

        mCmd2101.addResponseFilter(new RegularExpressionResponseFilter("^7CD(.*)$"));
//        mCmd2101.addResponseFilter(new RemoveSpacesResponseFilter());
    }

    public void doProcessResponse() {
        mCmd2101.getResponse().process();
        List<String> lines = mCmd2101.getResponse().getLines();
        if (lines.size() != 3)
            return;

        ObdMessageData obdData = new ObdMessageData(lines.get(1));
        CurrentValuesSingleton vals = CurrentValuesSingleton.getInstance();

        boolean enabled = (obdData.getDataByte(1) & 6) != 0;
        double out_voltage = obdData.getDataByte(2) / 10.0;
        int out_current = obdData.getDataByte(3);
        int in_voltage = obdData.getDataByte(4) * 2;
        int possibly_temperature = obdData.getDataByte(5) - 100;

        vals.set(R.string.col_ldc_enabled, Boolean.valueOf(enabled));
        vals.set(R.string.col_ldc_out_DC_voltage_V, Double.valueOf(out_voltage));
        vals.set(R.string.col_ldc_out_DC_current_A, Integer.valueOf(out_current));
        vals.set(R.string.col_ldc_in_DC_voltage_V, Integer.valueOf(in_voltage));
        vals.set(R.string.col_ldc_temperature_C, Integer.valueOf(possibly_temperature));
    }
}