package org.anz.challenge.process;

import org.anz.challenge.util.HelperUtil;
import org.anz.challenge.data.Values;

import java.text.ParseException;
import java.util.Map;

public class ProcessLineThread implements Runnable {
    private final String timestamp;
    private final String currencyPair;
    private final double price;
    private final long volume;
    private final Map<String, Values> sumTotalMap;

    public ProcessLineThread(String timestamp, String currencyPair, double price, long volume, Map<String, Values> sumTotalMap) {
        this.timestamp = timestamp;
        this.currencyPair = currencyPair;
        this.price = price;
        this.volume = volume;
        this.sumTotalMap = sumTotalMap;
    }

    @Override
    public void run() {
        Values previousValue = null;
        try {
            previousValue = sumTotalMap.putIfAbsent(currencyPair + "~" + HelperUtil.getTimeRange(timestamp), new Values(price * volume, volume));
        } catch (ParseException ignore) {
        }
        if (previousValue != null) {
            previousValue.addPriceForVolumes(price * volume);
            previousValue.addVolume(volume);
        }
    }


}
