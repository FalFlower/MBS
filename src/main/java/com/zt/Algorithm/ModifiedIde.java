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
 * 根据用户量来动态调整使用变异策略 因为用户多的时候 解空间相对较少，不需要很强的开发能力
 * 用户相对较少时使用IDE的三角策略
 * 用户比较多时更多的使用DE的策略
 * 可参考的控制量有：
 *     1）当前客流量/计划中的平均客流量 rand(0,1)>m/M时使用三角突变 （Test）如果当前客流量大于平均客流量  全部使用DE算法
 *     2）参考系统的平均总负载sLoadRate rand(0,1)>sLoadRate时使用三角突变 （Test）跟原算法判断条件 && / ||
 */
public class ModifiedIde extends BaseAlgorithm {
    private final double CR_MAX = 0.8;
    private final double CR_MIN = 0.1;
    private final double F_MAX = 0.8;
    private final double F_MIN = 0.2;
    private double[] crArray = new double[np];
    private double[] fArray = new double[np];

    private final int IDE_MUT = 0;
    private final int ONLY_TRIANGLE_MUT = 1;
    private final int ONLY_RAND_ONE = 2;
    private final int ONLY_RAND_TWO = 3;
    private final int TRIANGLE_PLUS_RAND_TWO = 4;

    private final int RESTART_ERROR = 0;
    private final int RESTART_DISTANCE = 1;
    private final int RESTART_SUCCESS_RATE = 2;
    /**计算成功率*/
    private double successRate = 0;

    private final double ERROR = Math.pow(10,-30);
    private final double DISTANCE_ERROR = Math.pow(10,-6);
    private final double SUCCESS_RATE_ERROR = 0.1;

    private final int CR_G = 0;
    private final int CR_RAND = 1;
    private final int CR_RAND_NI = 2;

    private final int F_RAND = 0;
    private final int F_RAND_CI = 1;
    private final int F_FIXED = 2;

    /**指定阈值*/
    private final double RT_THRESHOLD = (double) 2700/3000;

    /**给定新的策略：前半段使用rand/2突变，后半段使用三角突变*/
    private final int P_RAND_TWO_L_TRI = 5;
    /**给定新的策略：前半段使用IDE突变，后半段使用三角突变*/
    private final int P_IDE_L_TRI = 6;
    /**给定新的策略：前半段使用IDE变体突变(TRI + RAND_TWO)，后半段使用三角突变*/
    private final int P_TRI_AND_RAND_TWO_L_TRI = 7;

    private final double F_FIXED_VALUE = 0.6;

    /**统一修改策略*/
    int mutStrategy = P_IDE_L_TRI;
    int crStrategy = CR_G;
    int fStrategy = F_FIXED;
    int restartStrategy = RESTART_DISTANCE;
    int restartAlpha = 12;


    public ModifiedIde(Model model, int efsNum) {
        super(model, efsNum,250);
        double currentUserT = (double) model.getN() / AVA_PEOPLE_NUM;
        if (currentUserT>currentUserT){
            restartStrategy  = RESTART_DISTANCE;
        }else {
            restartStrategy  = RESTART_ERROR;
        }
    }


    /**基础的选择操作*/
    @Override
    protected void baseSelect(){
        int successNum = 0;
        for (int i = 0; i < np; i++) {
            double newFitness = model.countEE(childLocation.get(i),childPowerOut.get(i));
            double oldFitness = fitness[i];
            if (newFitness < oldFitness){
                List<Double[]> location = new ArrayList<>();
                List<Double> pOut = new ArrayList<>();
                for (int j = 0; j < populationLocation.get(i).size(); j++) {
                    location.add(new Double[]{childLocation.get(i).get(j)[0],childLocation.get(i).get(j)[1]});
                    pOut.add(childPowerOut.get(i).get(j));
                }
                populationLocation.set(i,location);
                populationPowerOut.set(i,pOut);
                successNum++;
            }
        }
        successRate = (double) successNum / np;
    }

