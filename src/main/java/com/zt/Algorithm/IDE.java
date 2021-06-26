package com.zt.Algorithm;

import com.zt.MBS.Model;
import com.zt.Tools.CountUtil;
import com.zt.Tools.Finder;
import com.zt.Tools.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public class IDE extends BaseAlgorithm {
    /**IDE参数*/
    private double crMax = 0.8;
    private double crMin = 0.1;
    private double fMax = 0.8;
    private double fMin = 0.2;
    private double[] crArray = new double[np];
    private double[] fArray = new double[np];
    private final double ERROR = Math.pow(10,-6);

    public IDE(Model model, int efsNum) {
        super(model, efsNum);
    }

    /**更新当代种群*/
    private void updatePopulation( int i , int j , double p , double x ,double y ){
        if (!model.checkPowerOutIsLegal(p,j)){
            pC++;
            /*如果变异产生不合法的数据，就重新生成新的值*/
            p = model.getMinPowerOut(j) >= powerOutMax ? powerOutMax : ThreadLocalRandom.current().nextDouble(model.getMinPowerOut(j),powerOutMax);
        }
        populationPowerOut.get(i).set(j,p);
        if (j>=model.getM()){
            if (!model.checkXLocationIsLegal(x)){
                if (x<locationMin){
                    x = locationMin;
                }
                if (x>locationMax){
                    x = locationMax;
                }
                pL++;
            }
            if (!model.checkYLocationIsLegal(y)){
                if (y<locationMin){
                    y = locationMin;
                }
                if (y>locationMax){
                    y = locationMax;
                }
                pL++;
            }
        }
        populationLocation.get(i).set(j,new Double[]{x,y});
    }

    /**重启功能*/
    private void restartPopulation(){
        double alpha = 0.0;
        for (int i = 0; i < d; i++) {
            int is = RandomUtil.getRandIndex(0.0,1.0) < (1.0/d) ? 1:0;
            alpha += is * Math.pow(2,-i);
        }
        double[] currentFitness = new double[np];
        /*计算当代适应度值*/
        for (int i = 0; i < np; i++) {
            currentFitness[i] = model.countEE(populationLocation.get(i),populationPowerOut.get(i));
        }
        Finder.quickSort(currentFitness,0,currentFitness.length-1);

        for (int i = 1; i < np; i++) {
            if ((currentFitness[i] - currentFitness[i-1]) <= ERROR && g > 25){
                if (RandomUtil.getRandIndex(0.0,1.1)>=0.5){
                    int jRand = RandomUtil.getRandIndex(0,d);
                    for (int j = 0; j < d; j++) {
                        if (j==jRand){
                            double p = model.getMinPowerOut(j) + RandomUtil.getRandIndex(0.0,1.0) * (powerOutMax - model.getMinPowerOut(j));
                            double x = locationMin + RandomUtil.getRandIndex(0.0,1.0) * (locationMax - locationMin);
                            double y = locationMin + RandomUtil.getRandIndex(0.0,1.0) * (locationMax - locationMin);
                            updatePopulation(i,j,p,x,y);
                        }
                    }
                }else {
                    int jRand = RandomUtil.getRandIndex(0,d);
                    for (int j = 0; j < d; j++) {
                        if (jRand == j){
                            int symbol = RandomUtil.getRandIndex(0.0,1.0) > 0.5 ? 1 : -1;
                            double p = populationPowerOut.get(i).get(j) + symbol * alpha * RandomUtil.getRandIndex(0.0,1.0) * (powerOutMax - model.getMinPowerOut(j));
                            double x = locationMin + RandomUtil.getRandIndex(0.0,1.0) * (locationMax - locationMin);
                            double y = locationMin + RandomUtil.getRandIndex(0.0,1.0) * (locationMax - locationMin);
                            updatePopulation(i,j,p,x,y);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void select() {
        baseSelect();
        restartPopulation();
//        System.out.println("IDE :" +g+" 最佳值为 "+bestValue);
    }

    /**交叉操作*/
    @Override
    public void crossover() {
        for (int i = 0; i < np; i++) {
            int jRand = ThreadLocalRandom.current().nextInt(0, d);
            for (int j = 0; j < d; j++) {
                double k = ThreadLocalRandom.current().nextDouble(0,1);
                if ((k <= crArray[i]) || (j == jRand)){
                    childPowerOut.get(i).set(j,mutantPowerOut.get(i).get(j));
                    childLocation.get(i).set(j,new Double[]{mutantLocation.get(i).get(j)[0],mutantLocation.get(i).get(j)[1]});
                }else {
                    childPowerOut.get(i).set(j,populationPowerOut.get(i).get(j));
                    childLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
                }
            }
        }
    }

    /**更新Cr、F值*/
    private void updateCrAndF(){
        for (int i = 0; i < np; i++) {
            crArray[i] = crMax + (crMin - crMax)*Math.pow((1-(double)g/GENERATION_MAX),4);
            fArray[i] = RandomUtil.getRandIndex(fMin,fMax);
        }
    }


    @Override
    public void mutation() {
        updateCrAndF();

        for (int i = 0; i < np; i++) {
            int[] randIndex = RandomUtil.getNotRepeatIndexOf3(i,0,d);
            for (int j = 0; j < d; j++) {
                if (RandomUtil.getRandIndex(0.0,1.0)>=Math.pow((1-(double)g/GENERATION_MAX),2)){
                    /*计算三角变量顺序*/
                    double[] randIndexFitness = new double[3];
                    System.arraycopy(fitness, 0, randIndexFitness, 0, randIndex.length);
                    int[] randIndexSort = Finder.getMaxNoIndex(randIndexFitness,3);
                    /*计算凸组合向量*/
                    double[] p = new double[3] ;
                    double cPowerOutValue = 0 ,cLocationXValue=0,cLocationYValue=0;
                    p[2] = 1;
                    p[1] = RandomUtil.getRandIndex(0.75,1);
                    p[0] = RandomUtil.getRandIndex(0.5,p[1]);
                    double pSum = CountUtil.countSum(p);
                    for (int index = 0; index < p.length; index++) {
                        double wi = (p[index]/pSum);
                        cPowerOutValue +=  wi* populationPowerOut.get(randIndex[index]).get(j);
                        cLocationXValue +=  wi* populationLocation.get(randIndex[index]).get(j)[0];
                        cLocationYValue +=  wi* populationLocation.get(randIndex[index]).get(j)[1];
                    }
                    mutantPowerOut.get(i).set(j,getLegalPowerOut(new double[]{f},new double[]{cPowerOutValue,
                            populationPowerOut.get(randIndexSort[0]).get(j),populationPowerOut.get(randIndexSort[2]).get(j)},j,DifferentialStrategy.DE_TRIANGLE_RAND_ONE));
                    if (j>=model.getM()){
                        mutantLocation.get(i).set(j,getLegalLocation(new double[]{f},
                                new double[]{cLocationXValue,populationLocation.get(randIndexSort[0]).get(j)[0], populationLocation.get(randIndexSort[2]).get(j)[0]}
                                ,new double[]{cLocationYValue,populationLocation.get(randIndexSort[0]).get(j)[1],populationLocation.get(randIndexSort[2]).get(j)[1]}
                                ,DifferentialStrategy.DE_TRIANGLE_RAND_ONE));
                    }else {
                        mutantLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
                    }
                }else {
                    baseMutation(i,j,randIndex);
                }

            }
        }
    }
}
