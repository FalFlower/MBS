package com.zt.Algorithm;

/**
 * @author zhangtian
 */
public abstract interface BaseFunction {
    /**算法入口 */
    public ReturnValue justDoIt();

    /**选择操作*/
    public void select();

    /**更新bestValue and bestSolution等*/
    public void choseBest();

    /**交叉操作*/
    public void crossover();

    /**变异操作*/
    public void mutation();
}