    @Override
    public void select() {
        baseSelect();
        restartPopulation();
//        System.out.println("当前代数为："+g+"最佳值为："+bestValue);
    }

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
            if (crStrategy==CR_RAND){
                crArray[i] = RandomUtil.getRandIndex(CR_MIN,CR_MAX);
            }else {
                crArray[i] = CR_MAX + (CR_MIN - CR_MAX)*Math.pow((1-(double)g/GENERATION_MAX),4);
            }
            if (fStrategy==F_FIXED){
                fArray[i] = F_FIXED_VALUE;
            }else {
                fArray[i] = RandomUtil.getRandIndex(F_MIN,F_MAX);
            }
        }
    }


    //todo 需要修改1
    /**生成不同策略的随机数组*/
    private int[] getRandomIndexArrayByStrategy(int i  , int mutStrategy){
        int[] randIndex =  new int[0];
        if (mutStrategy==ONLY_RAND_ONE || mutStrategy==IDE_MUT || mutStrategy==ONLY_TRIANGLE_MUT || mutStrategy == P_IDE_L_TRI){
            randIndex = RandomUtil.getNotRepeatIndexOf3(i,0,d);
        }else if (mutStrategy==ONLY_RAND_TWO || mutStrategy==TRIANGLE_PLUS_RAND_TWO || mutStrategy ==P_RAND_TWO_L_TRI || mutStrategy == P_TRI_AND_RAND_TWO_L_TRI){
            int[] midRandIndex = RandomUtil.randomSet(0,d,6);
            randIndex = new int[5];
            int index = 0;
            for (int k : midRandIndex) {
                if (i != k && index < 5) {
                    randIndex[index++] = k;
                }
            }
        }
        return randIndex;
    }

    /**基础的三角突变*/
    private void baseTriangleMut(int i ,int j , int[] randIndex,int randIndexNum){
        /*计算三角变量顺序*/
        double[] randIndexFitness = new double[randIndexNum];

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
    }

    //todo 需要修改2
    /**根据不同策略执行不同变异策略*/
    private void doMutByStrategy(int i , int j ,int[] randIndex,int mutStrategy){
        if (mutStrategy==IDE_MUT){
            if (RandomUtil.getRandIndex(0.0,1.0)>=Math.pow((1-(double)g/GENERATION_MAX),2)){
                baseTriangleMut(i,j,randIndex,randIndex.length);
            }else {
                baseMutation(i,j,randIndex);
            }
        }
        if (mutStrategy == ONLY_TRIANGLE_MUT){
            baseTriangleMut(i,j,randIndex,randIndex.length);
        }
        if (mutStrategy == ONLY_RAND_ONE){
            baseMutation(i,j,randIndex);
        }
        if (mutStrategy == ONLY_RAND_TWO){
            baseRandTwo(i,j,randIndex);
        }
        if (mutStrategy == TRIANGLE_PLUS_RAND_TWO){
            if (RandomUtil.getRandIndex(0.0,1.0)>=Math.pow((1-(double)g/GENERATION_MAX),2)){
                baseTriangleMut(i,j,randIndex,randIndex.length);
            }else {
                baseRandTwo(i,j,randIndex);
            }
        }
        if (mutStrategy == P_RAND_TWO_L_TRI){
            double currentUserT = (double) model.getN() / AVA_PEOPLE_NUM;
            if (currentUserT > RT_THRESHOLD){
                baseTriangleMut(i,j,randIndex,randIndex.length);
            }else {
                baseRandTwo(i,j,randIndex);
            }
        }
        if (mutStrategy == P_IDE_L_TRI){
            double currentUserT = (double) model.getN() / AVA_PEOPLE_NUM;
            if (currentUserT > RT_THRESHOLD){
                baseTriangleMut(i,j,randIndex,randIndex.length);
            }else {
                if (RandomUtil.getRandIndex(0.0,1.0)>=Math.pow((1-(double)g/GENERATION_MAX),2)){
                    baseTriangleMut(i,j,randIndex,randIndex.length);
                }else {
                    baseMutation(i,j,randIndex);
                }
            }
        }
        if (mutStrategy == P_TRI_AND_RAND_TWO_L_TRI){
            double currentUserT = (double) model.getN() / AVA_PEOPLE_NUM;
            if (currentUserT > RT_THRESHOLD){
                baseTriangleMut(i,j,randIndex,randIndex.length);
            }else {
                if (RandomUtil.getRandIndex(0.0,1.0)>=Math.pow((1-(double)g/GENERATION_MAX),2)){
                    baseTriangleMut(i,j,randIndex,randIndex.length);
                }else {
                    baseRandTwo(i,j,randIndex);
                }
            }
        }
    }

    /**执行DE/Rand/2策略*/
    private void baseRandTwo(int i , int j ,int[] randIndex){
        double p = getLegalPowerOut(new double[]{f},new double[]{populationPowerOut.get(randIndex[0]).get(j),
                populationPowerOut.get(randIndex[1]).get(j),populationPowerOut.get(randIndex[2]).get(j)
                ,populationPowerOut.get(randIndex[3]).get(j),populationPowerOut.get(randIndex[4]).get(j)},j,DifferentialStrategy.DE_RAND_TWO);
        mutantPowerOut.get(i).set(j,p);
        if (j>=model.getM()){
            mutantLocation.get(i).set(j,getLegalLocation(new double[]{f},
                    new double[]{populationLocation.get(randIndex[0]).get(j)[0],populationLocation.get(randIndex[1]).get(j)[0], populationLocation.get(randIndex[2]).get(j)[0]
                            , populationLocation.get(randIndex[3]).get(j)[0], populationLocation.get(randIndex[4]).get(j)[0]}
                    ,new double[]{populationLocation.get(randIndex[0]).get(j)[1],populationLocation.get(randIndex[1]).get(j)[1],populationLocation.get(randIndex[2]).get(j)[1]
                            ,populationLocation.get(randIndex[3]).get(j)[1],populationLocation.get(randIndex[4]).get(j)[1]}
                    ,DifferentialStrategy.DE_RAND_TWO));
        }else {
            mutantLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
        }
    }

    @Override
    public void mutation() {
        updateCrAndF();
        for (int i = 0; i < np; i++) {
            int[] randIndex = getRandomIndexArrayByStrategy(i,mutStrategy);
            for (int j = 0; j < d; j++) {
                doMutByStrategy(i,j,randIndex,mutStrategy);
            }
        }
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

    /**根据不同的重启策略，返回重启决策*/
    private boolean isRestart(int i , double[] currentFitness){
        boolean result = true;
        if (restartStrategy == RESTART_ERROR){
            result =  (currentFitness[i] - currentFitness[i-1]) <= ERROR;
        }
        if (restartStrategy == RESTART_DISTANCE){
            result = countDistanceLocation(populationLocation.get(i),populationLocation.get(i-1)) < DISTANCE_ERROR
                    || countDistancePower(populationPowerOut.get(i),populationPowerOut.get(i-1))<DISTANCE_ERROR;
        }
        if (restartStrategy == RESTART_SUCCESS_RATE){
            result  = successRate < SUCCESS_RATE_ERROR;
        }
        return result;
    }

    /**重启功能*/
    private void restartPopulation(){
        double alpha = 0.0;
        for (int i = 0; i < restartAlpha; i++) {
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
            if (isRestart(i,currentFitness) && g > 25){
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


    /**使用欧氏距离来代替重启的策略中的误差限*/
    private double countDistancePower(List<Double> pop1 , List<Double> pop2){
        double distance = 0;
        for (int i = 0; i < d; i++) {
            distance += (Math.pow(pop1.get(i) - pop2.get(i),2));
        }
        return Math.sqrt(distance);
    }
    private double countDistanceLocation(List<Double[]> pop1 , List<Double[]> pop2){
        double xDistanceMid1 = 0,xDistanceMid2 = 0;
        double yDistanceMid1 = 0,yDistanceMid2 = 0;
        for (int i = 0; i < d; i++) {
            xDistanceMid1  = (xDistanceMid1+pop1.get(i)[0])/2;
            xDistanceMid2  = (xDistanceMid2+pop1.get(i)[0])/2;
            yDistanceMid1  = (yDistanceMid1+pop1.get(i)[1])/2;
            yDistanceMid2  = (yDistanceMid2+pop1.get(i)[1])/2;
        }
        return Math.sqrt(Math.pow(xDistanceMid1-xDistanceMid2,2)+Math.pow(yDistanceMid1-yDistanceMid2,2));
    }
}
