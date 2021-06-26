package com.zt;


import com.zt.Algorithm.*;
import com.zt.MBS.Model;
import com.zt.MBS.ModelParameter;
import com.zt.Tools.DateUtil;
import com.zt.Tools.ExcelOperate;
import sun.security.provider.SHA;

import java.io.IOException;


/**
 * @author zhangtian
 */
public class Main {
	/** 将客流量按小时区分，每小时内按时间切片进行用户重新选择基站
	 * DAY_TIMES：一天24小时
	 * HOUR_TIMES：一小时60分 用户重新选择一次基站（此时此刻判断MBS是否需要加入）
	 *
	 * 代码逻辑：
	 * 	将客流量按小时区,动态变化分析
	 * 	系统具有传递性：当前基站的传输功耗能够传递给下一个切片的模型中
	 * 	使用DE算法优化当前时间内的模型
	 * 	将DE算法优化后的模型参数传递给下一个时间切片的模型，同时保存当前切片的最优解与最优值等
	 * */

	private static final int DE=0;
	private static final int SHADE=1;
	private static final int JSO = 2;
	private static final int IDE = 3;
	private static final int TEST = 10;
	private static final int LONG_TEST = 100;


	public static void main(String[] args){
		begin(TEST);
//		test(TEST);
//		doSomeTimeCut();
	}


	private static void test(int strategy) {
		int eeWeight = 2;
		int dayTime = 8;
		double[][] userLocationArray = ExcelOperate.getUserLocation(dayTime);
		Model model = new Model(eeWeight,userLocationArray);
		BaseAlgorithm solution ;
		switch (strategy){
			case DE:
				solution = new DifferentialEvolution(model,100);
				break;
			case SHADE:
				solution = new SHADE(model,100);
				break;
			case JSO:
				solution = new JSO(model,100);
				break;
			case IDE:
				solution = new IDE(model,100);
				break;
			case TEST:
				solution = new ModifiedIde(model,100);
				break;
			default:solution = new DifferentialEvolution(model,100);break;
		}

		ReturnValue result = solution.justDoIt();
		System.out.println("最佳目标值： "+result.getBestValue() + "迭代次数为："+result.getgLongNum());
		try {
			solution.outputFitnessValues(0,dayTime);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done");
		model.doUserSelectBs(result.getBestLocation(),result.getBestPowerOut());
		result = solution.justDoIt();
		try {
			solution.outputFitnessValues(1,dayTime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**正式开始执行模型*/
	public static void begin(int strategy){
		int eeWeight = 2;
		/*
		 * bestEE : 存储每个时间片内的DE算法的最佳评价指标值
		 * bestGenerationNum：存放每个时间片内的DE算法返回的最大迭代次数
		 * bestSolutionPouts：存储每个时间片内最优解中的功率解
		 * bestSolutionLocations：存储每个时间片内最优解中的坐标解
		 * */
		double[] bestEE = new double[ModelParameter.DAY_TIMES];
		double[] bestGenerationNum = new double[ModelParameter.DAY_TIMES];

		long sysStartTime = System.currentTimeMillis();

		for (int dayTime = 0; dayTime < ModelParameter.DAY_TIMES; dayTime++) {
			/*计算运行时间*/
			long startTime = System.currentTimeMillis();
			double[][] userLocationArray = ExcelOperate.getUserLocation(dayTime);
			/*如果没有用户，不走DE算法进行系统调节，所有基站陷入睡眠模式，清空MBS*/
			if (userLocationArray == null || userLocationArray.length==0){
				bestEE[dayTime] = 0;
				bestGenerationNum[dayTime] = 0;
			}else {
				/*步骤：1）先初始化Model与DifferentialEvolution 2）DE算法执行一次：3000代迭代 3）用户在选择一次基站 4）在做一次DE：3000次迭代*/
				Model model = new Model(eeWeight,userLocationArray);
				BaseAlgorithm solution;
				switch (strategy){
					case DE:
						solution = new DifferentialEvolution(model,1500);
						break;
					case SHADE:
						solution = new SHADE(model,1500);
						break;
					case JSO:
						solution = new JSO(model,1500);
						break;
					case IDE:
						solution = new IDE(model,1500);
						break;
					case TEST:
						solution = new ModifiedIde(model,1500);
						break;
					case LONG_TEST:
						solution = new ModifiedIde(model,30000);
						break;
					default:
						solution = new DifferentialEvolution(model,1500);
						break;
				}

				ReturnValue result = solution.justDoIt();

				try {
					ExcelOperate.outPutExcelByTime(dayTime,0, result.getBestPowerOut(),result.getBestLocation());
					solution.outputFitnessValues(0,dayTime);
				} catch (IOException e) {
					e.printStackTrace();
				}
				model.doUserSelectBs(result.getBestLocation(),result.getBestPowerOut());
				result = solution.justDoIt();
//				/*存储最优解到Excel*/
				try {
					ExcelOperate.outPutExcelByTime(dayTime,1, result.getBestPowerOut(),result.getBestLocation());
					solution.outputFitnessValues(1,dayTime);
				} catch (IOException e) {
					e.printStackTrace();
				}

				/*存储最优解以及最优值*/
				bestEE[dayTime] = result.getBestValue();
				bestGenerationNum[dayTime] = result.getgLongNum();
			}

			long endTime = System.currentTimeMillis();
			System.out.println("time: "+dayTime + " __运行时间为:"+(endTime-startTime)/1000/60+"分钟 "+(endTime-startTime)/1000%60+" 秒"+"  __bestEE:"+bestEE[dayTime]
					+ " bestGenerationNum: "+ bestGenerationNum[dayTime]*2 );
			System.out.println();
		}
		long sysEndTime = System.currentTimeMillis();
		/*存储所有时间片的最优解到Excel中*/
		try {
			ExcelOperate.outPutExcelValue(bestEE,bestGenerationNum);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("____总共运行时间为："+DateUtil.formatTime(sysEndTime-sysStartTime));

	}

	/**专门优化几个时间片*/
	public static void doSomeTimeCut(){
		double[] bestValues = new double[]{32.40506371,34.05293009,33.69738205,34.57504137};
		int[] times = new int[]{12,13,14,16};
		double[][] recordingBest = new double[10][4];
		double[][][] userLocationArrays = new double[][][]{ExcelOperate.getUserLocation(11),
				ExcelOperate.getUserLocation(12),ExcelOperate.getUserLocation(13),ExcelOperate.getUserLocation(15)};

		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < 4; j++) {
				long startTime = System.currentTimeMillis();
				Model model = new Model(2,userLocationArrays[j]);
				ModifiedIde solution = new ModifiedIde(model,1500);
				ReturnValue result = solution.justDoIt();
				model.doUserSelectBs(result.getBestLocation(),result.getBestPowerOut());
				result = solution.justDoIt();
				/*存储最优解以及最优值*/
				recordingBest[i][j] = result.getBestValue();
				long endTime = System.currentTimeMillis();
				System.out.println("第"+i+"次重复试验 时间片为 "+times[j]+"点  其最优值为："+result.getBestValue()+"  消耗时间为："+ DateUtil.formatTime(startTime-endTime));
			}
		}
		/*统计平均值*/
		for (int j = 0; j < 4; j++) {
			double avaValue = 0;
			for (int i = 0; i < 10; i++) {
				avaValue+=recordingBest[i][j];
			}
			System.out.println("时间片为 "+times[j]+" 对应的平均值为"+ avaValue/10 +" 与IDE平均最优值相差 "+(avaValue/10-bestValues[j]));
		}
	}

}
