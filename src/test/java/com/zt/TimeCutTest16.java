package com.zt;

import com.zt.Algorithm.ModifiedIde;
import com.zt.Algorithm.ReturnValue;
import com.zt.MBS.Model;
import com.zt.Tools.DateUtil;
import com.zt.Tools.ExcelOperate;

public class TimeCutTest16 {
    public static void main(String[] args) {
        double[] bestValues = new double[]{32.40506371,34.05293009,33.69738205,34.57504137};
        int[] times = new int[]{12,13,14,16};
        double[][] recordingBest = new double[10][4];
        double[][][] userLocationArrays = new double[][][]{ExcelOperate.getUserLocation(11),ExcelOperate.getUserLocation(11),
                ExcelOperate.getUserLocation(12),ExcelOperate.getUserLocation(13),ExcelOperate.getUserLocation(15)};
        int j = 3;
        for (int i = 0; i < 10; i++) {

            long startTime = System.currentTimeMillis();
            Model model = new Model(2,userLocationArrays[j]);
            ModifiedIde solution = new ModifiedIde(model,1500);
            ReturnValue result = solution.justDoIt();
            model.doUserSelectBs(result.getBestLocation(),result.getBestPowerOut());
            result = solution.justDoIt();
            /*存储最优解以及最优值*/
            recordingBest[i][j] = result.getBestValue();
            long endTime = System.currentTimeMillis();
            System.out.println("第"+i+"次重复试验 时间片为 "+times[j]+"点  其最优值为："+result.getBestValue()+"  消耗时间为："+ DateUtil.formatTime(startTime-endTime));
        }
        /*统计平均值*/
        double avaValue = 0;
        for (int i = 0; i < 10; i++) {
            avaValue+=recordingBest[i][j];
        }
        System.out.println("时间片为 "+times[j]+" 对应的平均值为"+ avaValue/10 +" 与IDE平均最优值相差 "+(avaValue/10-bestValues[j]));
    }
}
