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
public class JSO extends BaseAlgorithm{

    /**JSO参数
     * pBestRate 较好解的前%pBestRate中的一个解的索引
     * memorySize MCr、MF存储大小
     * maxNp、minNp 分别为最大最小种群规模
     * */
    private double pBestRate = 0.25;
    private double pBestRateMax = 0.25;
    private int maxNp;
    private int minNp = 4;

    /**
     * H memoryF 与 memoryCr 尺寸大小
     * kIndex memoryF 与 memoryCr 索引
     * pBestValueIndex 较好解的前%n中的一个解的索引
     * crArray、fArray、fwArray 分别存储当代每个个体的cr、f与加权f值
     * */
    private int H = 5;
    private int memIndex = 0;
    private int pBestValueIndex = 0;
    private double[] crArray = new double[np];
    private double[] fArray = new double[np];
    private double[] fwArray = new double[np];

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
    private double[] differentFitnessArray;

    /**失败的个体
     * defeatPopulationPowerOut、defeatPopulationLocation 失败个体
     * aIndex 失败个体外部存储索引 每当外部档案的大小超过| A |时，将删除随机选择的元素，为新插入的元素腾出空间
     * */
    private List<List<Double>> defeatPopulationPowerOut = new ArrayList<>();
    private List<List<Double[]>> defeatPopulationLocation = new ArrayList<>();

    public JSO(Model model, int efsNum) {
        super(model, efsNum,(int)(25 * Math.log(model.getM() + model.getK()) * Math.sqrt(model.getM() + model.getK())));
        maxNp = np;
        init();
    }
    /**初始化JSO算法独有参数*/
    private void init() {
        for (int i = 0; i < H; i++) {
                memoryF[i] = 0.5;
                memoryCr[i] = 0.8;
        }
    }

    /**入口*/
    @Override
    public ReturnValue justDoIt(){
        /*如果第二次执行 重新初始化种群*/
        if (np<maxNp){
            np=maxNp;
            populationLocation = new ArrayList<>();
            populationPowerOut = new ArrayList<>();

            for (int i = 0; i < np; i++) {
                List<Double> popPout = new ArrayList<>();
                List<Double[]> popLocation = new ArrayList<>();
                double[][] bsLocation = model.getBsLocationArray();
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
                populationLocation.add(popLocation);
                populationPowerOut.add(popPout);
            }
            cleanArrays();
        }
        repeatDone();
        return result;
    }

    /**更新种群大小*/
    private void updateNp(){
        int npNew = Math.round(((float) (minNp-maxNp)/EFS)*efs+maxNp);
        if (npNew < np){
            doUpdateNp(npNew);
            }
        }

    private void doUpdateNp(int npNew) {
        /*当NG+1<NG时，从种群中删除（NG-NG+1）排名最差的个体*/
        int[] minIndex = Finder.getMinNoIndex(fitness,np - npNew);
        List<List<Double[]>> newPopulationLocation = new ArrayList<>();
        List<List<Double>> newPopulationPowerOut = new ArrayList<>();
        for (int i = 0; i < np; i++) {
            if (Finder.searchIndex(minIndex,i)==-1){
                newPopulationLocation.add(populationLocation.get(i));
                newPopulationPowerOut.add(populationPowerOut.get(i));
            }
        }
        populationLocation = newPopulationLocation;
        populationPowerOut = newPopulationPowerOut;
        np = npNew;

        cleanArrays();
    }

