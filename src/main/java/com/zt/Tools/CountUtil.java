package com.zt.Tools;

import java.util.List;

/**
 * @author zhangtian
 */
public class CountUtil {

    /**计算均值*/
    public static double countMeanA(double[] values){
        double result = 0.0;
        for (double v : values) {
            result += v;
        }
        return (result/values.length);
    }

    /**计算均值*/
    public static double countMeanA(List<Double> values){
        double result = 0.0;
        for (double v : values) {
            result += v;
        }
        return (result/values.size());
    }

    /**计算加权均值*/
    public static double countMeanWa(List<Double> values){
        double result = 0.0;
        for (double v : values) {
            result += v;
        }
        return (result/values.size());
    }

    /**计算赫尔德均值*/
    public static double countLehmerMean(double[] values){
        double r2 = 0.0 ,r = 0.0;
        for (double value : values) {
            r2 += Math.pow(value,2);
            r+=value;
        }
        return r2/r;
    }

    /**计算赫尔德均值*/
    public static double countLehmerMean(List<Double> values){
        double r2 = 0.0 ,r = 0.0;
        for (double value : values) {
            r2 += Math.pow(value,2);
            r+=value;
        }
        return r2/r;
    }

    /**求和*/
    public static double countSum(double[] arr){
        double result = 0;
        for (int i = 0; i < arr.length; i++) {
            result += arr[i];
        }
        return result;
    }
}
