package com.zt.MBS;


import com.zt.Tools.Finder;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public class Model {
    /**
     * pMax 基站最大传输功率
     *p0 基站无负载时基本功耗
     * pSleep 基站睡眠时基本功耗
     *Δp=deltaP 随负载变化的功耗斜率
     * σ^2=noise 噪声
     * thresholdT 移动基站加入的系统负载的阈值 [待定！！！！]
     * thresholdSINR 基站与用户建立连接的阈值
     * thresholdLoadRate 系统基站平均连接用户数
     * B=bandwidth 基站带宽
     * cD 距离无关系数
     * nMax 基站最大连接数量 [待定！！！！]
     * ε=epsilon 信号传播的衰减系数
     * α=alpha ECR评价指标权重[以下都待定！！！！]
     * β=beta
     * γ=gamma
     * δ=delta
     * ζ=zeta
     */
    private final double pMax = 6.3;
    private final double p0 = 56.0;
    private final double pSleep = 39.0;
    private final double deltaP = 2.6;
    private final double noise =Math.pow(10,-17.5)*20000;
    private final double thresholdT = 0.8;
    private final double thresholdSINR = 0.02;
    private final double thresholdLoadRate = 0.8;
    private final double bandwidth = 20.0;
    private final double cD = 1.7;
    private final double nMax = 150;
    private final double epsilon = 0.02;
    private double alpha = 0.2;
    private double beta = 0.2;
    private double gamma = 0.2;
    private double delta = 0.2;
    private double zeta = 0.2;

    /**
     * M 固定基站数量
     * K 移动基站数量
     * N 用户数量
     * locationMin、locationMin分别是坐标的上下限
     * */
    private final int M = 25;
    private int K = 0;

    private int N = 3000;

    private final int MAX_N = 3500;
    private final int MAX_K = 10;
    private final int locationMin = 0;
    private final int locationMax = 2000;


    /**基站负载矩阵*/
    private double[] loadRateArray = new double[M+MAX_K];
    /**基站用户连接率矩阵*/
    private double[] bsConnectRateArray = new double[M+MAX_K];
    /**基站功耗矩阵*/
    private double[] powerArray = new double[M+MAX_K];
    /**基站传输功耗矩阵*/
    private double[] powerOutArray = new double[M+MAX_K];
    /**基站实时状态矩阵*/
    private int[] bsStateArray = new int[M+MAX_K];
    /**基站总传输速率/带宽矩阵*/
    private double[] bsTransmissionSpeedArray = new double[M+MAX_K];
    /**基站坐标矩阵*/
    private double[][] bsLocationArray = new double[M+MAX_K][2];
    /**用户坐标矩阵*/
    private double[][] userLocationArray = new double[MAX_N][2];
    /**用户与基站连接矩阵*/
    private double[][] userConnectBsArray = new double[M+MAX_K][MAX_N];
    /**用户与基站之间的传播损耗矩阵*/
    private double[][] propagationLossArray = new double[M+MAX_K][MAX_N];
    /**用户与基站之间的SINR矩阵*/
    private double[][] sinrArray = new double[M+MAX_K][MAX_N];
    /**用户与基站之间的传输速率矩阵*/
    private double[][] transmissionSpeedArray = new double[M+MAX_K][MAX_N];
    /**用户连接权重矩阵*/
    private double[][] connectWeightArray = new double[MAX_N][M+MAX_K];

    /**用于存储负载率分别为100%，50%，30%，10%以及睡眠状态的动态功率以及传输速率
     * [100%,50%)  -> 100%
     * [50%,30%) -> 50%
     * [30%,10%) -> 30%
     * [10%,0%) -> 10%
     * 0% -> Sleep
     * */
    double[] power100 = new double[M+MAX_K];
    double[] power50 = new double[M+MAX_K];
    double[] power30 = new double[M+MAX_K];
    double[] power10 = new double[M+MAX_K];
    double[] powerSleep = new double[M+MAX_K];
    double[] transmissionSpeed100 = new double[M+MAX_K];
    double[] transmissionSpeed50 = new double[M+MAX_K];
    double[] transmissionSpeed30 = new double[M+MAX_K];
    double[] transmissionSpeed10 = new double[M+MAX_K];


    /**初始化的数据有：固定基站的坐标，bsLocationArray前M个坐标值不会变
     *  根据用户数判断需要多少移动基站
     *  初始化基站的时候进行用户选择
     * @param eeWeight EE权重
     * @param userLocationArray  用户坐标矩阵*/
    public Model(int eeWeight, double[][] userLocationArray) {
        K = countMobileBsNum(userLocationArray.length);
        N = userLocationArray.length;
        initModel(eeWeight,userLocationArray);
        initUserConnectBsArray();
    }

    /**初始化时随机用户连接基站
     * 同时 初始化矩阵数据 同时初始化基站传输功率
     *   DE算法的初始化放在这
     * */
    private void initUserConnectBsArray(){
        for (int i = 0; i < (M + K); i++) {
            /*有移动基站*/
            if (i>=M){
                bsLocationArray[i][0] = ThreadLocalRandom.current().nextDouble(locationMin,locationMax);
                bsLocationArray[i][1] = ThreadLocalRandom.current().nextDouble(locationMin,locationMax);
            }else {
                bsLocationArray[i] = ModelParameter.getBsLocation()[i];
            }
        }
        /*更新用户与基站之间的传播损耗矩阵*/
        countAllPropagationLoss();
        /*传递等于0 的值，代表当前系统所有基站处于睡眠状态*/
        countAllConnectWeight(0);
        /*用户根据权重矩阵进行基站选择 当前系统所有基站处于睡眠状态*/
        userDoChoose(0);

        for (int i = 0; i < (M + K); i++) {
            powerOutArray[i] = getMinPowerOut(i) >= pMax ? pMax : ThreadLocalRandom.current().nextDouble(getMinPowerOut(i),pMax);
            bsStateArray[i] = powerOutArray[i] > 0 ? 1 : 0;
        }
    }




    /**计算移动基站数量*/
    private int countMobileBsNum(int userNum){
        int baseNum = (int) (M*nMax*thresholdLoadRate);
        if (userNum <= baseNum){
            return 0;
        }else {
            return (int)Math.ceil( ((userNum - baseNum) / (nMax*thresholdT)));
        }
    }

    /**初始化权重以及用户坐标矩阵*/
    private void initModel(int eeWeightIndex, double[][] userLocation) {
        alpha  = ModelParameter.getEeWeights()[eeWeightIndex][0];
        beta = ModelParameter.getEeWeights()[eeWeightIndex][1];
        gamma  = ModelParameter.getEeWeights()[eeWeightIndex][2];
        delta  = ModelParameter.getEeWeights()[eeWeightIndex][3];
        zeta = ModelParameter.getEeWeights()[eeWeightIndex][4];

        /*更新用户坐标矩阵*/
        userLocationArray = userLocation;
    }


    /**计算基站负载率*/
    public double countLoadRate(double pOut){
        return pOut / pMax;
    }

    /**计算所有基站负载率*/
    public void countAllBsLoadRate(){
        for (int i = 0; i < M + K; i++) {
            loadRateArray[i] = countLoadRate(powerOutArray[i]);
        }
    }

    /**计算系统基站平均总负载*/
    public double countSystemAvaLoadRate(){
        double result = 0;
        for (int i = 0; i < M + K; i++) {
            result += countLoadRate(powerOutArray[i]);
        }
        return result/(M+K);
    }

    /**计算所有基站的连接用户负载率*/
    public void countAllBsConnectRate(){
        for (int i = 0; i < M + K; i++) {
            bsConnectRateArray[i] = getBsConnectNum(i) / nMax;
        }
    }


    /**更新100%~Sleep不同负载下的基站功耗矩阵和传输速率矩阵
     *  [100%,50%)  -> 100%
     *  [50%,30%) -> 50%
     *  [30%,10%) -> 30%
     *  [10%,0%) -> 10%
     *  0% -> Sleep
     * */
    private void updateBasedLoadRateArrays(){
        int index100=0 , index50=0,index30=0,index10=0,indexSleep=0;
        for (int i = 0; i < loadRateArray.length; i++) {
            if (loadRateArray[i] == 0){
                powerSleep[indexSleep++] = powerArray[i];
            }else if (loadRateArray[i]>0 && loadRateArray[i] <=0.1){
                power10[index10] = powerArray[i];
                transmissionSpeed10[index10++] = bsTransmissionSpeedArray[i];
            }else if (loadRateArray[i]>0.1 && loadRateArray[i] <=0.3){
                power30[index30] = powerArray[i];
                transmissionSpeed30[index30++] = bsTransmissionSpeedArray[i];
            }else if (loadRateArray[i]>0.3 && loadRateArray[i] <=0.5){
                power50[index50] = powerArray[i];
                transmissionSpeed50[index50++] = bsTransmissionSpeedArray[i];
            }else {
                power100[index100] = powerArray[i];
                transmissionSpeed100[index100++] = bsTransmissionSpeedArray[i];
            }
        }
    }


    /**计算基站与用户之间的距离*/
    public double countDistance(double[] bsLocation, double[] userLocation){
        return Math.sqrt(Math.pow(bsLocation[0]-userLocation[0],2) + Math.pow(bsLocation[1]-userLocation[1],2));
    }

    /**计算基站与用户之间的传播损耗*/
    public double countPropagationLoss(double distance){
        return epsilon*Math.log10(distance)+cD;
    }

    /**计算每个基站与所有用户之间的传播损耗矩阵*/
    public void countAllPropagationLoss(){
        for (int i = 0; i < M + K; i++) {
            for (int j = 0; j < N; j++) {
                propagationLossArray[i][j] = countPropagationLoss(countDistance(bsLocationArray[i],userLocationArray[j]));
            }
        }
    }

    /**计算基站与用户之间的SINR信号与干扰加噪声比*/
    public void countSINR(int bsIndex, int userIndex){
        double plMid = 0;
        for (int i = 0; i < (M + K); i++) {
            if (i==bsIndex){continue;}
            plMid +=propagationLossArray[i][userIndex] * powerOutArray[i];
        }
        double result =  propagationLossArray[bsIndex][userIndex]  * powerOutArray[bsIndex] / (plMid + noise);
        sinrArray[bsIndex][userIndex] = result;
    }

    /**计算每个基站与所有用户之间的SINR信号与干扰加噪声比*/
    public void countAllSINR(){
        for (int i = 0; i < M + K; i++) {
            for (int j = 0; j < N; j++) {
                countSINR(i,j);
            }
        }
    }

    /**计算log2(N)
     * Math.log的底为e
     * */
    public double log2(double N) {
        return Math.log(N)/Math.log(2);
    }

    /**计算基站与用户之间的传输速率*/
    public double countTransmissionSpeed(int bsIndex,int userIndex){
        return bandwidth * log2(1+sinrArray[bsIndex][userIndex]);
    }

    /**计算基于连接矩阵的基站与用户之间的传输速率*/
    public void countAllTransmissionSpeed(){
        for (int i = 0; i < M + K; i++) {
            for (int j = 0; j < N; j++) {
                if (userConnectBsArray[i][j]==1){
                    transmissionSpeedArray[i][j] = countTransmissionSpeed(i,j);
                }
            }
        }
    }

    /**计算基站的总传输速率*/
    public double countBsTransmissionSpeed(int bsIndex){
        double speedSum = 0;
        for (int i = 0; i < N; i++) {
            speedSum += userConnectBsArray[bsIndex][i] * transmissionSpeedArray[bsIndex][i];
        }
        bsTransmissionSpeedArray[bsIndex] = speedSum;
        return speedSum;
    }
    /**计算所有基站的总传输速率*/
    public void countAllBsTransmissionSpeed(){
        for (int i = 0; i < M + K; i++) {
            bsTransmissionSpeedArray[i] = countBsTransmissionSpeed(i);
        }
    }
    /**计算基站动态功耗*/
    public double countPower(double pOut,double loadRate){
        if (loadRate==0) {return pSleep;}
        return p0 + deltaP * pOut;
    }

    /**计算基站的动态功耗*/
    public void countBsPower(int bsIndex){
        powerArray[bsIndex] =bsStateArray[bsIndex] * (p0 + deltaP * powerOutArray[bsIndex]) + (1 - bsStateArray[bsIndex] ) * pSleep;
    }

    /**计算所有基站的动态功耗*/
    public void countAllBsPower(){
        for (int i = 0; i < M + K; i++) {
            countBsPower(i);
        }
    }

    /**计算用户选择与基站连接时的权重*/
    public void countConnectWeight(int bsIndex, int userIndex,int sysLoadRate){
        if (sysLoadRate == 0){
            connectWeightArray[userIndex][bsIndex] = cD / propagationLossArray[bsIndex][userIndex];
        }else {
            connectWeightArray[userIndex][bsIndex] =(sinrArray[bsIndex][userIndex] ) / (loadRateArray[bsIndex] +cD);
        }

    }

    /**计算所有用户选择与各个基站连接时的权重*/
    public void countAllConnectWeight(int sysLoadRate){
        for (int i = 0; i <N; i++) {
            for (int j = 0; j < M+K; j++) {
                countConnectWeight(j,i,sysLoadRate);
            }
        }
    }

    /**获取基站的当前连接数量*/
    public int getBsConnectNum(int bsIndex){
        int sum = 0;
        for (int i = 0; i < N; i++) {
            sum += userConnectBsArray[bsIndex][i];
        }
        return sum;
    }

    /**计算评价指标EE*/
    public double countEE(List<Double[]> solutionLocation , List<Double> solutionPowerOut){
        initValuesFromCountEE(solutionLocation,solutionPowerOut);
        readySpeedAndPowerData();
        return 100 * countSystemPowerBasedWeight() / countSystemTransmissionSpeedBasedWeight();
    }

    /**计算EE的分子：系统基于权重的动态功耗*/
    private double countSystemPowerBasedWeight(){
        double result = 0.0;
        for (double v : powerSleep) {
            result += zeta * v;
        }
        for (double v : power10) {
            result += delta * v;
        }
        for (double v : power30) {
            result += gamma * v;
        }
        for (double v : power50) {
            result += beta * v;
        }
        for (double v : power100) {
            result += alpha * v;
        }
        return result;
    }
    /**计算EE的分母：系统基于权重的传输速率*/
    private double countSystemTransmissionSpeedBasedWeight(){
        double result = 0.0;
        for (double v : transmissionSpeed30) {
            result += delta * v;
        }
        for (double v : transmissionSpeed10) {
            result += gamma * v;
        }
        for (double v : transmissionSpeed50) {
            result += beta * v;
        }
        for (double v : transmissionSpeed100) {
            result += alpha * v;
        }
        return result;
    }
    /**准备计算EE的基于不同负载率的传输速率和动态功耗矩阵*/
    public void readySpeedAndPowerData(){
        clearArrays();
        countAllBsLoadRate();
        countAllPropagationLoss();
        countAllSINR();
        countAllTransmissionSpeed();
        countAllBsTransmissionSpeed();
        countAllBsPower();
        updateBasedLoadRateArrays();
    }

    /**清空模型,避免数据复用*/
    private void clearArrays() {
        for (int i = 0; i < M + MAX_K; i++) {
            loadRateArray[i] = 0;
            powerArray[i] = 0;
            power100[i] = 0;
            power50[i] = 0;
            power30[i] = 0;
            power10[i] = 0;
            powerSleep[i] = 0;
            transmissionSpeed100[i] = 0;
            transmissionSpeed50[i] = 0;
            transmissionSpeed30[i] = 0;
            transmissionSpeed10[i] = 0;
            for (int j = 0; j < MAX_N; j++) {
                propagationLossArray[i][j] = 0;
                propagationLossArray[i][j] = 0;
                propagationLossArray[i][j] = 0;
                transmissionSpeedArray[i][j] = 0;
            }
        }
    }

    /**将DE那边传递过来的可行解赋值到Model中*/
    private void initValuesFromCountEE(List<Double[]> solutionLocation, List<Double> solutionPowerOut) {
        for (int i = 0; i < solutionLocation.size(); i++) {
            /*只需更新移动基站的坐标*/
            if (i>=M){
                bsLocationArray[i][0] = solutionLocation.get(i)[0];
                bsLocationArray[i][1] = solutionLocation.get(i)[1];
            }
            powerOutArray[i] = solutionPowerOut.get(i);
            bsStateArray[i] = solutionPowerOut.get(i) > 0 ? 1 : 0;
        }
    }

    /**进行用户与基站的匹配选择，用户向基站发送连接请求
     * 每当时间切片一到，用户重新扫描基站，与基站重新建立连接
     */
    public void doUserSelectBs(List<Double[]> bestLocation, List<Double> bestPowerOut){
        /*将最优解更新到Model中*/
        for (int i = 0; i < M + K; i++) {
            bsLocationArray[i][0] = bestLocation.get(i)[0];
            bsLocationArray[i][1] = bestLocation.get(i)[1];
            powerOutArray[i] = bestPowerOut.get(i);
        }

        countAllBsLoadRate();
        countAllPropagationLoss();
        countAllSINR();
        /*传递不等于0 的值，代表当前系统有基站活跃着*/
        countAllConnectWeight(1);
        updateUserConnectBsArray();
        /*计算基站用户连接率*/
        countAllBsConnectRate();
}

    /**更新用户与基站的连接矩阵*/
    private void updateUserConnectBsArray() {
        /*用户重新选择基站时需要断开之前的连接，也就是清空连接列表*/
        userConnectBsArray = new double[M+MAX_K][N];
        /*基站不全睡眠*/
        userDoChoose(1);
    }

    /**用户根据权重矩阵做出选择
     * @param state state=0 对应基站全部睡眠情况 state=1 则有活跃着
     * */
    private void userDoChoose(int state) {
        for (int i = 0; i < N; i++) {
            /*获得用户选择权重最大的基站索引*/
            int maxNo = 1;
            int bsIndex = Finder.maxNoIndex(connectWeightArray[i], maxNo,M+K);
            if (state==0){
                while ((checkBsConnectNum(bsIndex) != -1 )&&(maxNo<nMax)) {
                    maxNo++;
                    bsIndex = Finder.maxNoIndex(connectWeightArray[i], maxNo,M+K);
                }
            }else {
                while ((checkBsConnectNum(bsIndex) != -1 || sinrArray[bsIndex][i] <= thresholdSINR)&&(maxNo<nMax)) {
                    maxNo++;
                    bsIndex = Finder.maxNoIndex(connectWeightArray[i], maxNo,M+K);
                }
            }
            userConnectBsArray[bsIndex][i] = 1;
        }
    }

    /*约束检查
     * 统一规定：返回-1 为true ，返回其他为违规索引
     */

    /**检查基站连接数量是否越界*/
    public int checkBsConnectNum(int bsIndex){
        if (getBsConnectNum(bsIndex) >= nMax){
            return bsIndex;
        }
        return  -1;
    }

    /**检查用户连接数量是否违规*/
    public int checkUserConnectNum(int userIndex){
        int sum = 0;
        for (int i = 0; i < M + K; i++) {
            sum += userConnectBsArray[i][userIndex];
        }
        if (sum > 1){
            return userIndex;
        }
        return -1;
    }


    /**检查传输功耗是否越界
     * 传输功率应该有一个下界，定义为基站连接率 * PMax
     * */
    public boolean checkPowerOutIsLegal(double pOut,int bsIndex){
        return (pOut >= getMinPowerOut(bsIndex) && pOut <= pMax);
    }

    /**获得基站与与该基站连接所有的用户之间的最小传输功率*/
    public double getMinPowerOut(int bsIndex){
        return (getBsConnectNum(bsIndex) / nMax) * pMax;
    }


    /**检查移动基站坐标是否越界*/
    public boolean checkXLocationIsLegal(double x){
        return (x >= locationMin && x <= locationMax) ;
    }

    public boolean checkYLocationIsLegal(double y){
        return (y >= locationMin && y <= locationMax);
    }

    public int getM() {
        return M;
    }

    public int getK() {
        return K;
    }

    public int getN() {
        return N;
    }

    public double[] getPowerOutArray() {
        return powerOutArray;
    }

    public double[][] getBsLocationArray() {
        return bsLocationArray;
    }

}
