package com.zt.Tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author zhangtian
 */
public class Finder {
    /**获取最大值*/
    public static double max(double[] arr){
        double max=arr[0];
        for (double v : arr) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }
    /**获取最大值*/
    public static double max(List<Double> arr){
        double max=arr.get(0);
        for (double v : arr) {
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /**获取最大值索引*/
    public static int maxIndex(double[] arr){
        int maxIndex=0;;
        for(int i=0;i<arr.length;i++){
            if(arr[i]>arr[maxIndex]){
                maxIndex=i;
            }
        }
        return maxIndex;
    }

    /**获取第maxNo大值得索引*/
    public static int maxNoIndex(double[] arr,int maxNo,int length){
        double[] arrMid = new double[length];
        System.arraycopy(arr, 0, arrMid, 0, length);
        quickSort(arrMid, 0, length-1);
        return searchIndex(arr,arrMid[length-maxNo]);
    }

    /**获取数组中顺序排列（大->小）前n个值*/
    public static int[] getMaxNoIndex(double[] arr,int n){
        double[] arrMid = new double[arr.length];
        int[] result = new int[n];
        System.arraycopy(arr, 0, arrMid, 0, arr.length);
        quickSort(arrMid, 0, arr.length-1);
        for (int i = 0; i < n; i++) {
            result[i]=searchIndex(arr,arrMid[arrMid.length-1-i]);
        }
        return result;
    }


    /**获取数组倒数前几的索引值*/
    public static int[] getMinNoIndex(double[] arr ,int n){
        double[] arrMid = new double[arr.length];
        int[] result = new int[n];
        System.arraycopy(arr, 0, arrMid, 0, arr.length);
        quickSort(arrMid, 0, arr.length-1);
        for (int i = 0; i < n; i++) {
            result[i]=searchIndex(arr,arrMid[i]);
        }
        return result;
    }


    /**快速排序*/
    public static void quickSort(double[] arr,int low,int high){
        int i,j;
        double temp,t;
        if(low>high){
            return;
        }
        i=low;
        j=high;
        //temp就是基准位
        temp = arr[low];

        while (i<j) {
            //先看右边，依次往左递减
            while (temp<=arr[j]&&i<j) {
                j--;
            }
            //再看左边，依次往右递增
            while (temp>=arr[i]&&i<j) {
                i++;
            }
            //如果满足条件则交换
            if (i<j) {
                t = arr[j];
                arr[j] = arr[i];
                arr[i] = t;
            }

        }
        //最后将基准为与i和j相等位置的数字交换
        arr[low] = arr[i];
        arr[i] = temp;
        //递归调用左半数组
        quickSort(arr, low, j-1);
        //递归调用右半数组
        quickSort(arr, j+1, high);
    }

    public static void quickSort(int[] arr,int low,int high){
        int i,j;
        int temp,t;
        if(low>high){
            return;
        }
        i=low;
        j=high;
        //temp就是基准位
        temp = arr[low];

        while (i<j) {
            //先看右边，依次往左递减
            while (temp<=arr[j]&&i<j) {
                j--;
            }
            //再看左边，依次往右递增
            while (temp>=arr[i]&&i<j) {
                i++;
            }
            //如果满足条件则交换
            if (i<j) {
                t = arr[j];
                arr[j] = arr[i];
                arr[i] = t;
            }

        }
        //最后将基准为与i和j相等位置的数字交换
        arr[low] = arr[i];
        arr[i] = temp;
        //递归调用左半数组
        quickSort(arr, low, j-1);
        //递归调用右半数组
        quickSort(arr, j+1, high);
    }

    /**获取最小值
     * */
    public static double min(double[] arr){
        double min=arr[0];
        for (double j : arr) {
            if (j < min) {
                min = j;
            }
        }
        return min;
    }

    /**获取最小值索引*/
    public static int minIndex(double[] arr){
        int minIndex=0;;
        for(int i=0;i<arr.length;i++){
            if(arr[i]<arr[minIndex]){
                minIndex=i;
            }
        }
        return minIndex;
    }

    /**在数组中查找指定元素是否存在 ,如是存在返回true,不存在返回false*/
    public static boolean search(double[] arr,double number){
        for (double j : arr) {
            if (new Double(number).equals(j)) {
                return true;
            }
        }
        return false;
    }

    /**在数组中查找指定元素是否存在 ,如是存在返回索引,不存在返回-1*/
    public static int searchIndex(double[] arr,double number){
        for(int i=0;i<arr.length;i++){
            if(number==arr[i]){
                return i;
            }
        }
        return -1;
    }
    public static int searchIndex(int[] arr,double number){
        for(int i=0;i<arr.length;i++){
            if(number==arr[i]){
                return i;
            }
        }
        return -1;
    }


}
