package com.zt.Tools;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public class RandomUtil {
    /**获取指定范围 [min.max) 内的n个随机数*/
    public static int[] randomSet(int min, int max, int n) {
        Set<Integer> set = new HashSet<Integer>();
        int[] array = new int[n];
        for (; true;) {
            int num = ThreadLocalRandom.current().nextInt(min, max);
            set.add(num);
            if (set.size() >= n) {
                break;
            }
        }
        int i = 0;
        for (int a : set) {
            array[i] = a;
            i++;
        }
        return array;
    }

    /**获得不重复的三个指定范围内的随机数*/
    public static int[] getNotRepeatIndexOf3(int noIndex,int min , int max){
        int[] rand = new int[3];
        int randIndex=0;
        int[] randArray = RandomUtil.randomSet(min , max, 4);
        if (randArray[randIndex] == noIndex){
            randIndex++;
        }
        rand[0] = randArray[randIndex++];
        if (randArray[randIndex] == noIndex){
            randIndex++;
        }
        rand[1] = randArray[randIndex++];
        if (randArray[randIndex] == noIndex){
            randIndex++;
        }
        rand[2] = randArray[randIndex];
        return rand;
    }

    /**返回[0,1]之间的正态分布随机数
     * @param u 均值
     *@param v 方差*/
    public static double getNormalDistribution(double u, double v){
        double result = Math.sqrt(v)*new Random().nextGaussian()+u;
        if (result >=1){
            result = 1;
        }
        if (result <= 0){
            result = 0;
        }
        return result;
    }

    /**返回(0,1]之间的柯西分布随机数
     * @param u 均值
     * @param v 方差 */
    public static double getCauchyDistribution(double u , double v ){
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        double result = u +v * Math.tan(Math.PI*(random.nextDouble())-0.5);
        while (result<=0){
            result = u +v * Math.tan(Math.PI*(random.nextDouble())-0.5);
        }
        if (result>1){
            result = 1;
        }
        return result;
    }

    /**返回指定闭区间内随机数 [min,max)*/
    public static int getRandIndex(int min ,int max){
        return (int)(Math.random() * (max - min));
    }
    public static double getRandIndex(double min ,double max){
//        return (Math.random() * (max - min));
        return min + (new Random().nextDouble() * (max - min));
    }

    /**获得指定数组的随机一个数*/
    public static int getRandIndexFromArray(int[] arr){
        return arr[getRandIndex(0,arr.length)];
    }

    /**获得两个数组中的随机值*/
    public static List<Double> getRandPowerValueFromTwoArray(List<List<Double>> arr1 , int len1, List<List<Double>> arr2 , int len2,int randIndex,int D){
        List<Double> result =new ArrayList<>();
        if (randIndex<len1){
            for (int i = 0; i < D; i++) {
                result.add(arr1.get(randIndex).get(i));
            }
        }else {
            for (int i = 0; i < D; i++) {
                result.add(arr2.get(randIndex-len1).get(i));
            }
        }
        return result;
    }
    public static List<Double[]> getRandLocationValueFromTwoArray(List<List<Double[]>> arr1 , int len1, List<List<Double[]>> arr2 , int len2,int randIndex,int D){
        List<Double[]> result =new ArrayList<>();
        if (randIndex<len1){
            for (int i = 0; i < D; i++) {
                result.add(new Double[]{arr1.get(randIndex).get(i)[0],arr1.get(randIndex).get(i)[1]});
            }
        }else {
            for (int i = 0; i < D; i++) {
                result.add(new Double[]{arr2.get(randIndex-len1).get(i)[0],arr2.get(randIndex-len1).get(i)[1]});
            }
        }
        return result;
    }
}
