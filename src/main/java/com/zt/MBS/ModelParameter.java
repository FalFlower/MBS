package com.zt.MBS;

import com.zt.Tools.ExcelOperate;
import com.zt.Tools.Finder;

/**
 * @author zhangtian
 */
public class ModelParameter {
    /**EE评价指标权重
     * 分别为：α、β、γ、δ、ζ
     * */
    private static double[][] eeWeights = {
            {0.2,0.2,0.2,0.2,0.2},
            {0.1,0.3,0.3,0.2,0.1},
            {0.1,0.2,0.3,0.3,0.1},
            {0.05,0.25,0.3,0.3,0.1},
            {0.05,0.2,0.3,0.3,0.15},
            {0.05,0.15,0.3,0.3,0.2},
            {0.05,0.15,0.35,0.3,0.15}
    };

    public static final int DAY_TIMES = 24;
    public static final int MAX_LOCATION = 2000;
    public static final int MIN_LOCATION = 0;
    public static final double MAX_POWER_OUT = 6.3;
    public static final double MIN_POWER_OUT = 0.0;
    /**一天内的客流量
     * 9:00 ~ 23:00
     *                                                                                        9      10     11      12       13      14       15       16      17      18       19       20     21     22   */
    private static final int[] userNums = { 0, 0, 0, 0, 0, 0, 0, 0, 480, 950, 1500, 2200, 2500, 2550, 2600, 2700, 3000, 3500, 3400, 3200, 2100, 750, 0 ,0};

    /**生成固定基站坐标*/
    public static double[][] getBsLocation(){
        return new double[][]{
                {1,1}, {401,1}, {801,1}, {1201,1}, {1601,1},
                {1,401}, {401,401}, {801,401}, {1201,401}, {1601,401},
                {1,801}, {401,801}, {801,801}, {1201,801}, {1601,801},
                {1,1201}, {401,1201}, {801,1201}, {1201,1201}, {1601,1201},
                {1,1601}, {401,1601}, {801,1601}, {1201,1601}, {1601,1601}
        };
    }

    public static int getUserNumMax() {
        return USER_NUM_MAX;
    }

    private static final int USER_NUM_MAX = 3500;

//    /**生成线性增长的用户坐标
//     * 如果当前小时内客流表人数较上一小时内递增，直接使用取得的值
//     * 如果当前小时内客流表人数较上一小时内递减，从之前的数组中随机的删减差值的人数
//     * !!!!!!!!! 返回值可能为null ， 当客流为0时
//     * */
//    public static double[][] getUserLocation(int times , int timesSlice){
//        /*默认times = 0 时 getUserNums(times) = 0*/
//        if (times == 0) {return null;}
//        if ( getUserNums(times) == 0 && getUserNums(times - 1) == 0){
//            return null;
//        }
//        int oldUserNums = getUserNums(times,timesSlice - 1);
//        int actualUserNums = getUserNums(times,timesSlice);
//        double[][] result = new double[actualUserNums][2];
//        if ((actualUserNums - oldUserNums) >= 0){
//            /*递增趋势*/
//            result =  ExcelOperate.getUserLocation(actualUserNums);
//        }else {
//            /*递减趋势*/
//            double[][] oldUserLocation = ExcelOperate.getUserLocation(oldUserNums);
//            if (actualUserNums==0){
//                return null;
//            }
//            int[] randIndex = Finder.randomSet(0 , oldUserNums , actualUserNums);
//            for (int i = 0; i < actualUserNums; i++) {
//                result[i] = oldUserLocation[randIndex[i]];
//            }
//        }
//        return result;
//    }




//    public static int getUserNums(int time) {
//        return userNums[time];
//    }
//
//    public static int getUserNums(int time , int timeSlice) {
//        if (timeSlice < 0){
//            time --;
//            timeSlice = HOUR_TIMES - TIME_SLICE;
//        }
//        if (time < 0){
//            time = 0;
//        }
//        int oldUserNum = time == 0 ? 0:userNums[time-1];
//        int newUserNum = userNums[time];
//        return oldUserNum + (newUserNum - oldUserNum) * (timeSlice + TIME_SLICE) / HOUR_TIMES;
//    }

    public static double[][] getEeWeights() {
    return eeWeights;
}

    public static int[] getUserNums() {
        return userNums;
    }

}
