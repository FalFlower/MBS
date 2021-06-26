package com.zt;

import com.zt.Algorithm.*;
import com.zt.MBS.Model;
import com.zt.MBS.ModelParameter;
import com.zt.Tools.ExcelOperate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelTest {
    public static void main(String[] args) {
        testBlackBoxModel();
    }
    private static void testBlackBoxModel() {
        int eeWeight = 2;
        int dayTime = 8;
        double[][] userLocationArray = ExcelOperate.getUserLocation(dayTime);
        Model model = new Model(eeWeight,userLocationArray);

        double[] testPowerOut = new double[]{0.6367620040866506,  4.472988865725222,  1.4003710675518217 , 3.361587474635536  ,3.2220803805126663,  3.941403009424631  ,4.492982836124377  ,2.3907134939145935  ,2.259587483565613 , 2.855781402186974,  0.6601190449069968  ,1.7880747966708308,  5.267669892331811 , 5.110644279805143,3.1811948863505917,5.74962336996961,2.5112964656088983,4.263092842863134,2.0446274573301313,1.7600145291540434,5.739400686835182,4.724112986164196,5.163634679170214,4.560608737113256,4.100403610512238 };
        double[] testPowerOut2 = new double[]{4.238446449387056 , 3.411476892793799 , 4.294818429277248,  5.268737852312507  ,6.264529218264875  ,2.8196203730219414  ,2.6475819289729228 , 1.1599542192439993 , 1.0876294250579268  ,1.419777942138221 , 5.036694983209702  ,2.390835977147151 , 6.287237911990696  ,3.847717596402451 , 5.673367312750765  ,2.216903643054965  ,5.178157559048399 , 4.436232221464844 , 5.86626563424032,  2.1574443749441565 , 5.659528251868235,  5.1042159643292075  ,3.987549462469029 , 2.322416321142656,5.36677124651674  };
        double[][] testLocation =  ModelParameter.getBsLocation();
        List<Double[]> solutionLocation = new ArrayList<>();
        List<Double> solutionPowerOut = new ArrayList<>();
        List<Double> solutionPowerOut2 = new ArrayList<>();
        for (int i = 0; i <testLocation.length; i++) {
            solutionLocation.add(new Double[]{testLocation[i][0],testLocation[i][1]});
            solutionPowerOut.add(testPowerOut[i]);
            solutionPowerOut2.add(testPowerOut2[i]);
        }
        System.out.println(model.countEE(solutionLocation,solutionPowerOut));
        System.out.println(model.countEE(solutionLocation,solutionPowerOut2));
        System.out.println(model.countEE(solutionLocation,solutionPowerOut));

//        BaseAlgorithm solution = new DifferentialEvolution(model,100);

//        ReturnValue result = solution.justDoIt();
//        System.out.println("最佳目标值： "+result.getBestValue() + "迭代次数为："+result.getgLongNum());
//        try {
//            solution.outputFitnessValues(0,dayTime);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        System.out.println("done");
//        model.doUserSelectBs(result.getBestLocation(),result.getBestPowerOut());
//        result = solution.justDoIt();
//        System.out.println("最佳目标值： "+result.getBestValue() + "迭代次数为："+result.getgLongNum());
//        try {
//            solution.outputFitnessValues(1,dayTime);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