    private void cleanArrays() {
        /*清空数组，改变长度 */
        allFitness = new double[GENERATION_MAX][np];
        allBestValues = new double[GENERATION_MAX];
        fitness = new double[np];
        differentFitnessArray = new double[np];
        crArray = new double[np];
        fArray = new double[np];
        fwArray = new double[np];

        mutantLocation = new ArrayList<>();
        childLocation = new ArrayList<>();
        for (int i = 0; i < np; i++) {
            List<Double> mutPout = new ArrayList<>();
            List<Double> chiPout = new ArrayList<>();
            List<Double[]> mutLocation = new ArrayList<>();
            List<Double[]> chiLocation = new ArrayList<>();
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

    /**更新记忆存储MemCr与MemF*/
    private void updateMemoryCrAndF(List<Double> sCr,List<Double> sF){
        if (sCr.size()==0){
            memIndex = (memIndex+1) % H;
            return;
        }
        double midMemCr = 0 , midMemCr2=0,midMemF2 = 0 , midMemF = 0;
        for (double v : differentFitnessArray) {
            differentFitnessSum+=v;
        }
        if (Finder.max(sCr)==0|| memoryCr[memIndex]==-1){
            memoryCr[memIndex] = -1;
        }else {
            for (int i = 0; i < differentFitnessIndex; i++) {
                double weight = differentFitnessArray[i] / differentFitnessSum;
                midMemCr2+= (weight * sCr.get(i)* sCr.get(i));
                midMemCr += (weight * sCr.get(i));
            }
            memoryCr[memIndex] =(midMemCr2/ midMemCr+memoryCr[memIndex])/2;
        }
        for (int i = 0; i < differentFitnessIndex; i++) {
            double weight = differentFitnessArray[i] / differentFitnessSum;
            midMemF2 += (weight * sF.get(i) * sF.get(i));
            midMemF += (weight * sF.get(i));
        }
        memoryF[memIndex] = (midMemF2 / midMemF+memoryF[memIndex])/2;
        memIndex = (memIndex+1) % H;
    }

    /**更新cr与f值
     * @param npIndex 第几个个体*/
    private void updateCrAndFs(int npIndex){
        int randIndex = RandomUtil.getRandIndex(0,H);
        if (randIndex == H-1){
            memoryCr[randIndex] = 0.9;
            memoryF[randIndex] = 0.9;
        }
        if (memoryCr[randIndex] < 0){
            crArray[npIndex] =0;
        }else {
            crArray[npIndex] = RandomUtil.getNormalDistribution(memoryCr[randIndex],0.1);
        }
        if (g < 0.25 * GENERATION_MAX){
            crArray[npIndex] = Math.max(crArray[npIndex],0.7);
        }else if (g < 0.5 * GENERATION_MAX){
            crArray[npIndex] = Math.max(crArray[npIndex],0.6);
        }
        fArray[npIndex] = RandomUtil.getCauchyDistribution(memoryF[randIndex],0.1);
        if (g < 0.6 * GENERATION_MAX && fArray[npIndex] > 0.7){
            fArray[npIndex] = 0.7;
        }
        if (g<0.2 * GENERATION_MAX){
            fwArray[npIndex] = fArray[npIndex] * 0.7;
        }else if (g<0.4 * GENERATION_MAX){
            fwArray[npIndex] = fArray[npIndex] * 0.8;
        }else {
            fwArray[npIndex] = fArray[npIndex] * 1.2;
        }

        /*获取pBestIndex*/
        int[] indexArray = Finder.getMaxNoIndex(fitness, (int) Math.round(pBestRate*np));
        pBestValueIndex = RandomUtil.getRandIndexFromArray(indexArray);
    }

    @Override
    public void select() {
        /*成功个体使用的CRi和Fi值被记录在SCR和SF中*/
        List<Double> sCr = new ArrayList<>();
        List<Double> sF = new ArrayList<>();

        /*重置计算meanWa相关数据*/
        differentFitnessIndex = 0;
        differentFitnessSum = 0;
        differentFitnessArray = new double[np];

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
        updateNp();
//        pBestRate = ( (pBestRateMax - pBestRateMax/2) / EFS)*efs + pBestRateMax/2;
        pBestRate = pBestRateMax*(1-0.5*efs/EFS);
//        System.out.println("pBestRate: "+pBestRate+" np:"+np+" gNum: "+g);
//        System.out.println("JSO :" +g+" 最佳值为 "+bestValue);
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
            updateCrAndFs(i);
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

                double p = getLegalPowerOut(new double[]{fArray[i],fwArray[i]},powerOutValues,j,DifferentialStrategy.DE_CURRENT_TO_BEST_W_ONE);
                mutantPowerOut.get(i).set(j,p);
                if (j>=model.getM()){
                    mutantLocation.get(i).set(j,getLegalLocation(new double[]{fArray[i],fwArray[i]},locationXValues,locationYValues,DifferentialStrategy.DE_CURRENT_TO_BEST_W_ONE));
                }else {
                    mutantLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
                }
            }
        }
    }
}
