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
public class DifferentialEvolution extends BaseAlgorithm{

    public DifferentialEvolution(Model model,int efsNum) {
        super(model,efsNum);
    }

    /**选择操作*/
    @Override
    public void select() {
        baseSelect();
    }


    /**交叉操作*/
    @Override
    public void crossover() {
        for (int i = 0; i < np; i++) {
            int jRand = ThreadLocalRandom.current().nextInt(0, d);
            for (int j = 0; j < d; j++) {
                double k = ThreadLocalRandom.current().nextDouble(0,1);
                if ((k <= cr) || (j == jRand)){
                    childPowerOut.get(i).set(j,mutantPowerOut.get(i).get(j));
                    childLocation.get(i).set(j,new Double[]{mutantLocation.get(i).get(j)[0],mutantLocation.get(i).get(j)[1]});
                }else {
                    childPowerOut.get(i).set(j,populationPowerOut.get(i).get(j));
                    childLocation.get(i).set(j,new Double[]{populationLocation.get(i).get(j)[0],populationLocation.get(i).get(j)[1]});
                }
            }
        }
    }



    /**变异操作*/
    @Override
    public void mutation(){
        for (int i = 0; i < np; i++) {
            int[] randIndex = RandomUtil.getNotRepeatIndexOf3(i,0,d);
            for (int j = 0; j < d; j++) {
                baseMutation(i,j,randIndex);
            }
        }
    }


}
