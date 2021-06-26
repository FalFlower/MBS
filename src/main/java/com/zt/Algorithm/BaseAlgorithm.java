package com.zt.Algorithm;

import com.zt.MBS.Model;
import com.zt.MBS.ModelParameter;
import com.zt.Tools.ExcelOperate;
import com.zt.Tools.Finder;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public abstract class BaseAlgorithm extends DifferentialStrategy implements BaseFunction  {
    protected Model model;

    protected final int AVA_PEOPLE_NUM = 3000;

    /**坐标以及传输功率的上下界*/
    protected final int locationMin = ModelParameter.MIN_LOCATION;
    protected final int locationMax = ModelParameter.MAX_LOCATION;

    protected final double powerOutMin = ModelParameter.MIN_POWER_OUT;
    protected final double powerOutMax = ModelParameter.MAX_POWER_OUT;

    /**SHADE的传统参数*/
    protected double cr = 0.5;
    protected double f = 0.5;
    protected int np = 100 ;
    protected int d ;
    protected  int GENERATION_MAX = 3000;
    protected int g = 0;
    protected int EFS;
    protected int efs = 0;
    /**pC 统计出界次数*/
    protected long pC = 0;
    protected  long pL = 0;

    /**染色体为两个List<List<double>>类型组成，一个是坐标基因序列，一个是传输功率基因序列，每个由M+K个基因组成*/
    protected List<List<Double[]>> populationLocation = new ArrayList<>();
    protected List<List<Double>> populationPowerOut = new ArrayList<>();
    protected List<List<Double[]>> mutantLocation = new ArrayList<>();
    protected List<List<Double>> mutantPowerOut = new ArrayList<>();
    protected List<List<Double[]>> childLocation = new ArrayList<>();
    protected List<List<Double>> childPowerOut = new ArrayList<>();

    /**bestValues：各代的最优值
     * bestSolutions：各代的最优解
     * bestValue：算法最优值
     * bestSolution：算法最优解
     * fitness：存储当前代中所有个体的适应度值
     * */
    protected double bestValue = 0.0;
    protected List<Double[]> bestSolutionLocation =  new ArrayList<>();
    protected List<Double> bestSolutionPowerOut =  new ArrayList<>();

    protected double[] fitness;
    protected double[][] allFitness;
    protected double[] allBestValues;
    protected ReturnValue result = new ReturnValue();

    public BaseAlgorithm(Model model,int efsNum) {
        this.model = model;
        initValues(model.getM()+model.getK(),efsNum);
    }

    public BaseAlgorithm(Model model,int efsNum,int np) {
        this.model = model;
        this.np = np;
        initValues(model.getM()+model.getK(),efsNum);
    }

    /**入口*/
    @Override
    public ReturnValue justDoIt(){
        repeatDone();
        return result;
    }

    protected void repeatDone() {
        efs = 0;
        g = 0;
        long choseBestTime = 0,mutationTime=0,crossoverTime=0,selectTime=0;
        long startTime,endTime;
        while (efs < EFS){
            startTime = System.currentTimeMillis();
            choseBest();
            endTime = System.currentTimeMillis();
            choseBestTime+=(endTime-startTime);

            efs += d;

            startTime = System.currentTimeMillis();
            mutation();
            endTime = System.currentTimeMillis();
            mutationTime+=(endTime-startTime);

            startTime = System.currentTimeMillis();
            crossover();
            endTime = System.currentTimeMillis();
            crossoverTime+=(endTime-startTime);

            startTime = System.currentTimeMillis();
            select();
            endTime = System.currentTimeMillis();
            selectTime+=(endTime-startTime);

            g++;

        }
        System.out.println("测试    DE评价时间："+choseBestTime+"    DE变异时间："+mutationTime+"    DE交叉时间："+crossoverTime+"    DE选择时间："+selectTime);
        /*更新返回值*/
        result.setBestLocation(bestSolutionLocation);
        result.setBestPowerOut(bestSolutionPowerOut);
        result.setBestValue(bestValue);
        result.setgLongNum(g);

        System.out.println("出界次数统计 pC: "+pC+" pL: "+pL);
    }

    /**初始化数据*/
    private void initValues(int bsNum, int efsNum){
        d = bsNum;
        /*修改次数*/
        EFS = efsNum * d;
        GENERATION_MAX = efsNum;
        allFitness = new double[efsNum][np];
        allBestValues = new double[efsNum];
        fitness = new double[np];

        for (int i = 0; i < np; i++) {
            List<Double> popPout = new ArrayList<>();
            List<Double> mutPout = new ArrayList<>();
            List<Double> chiPout = new ArrayList<>();
            List<Double[]> popLocation = new ArrayList<>();
            List<Double[]> mutLocation = new ArrayList<>();
            List<Double[]> chiLocation = new ArrayList<>();

            double[][] bsLocation = model.getBsLocationArray();
            /*i==0 : Model 初始化的一条染色体  i>0 : DE算法随机初始化*/
            if (i==0){
                double[] bsPowerOut = model.getPowerOutArray();
                for (int j = 0; j < d; j++) {
                    Double[] location = new Double[2];
                    location[0] = bsLocation[j][0];
                    location[1] = bsLocation[j][1];
                    popLocation.add(location);
                    popPout.add(bsPowerOut[j]);
                }
            }else {
                for (int j = 0; j < d; j++) {
                    Double[] location = new Double[2];
                    if (j>=model.getM()){
                        location[0] = ThreadLocalRandom.current().nextDouble(locationMin,locationMax);
                        location[1] = ThreadLocalRandom.current().nextDouble(locationMin,locationMax);
                    }else {
                        location[0] = bsLocation[j][0];
                        location[1] = bsLocation[j][1];
                    }
                    popLocation.add(location);
                    popPout.add(model.getMinPowerOut(j) >= powerOutMax ? powerOutMax : ThreadLocalRandom.current().nextDouble(model.getMinPowerOut(j),powerOutMax));
                }
            }

            populationLocation.add(popLocation);
            populationPowerOut.add(popPout);
            for (int j = 0; j < d; j++) {
                mutPout.add(0.0);
                mutLocation.add(new Double[]{0.0,0.0});
            }
            mutantLocation.add(mutLocation);
            mutantPowerOut.add(mutPout);
            for (int j = 0; j < d; j++) {
                chiPout.add(0.0);
                chiLocation.add(new Double[]{0.0,0.0});
            }
            childLocation.add(chiLocation);
            childPowerOut.add(chiPout);
        }

    }

    /**更新bestValue and bestSolution等*/
    @Override
    public void choseBest() {
        evaluateCurrentPopulation();

        if (new Double(0.0).equals(bestValue) || bestValue > Finder.min(fitness)){
            bestValue = Finder.min(fitness);
            bestSolutionLocation = populationLocation.get(Finder.minIndex(fitness));
            bestSolutionPowerOut = populationPowerOut.get(Finder.minIndex(fitness));
        }

        allBestValues[g] = bestValue;

    }


    /**输出每代的适应度值和最佳适应度值*/
    public void outputFitnessValues(int index,int dayTime)throws IOException {
//        ExcelOperate.outputAlgorithmLastNpSolution(populationLocation,populationPowerOut,np,d,index);
        ExcelOperate.outputAllFitnessValue(allFitness,index,dayTime);
        ExcelOperate.outputAllBestFitnessValue(allBestValues,index,dayTime);
    }


    /**基础的变异操作*/
    protected void baseMutation(int i,int j,int[] randIndex){
        double p = getLegalPowerOut(new double[]{f},new double[]{populationPowerOut.get(randIndex[0]).get(j),
                populationPowerOut.get(randIndex[1]).get(j),populationPowerOut.get(randIndex[2]).get(j)},j,DifferentialStrategy.DE_RAND_ONE);
        mutantPowerOut.get(i).set(j,p);
        if (j>=model.getM()){
            mutantLocation.get(i).set(j,getLegalLocation(new double[]{f},
                    new double[]{populationLocation.get(randIndex[0]).get(j)[0],populationLocation.get(randIndex[1]).get(j)[0], populationLocation.get(randIndex[2]).get(j)[0]}
                    ,new double[]{populationLocation.get(randIndex[0]).get(j)[1],populationLocation.get(randIndex[1]).get(j)[1],populationLocation.get(randIndex[2]).get(j)[1]}
                    ,DifferentialStrategy.DE_RAND_ONE));
        }else {
            mutantLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
        }
    }

    /**基础的选择操作*/
    protected void baseSelect(){
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

            }
        }
    }

    /**得到合法的传输功率
     *  如果没有用户连接，就让基站陷入睡眠模式，令 pOut = 0；
     * */
    protected double getLegalPowerOut(double[] f , double[] values, int bsIndex,int strategy){
        if (model.getBsConnectNum(bsIndex) == 0){
            return  0.0;
        }
        double pOut = 0.0;
        switch (strategy){
            case DifferentialStrategy.DE_RAND_ONE:
                pOut = deRandOne(f[0],values[0],values[1],values[2]) ;
                break;
            case DifferentialStrategy.DE_CURRENT_TO_BEST_ONE:
                pOut = deCurrentToBestOne(f[0],values[0],values[1],values[2],values[3]) ;
                break;
            case DifferentialStrategy.DE_CURRENT_TO_BEST_W_ONE:
                pOut = deCurrentToBestWeightOne(f[0],f[1],values[0],values[1],values[2],values[3]) ;
                break;
            case DifferentialStrategy.DE_TRIANGLE_RAND_ONE:
                pOut = deTriangleRandOne(f[0],values[0],values[1],values[2]) ;
                break;
            case DifferentialStrategy.DE_RAND_TWO:
                pOut = deRandTwo(f[0],values[0],values[1],values[2],values[3],values[4]) ;
                break;
            default:
                break;
        }
        while (!model.checkPowerOutIsLegal(pOut,bsIndex)){
            pC++;
            /*如果变异产生不合法的数据，就重新生成新的值*/
            pOut = model.getMinPowerOut(bsIndex) >= powerOutMax ? powerOutMax : ThreadLocalRandom.current().nextDouble(model.getMinPowerOut(bsIndex),powerOutMax);
        }
        return pOut;
    }

    /**得到合法的基站坐标*/
    protected Double[] getLegalLocation(double[] f ,double[] xValues,double[] yValues,int strategy){
        Double[] location = new Double[2];
        double x = 0, y =0;
        switch (strategy){
            case DifferentialStrategy.DE_RAND_ONE:
                x= deRandOne(f[0],xValues[0],xValues[1],xValues[2]);
                y = deRandOne(f[0],yValues[0],yValues[1],yValues[2]);
                break;
            case DifferentialStrategy.DE_CURRENT_TO_BEST_ONE:
                x= deCurrentToBestOne(f[0],xValues[0],xValues[1],xValues[2],xValues[3]);
                y = deCurrentToBestOne(f[0],yValues[0],yValues[1],yValues[2],yValues[3]);
                break;
            case DifferentialStrategy.DE_CURRENT_TO_BEST_W_ONE:
                x= deCurrentToBestWeightOne(f[0],f[1],xValues[0],xValues[1],xValues[2],xValues[3]);
                y = deCurrentToBestWeightOne(f[0],f[1],yValues[0],yValues[1],yValues[2],yValues[3]);
                break;
            case DifferentialStrategy.DE_TRIANGLE_RAND_ONE:
                x= deTriangleRandOne(f[0],xValues[0],xValues[1],xValues[2]);
                y = deTriangleRandOne(f[0],yValues[0],yValues[1],yValues[2]);
                break;
            case DifferentialStrategy.DE_RAND_TWO:
                x= deRandTwo(f[0],xValues[0],xValues[1],xValues[2],xValues[3],xValues[4]);
                y = deRandTwo(f[0],yValues[0],yValues[1],yValues[2],yValues[3],yValues[4]);
                break;
            default:
                break;
        }
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
        location[0] = x;
        location[1] = y;
        return location;
    }

    /**评价当前种群所有个体*/
    protected void evaluateCurrentPopulation(){
        for (int i = 0; i < populationLocation.size(); i++) {
            double fit = model.countEE(populationLocation.get(i),populationPowerOut.get(i));;
            fitness[i] = fit;
            allFitness[g][i] = fit;
        }
    }


}
