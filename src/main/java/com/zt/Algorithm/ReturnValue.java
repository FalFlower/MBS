package com.zt.Algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangtian
 */
public class ReturnValue {
    private double bestValue;
    private List<Double[]> bestLocation;
    private List<Double> bestPowerOut;
    private int gLongNum;

    public ReturnValue(double bestValue, List<Double[]> bestLocation, List<Double> bestPowerOut, int gLongNum) {
        this.bestValue = bestValue;
        this.bestLocation = bestLocation;
        this.bestPowerOut = bestPowerOut;
        this.gLongNum = gLongNum;
    }

    public ReturnValue() {
        this.bestValue = 0.0;
        this.bestLocation = new ArrayList<>();
        this.bestPowerOut = new ArrayList<>();
        gLongNum = 0;
    }


    public double getBestValue() {
        return bestValue;
    }

    public void setBestValue(double bestValue) {
        this.bestValue = bestValue;
    }

    public List<Double[]> getBestLocation() {
        return bestLocation;
    }

    public void setBestLocation(List<Double[]> bestLocation) {
        this.bestLocation = bestLocation;
    }

    public List<Double> getBestPowerOut() {
        return bestPowerOut;
    }

    public void setBestPowerOut(List<Double> bestPowerOut) {
        this.bestPowerOut = bestPowerOut;
    }

    public int getgLongNum() {
        return gLongNum;
    }

    public void setgLongNum(int gLongNum) {
        this.gLongNum = gLongNum;
    }
}
