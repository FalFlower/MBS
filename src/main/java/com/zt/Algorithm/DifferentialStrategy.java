package com.zt.Algorithm;

/**
 * @author zhangtian
 *
 */
public abstract class DifferentialStrategy {
    /*不同的差分策略*/

    public static final int DE_RAND_ONE = 1;
    public static final int DE_CURRENT_TO_BEST_ONE = 2;
    public static final int DE_RAND_TWO = 3;
    public static final int DE_CURRENT_TO_BEST_TWO = 4;
    public static final int DE_BEST_ONE = 5;
    public static final int DE_CURRENT_TO_BEST_W_ONE = 6;
    public static final int DE_TRIANGLE_RAND_ONE = 7;

    /**DE/rand/1 （DE算子）
     * @param f 变异因子
     * @param rand1 基因值
     * @param rand2 基因值
     * @param rand3 基因值*/
    protected double deRandOne(double f,double rand1,double rand2,double rand3){
        return rand1 + f * (rand2 - rand3);
    }

    /**DE/rand/2
     * @param f 变异因子
     * @param randValue1 种群中适应度最佳个体中随机的一个个体的基因
     * @param randValue2 种群中随机个体的基因
     * @param randValue3 种群中随机个体的基因
     * @param randValue4 种群中随机个体的基因
     * @param randValue5 种群中随机个体的基因
     * randValue1 ≠ randValue2 ≠ randValue3 ≠ randValue4 ≠ randValue5*/
    protected double deRandTwo(double f,double randValue1,double randValue2,double randValue3,double randValue4,double randValue5){
        return randValue1+f*(randValue2-randValue3)+f*(randValue4-randValue5);
    }

    /**DE/current-to-pBest/1 （SHADE算子）
     * @param f 变异因子
     * @param bestValue 种群中适应度最佳个体中随机的一个个体的基因
     * @param currentValue 当前个体的基因
     * @param randValue 种群中随机个体的基因
     * @param defeatedAndPopulationValue 变异交叉失败个体与当前种群中随机的一个个体的基因 */
    protected double deCurrentToBestOne(double f, double bestValue, double currentValue, double randValue, double defeatedAndPopulationValue){
        return currentValue+f*(bestValue-currentValue)+f*(randValue-defeatedAndPopulationValue);
    }

    /**DE/current-to-pBest-w/1 （JSO算子）
     * @param fW 加权变异因子
     * @param f 变异因子
     * @param bestValue 种群中适应度最佳个体中随机的一个个体的基因
     * @param currentValue 当前个体的基因
     * @param randValue 种群中随机个体的基因
     * @param defeatedAndPopulationValue 变异交叉失败个体与当前种群中随机的一个个体的基因 */
    protected double deCurrentToBestWeightOne(double f,double fW, double bestValue, double currentValue, double randValue, double defeatedAndPopulationValue){
        return currentValue+fW*(bestValue-currentValue)+f*(randValue-defeatedAndPopulationValue);
    }

    /**DE/current-to-pBest/2
     * @param f 变异因子
     * @param bestValue 种群中适应度最佳个体中随机的一个个体的基因
     * @param randValue1 种群中随机个体的基因
     * @param randValue2 种群中随机个体的基因
     * @param randValue3 种群中随机个体的基因
     * @param randValue4 种群中随机个体的基因
     * randValue1 ≠ randValue2 ≠ randValue3 ≠ randValue4*/
    protected double deCurrentToBestTwo(double f,double bestValue,double randValue1,double randValue2,double randValue3,double randValue4){
        return bestValue+f*(randValue1-randValue2)+f*(randValue3-randValue4);
    }

    /**DE/best/1
     * @param f 变异因子
     * @param bestValue 种群中适应度最佳个体中随机的一个个体的基因
     * @param rand1 种群中随机个体的基因
     * @param rand2 种群中随机个体的基因
     * rand1 ≠ rand2*/
    protected double deBestOne(double f,double bestValue,double rand1,double rand2){
        return bestValue+f*(rand1-rand2);
    }

    /**DE/triangle-rand/1 （IDE算子）
     * @param f 变异因子
     * @param cValue 三角凸组合变量
     * @param bestValue 三角最优变量
     * @param worstValue 三角最优变量*/
    public double deTriangleRandOne(double f,double cValue,double bestValue,double worstValue){
        return cValue + 2*f * (bestValue - worstValue);
    }


}
