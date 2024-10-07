package org.anz.challenge.data;

import java.util.ArrayDeque;

public class Values {
    private double totalPriceForVolumes;
    private long totalVolume;
    private double vwap;

    public Values(double totalPriceForVolumes, long totalVolume) {
        this.totalPriceForVolumes = totalPriceForVolumes;
        this.totalVolume = totalVolume;
    }

    public void addPriceForVolumes(double value) {
        totalPriceForVolumes += value;
    }

    public void addVolume(long volume) {
        totalVolume += volume;
    }

    public void calculateVWAP() {
        vwap = totalPriceForVolumes / totalVolume;
    }

    public double getVwap() {
        return vwap;
    }
}
