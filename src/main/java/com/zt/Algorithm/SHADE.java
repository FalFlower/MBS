package com.zt.Algorithm;

import com.zt.MBS.Model;
import com.zt.Tools.Finder;
import com.zt.Tools.RandomUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public class SHADE extends BaseAlgorithm{
    /**SHADE算法参数*/
    /**
     * H memoryF 与 memoryCr 尺寸大小
     * kIndex memoryF 与 memoryCr 索引
     * pBestValueIndex 较好解的前%n中的一个解的索引
     * n 指定获取前%n较好解
     * crArray、fArray 分别存储当代每个个体的cr与f值
     * */
    private int H = np;
    private int memIndex = 0;
    private int pBestValueIndex = 0;
    private double n = 0;
    private final double N_2_NP =  2.0/np;
    private double[] crArray = new double[np];
    private double[] fArray = new double[np];

    /**
     * memoryF 存储最佳F值
     * memoryCr 存储最佳Cr值
     * */
    private double[] memoryF = new double[H];
    private double[] memoryCr = new double[H];

    /**difIndex 适应度差值索引
     * */
    private int differentFitnessIndex = 0;
    private double differentFitnessSum = 0;
    private double[] differentFitnessArray = new double[H];

    /**失败的个体
     * defeatPopulationPowerOut、defeatPopulationLocation 失败个体
     * aIndex 失败个体外部存储索引 每当外部档案的大小超过| A |时，将删除随机选择的元素，为新插入的元素腾出空间
     * */
    private List<List<Double>> defeatPopulationPowerOut = new ArrayList<>();
    private List<List<Double[]>> defeatPopulationLocation = new ArrayList<>();

    public SHADE(Model model, int efsNum) {
        super(model, efsNum);
        init();
    }

    /**初始化SHADE算法独有参数*/
    private void init() {
        for (int i = 0; i < np; i++) {
            memoryF[i] = 0.5;
            memoryCr[i] = 0.5;
        }
    }


    /**更新mCr与mF*/
    public void updateMemoryCrAndF(List<Double> sCr,List<Double> sF){
        if (sCr.size()==0){
            memIndex = (memIndex+1) % np;
            return;
        }
        double midMemCr = 0 , midMemF2 = 0 , midMemF = 0;
        if (differentFitnessIndex>0){
            for (double v : differentFitnessArray) {
                differentFitnessSum+=v;
            }
            for (int i = 0; i < differentFitnessIndex; i++) {
                double weight = differentFitnessArray[i] / differentFitnessSum;
                midMemCr += (weight * sCr.get(i));
                midMemF2 += (weight * sF.get(i) * sF.get(i));
                midMemF += (weight * sF.get(i));
            }
            memoryCr[memIndex] = midMemCr;
            memoryF[memIndex] = midMemF2 / midMemF;
            memIndex = (memIndex+1) % np;
        }
    }



    @Override
    public void select() {
        /*成功个体使用的CRi和Fi值被记录在SCR和SF中*/
        List<Double> sCr = new ArrayList<>();
        List<Double> sF = new ArrayList<>();

        /*重置计算meanWa相关数据*/
        differentFitnessIndex = 0;
        differentFitnessSum = 0;
        differentFitnessArray = new double[H];

        for (int i = 0; i < np; i++) {
            double newFitness = model.countEE(childLocation.get(i),childPowerOut.get(i));
            double oldFitness = fitness[i];
            if (newFitness < oldFitness){
                /*变异优于父代*/
                List<Double[]> location = new ArrayList<>();
                List<Double> pOut = new ArrayList<>();
                for (int j = 0; j < populationLocation.get(i).size(); j++) {
                    location.add(new Double[]{childLocation.get(i).get(j)[0],childLocation.get(i).get(j)[1]});
                    pOut.add(childPowerOut.get(i).get(j));
                }
                populationLocation.set(i,location);
                populationPowerOut.set(i,pOut);
                /*存储失败的父代基因*/
                List<Double> power = new ArrayList<>(populationPowerOut.get(i));
                List<Double[]> population = new ArrayList<>();
                for (Double[] doubles : populationLocation.get(i)) {
                    population.add(new Double[]{doubles[0],doubles[1]});
                }
                if (defeatPopulationPowerOut.size()<=np){
                    defeatPopulationPowerOut.add(power);
                    defeatPopulationLocation.add(population);
                }else {
                    int randIndex = RandomUtil.getRandIndex(0,H);
                    defeatPopulationPowerOut.set(randIndex,power);
                    defeatPopulationLocation.set(randIndex,population);
                }

                /*更新memCr、memF等参数*/
                sCr.add(crArray[i]);
                sF.add(fArray[i]);

                differentFitnessArray[differentFitnessIndex] = oldFitness - newFitness;
                differentFitnessIndex++;
            }
        }
        updateMemoryCrAndF(sCr,sF);
//        System.out.println("SHADE: "+g);
//        System.out.println("IDE :" +g+" 最佳值为 "+bestValue);
    }


    /**为一代中的每一个个体更新CR、F、pBest等参数
     * @param i*/
    public void updateCrAndF(int i){
        int index = RandomUtil.getRandIndex(0,H);
        crArray[i] = RandomUtil.getNormalDistribution(memoryCr[index],0.1);
        fArray[i] = RandomUtil.getCauchyDistribution(memoryF[index],0.1);

        /*使用n浮动策略，n应在[0.05~0.25]之间浮动*/
        n =Math.random() * (0.2 - N_2_NP) + N_2_NP;
        int[] indexArray = Finder.getMaxNoIndex(fitness, (int) Math.round(n*np));
        pBestValueIndex = RandomUtil.getRandIndexFromArray(indexArray);
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


    @Override
    public void mutation() {
        double[] powerOutValues = new double[4];
        double[] locationXValues = new double[4];
        double[] locationYValues = new double[4];
        for (int i = 0; i < np; i++) {
            updateCrAndF(i);
            /*defeatRandIndex ≠ i ≠ randIndex*/
            int defeatRandIndex = RandomUtil.getNotRepeatIndexOf3(i,0,np+defeatPopulationPowerOut.size())[0];
            List<Double> powerRandDefeat = RandomUtil.getRandPowerValueFromTwoArray(populationPowerOut,np,defeatPopulationPowerOut,defeatPopulationPowerOut.size(),defeatRandIndex,d);
            List<Double[]> locationRandDefeat = RandomUtil.getRandLocationValueFromTwoArray(populationLocation,np,defeatPopulationLocation,defeatPopulationLocation.size(),defeatRandIndex,d);
            int[] randIndexArray = RandomUtil.getNotRepeatIndexOf3(i,0,np);
            int randIndex = randIndexArray[0] == defeatRandIndex ? randIndexArray[1]:randIndexArray[0];

            for (int j = 0; j < d; j++) {
                /*更新传递参数*/
                powerOutValues[0] = populationPowerOut.get(pBestValueIndex).get(j);
                powerOutValues[1] = populationPowerOut.get(i).get(j);
                powerOutValues[2] = populationPowerOut.get(randIndex).get(j);
                powerOutValues[3] = powerRandDefeat.get(j);

                locationXValues[0] = populationLocation.get(pBestValueIndex).get(j)[0];
                locationYValues[0] = populationLocation.get(pBestValueIndex).get(j)[1];
                locationXValues[1] = populationLocation.get(i).get(j)[0];
                locationYValues[1] = populationLocation.get(i).get(j)[1];
                locationXValues[2] = populationLocation.get(randIndex).get(j)[0];
                locationYValues[2] = populationLocation.get(randIndex).get(j)[1];
                locationXValues[3] = locationRandDefeat.get(j)[0];
                locationYValues[3] = locationRandDefeat.get(j)[1];

                double p = getLegalPowerOut(new double[]{fArray[i]},powerOutValues,j,DifferentialStrategy.DE_CURRENT_TO_BEST_ONE);
                mutantPowerOut.get(i).set(j,p);
                if (j>=model.getM()){
                    mutantLocation.get(i).set(j,getLegalLocation(new double[]{fArray[i]},locationXValues,locationYValues,DifferentialStrategy.DE_CURRENT_TO_BEST_ONE));
                }else {
                    mutantLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
                }
            }
        }
    }


}
