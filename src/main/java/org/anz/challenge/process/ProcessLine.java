package org.anz.challenge.process;

import org.anz.challenge.util.HelperUtil;
import org.anz.challenge.data.Values;

import java.text.ParseException;
import java.util.Map;

public class ProcessLine {

    public static void execute(String timestamp, String currencyPair, double price, long volume, Map<String, Values> outputMap) {
        Values previousValue = null;
        try {
            previousValue = outputMap.putIfAbsent(currencyPair + "~" + HelperUtil.getTimeRange(timestamp), new Values(price * volume, volume));
        } catch (ParseException ignore) {
        }
        if (previousValue != null) {
            previousValue.addPriceForVolumes(price * volume);
            previousValue.addVolume(volume);
        }
    }
}
