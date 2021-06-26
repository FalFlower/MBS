package com.zt.Tools;


import com.zt.MBS.ModelParameter;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
//xss

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author other
 */
public class ExcelOperate {
    /**获取指定数量用户坐标 （从3500个数据中截取）*/
    public static double[][] getUserLocation(int time){
        int userNum = ModelParameter.getUserNums()[time];
        if (userNum==0){
            return null;
        }
        double[][] result = new double[userNum][2];
        List<Double[]> excelData = importExcel();
        if (userNum <= ModelParameter.getUserNumMax()){
            for (int i = 0; i < userNum; i++) {
                result[i][0] = excelData.get(i)[0];
                result[i][1] = excelData.get(i)[1];
            }
        }else {
            for (int i = 0; i < userNum; i++) {
                result[i][0] = excelData.get(excelData.size()-i-1)[0];
                result[i][1] = excelData.get(excelData.size()-i-1)[1];
            }
        }
        return result;
    }


    /**读取Excel的方法*/
    public static List<Double[]> importExcel(){
        ArrayList<Double[]> list = new ArrayList<>();
        try {
            //1、获取文件输入流
            InputStream inputStream = new FileInputStream("userLocationData18.xls");
            //2、获取Excel工作簿对象
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            //3、得到Excel工作表对象
            HSSFSheet sheetAt = workbook.getSheetAt(0);
            //4、循环读取表格数据
            for (Row row : sheetAt) {
                //首行（即表头）不读取
                if (row.getRowNum() == 0) {
                    continue;
                }
                //读取当前行中单元格数据，索引从0开始
                double x = (row.getCell(0).getNumericCellValue());
                double y = row.getCell(1).getNumericCellValue();
                list.add(new Double[]{x,y});
            }
            //5、关闭流
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }



    /**输出算法的最后一代解所有个体 NP*D*/
    public static void outputAlgorithmLastNpSolution(List<List<Double[]>> populationLocation, List<List<Double>> populationPowerOut, int NP, int D, int index) throws IOException{
        /*创建Workbook类*/
        Workbook workbookLocation = new HSSFWorkbook();
        Workbook workbookPout = new HSSFWorkbook();
        Sheet sheetLocation = workbookLocation.createSheet("ModelData_last_generation_location_"+index);
        Sheet sheetPout = workbookPout.createSheet("ModelData_last_generation_power_out_"+index);
        /*创建表头*/
        Row headerLocation = sheetLocation.createRow(0);
        headerLocation.createCell(0).setCellValue("location_x");
        headerLocation.createCell(1).setCellValue("location_y");

        Row headerPout = sheetPout.createRow(0);
        headerPout.createCell(0).setCellValue("powerOut");

        for (int i = 0; i < NP; i++) {
            Row rowLocation = sheetLocation.createRow(i+1);
            Row rowPout = sheetPout.createRow(i+1);
            /*存储坐标*/
            for (int j = 0; j < D*2; j+=2) {
                rowLocation.createCell(j).setCellValue(populationLocation.get(i).get(j/2)[0]);
                rowLocation.createCell(j+1).setCellValue(populationLocation.get(i).get(j/2)[1]);
            }
            /*存储功率*/
            for (int j = 0; j < D; j++) {
                rowPout.createCell(j).setCellValue(populationPowerOut.get(i).get(j));
            }
        }
        File fileLocation = new File("ModelData_last_generation_location_"+index+".xls");
        File filePout = new File("ModelData_last_generation_power_out_"+index+".xls");

        if (!fileLocation.exists()) {
            fileLocation.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(fileLocation)){
            workbookLocation.write(out);
            workbookLocation.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }

        if (!filePout.exists()) {
            filePout.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(filePout)){
            workbookPout.write(out);
            workbookPout.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }


    }

    /**输出算法的最后一代解所有代 NP*D解的目标函数值*/
    public static void outputAllFitnessValue(double[][] fitness, int index, int dayTime)throws IOException{
        /*创建Workbook类*/
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ModelData_all_fitness_"+dayTime+"_"+index);
        /*写入数据*/
        for (int i = 0; i < fitness.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < fitness[0].length; j++) {
                row.createCell(j).setCellValue(fitness[i][j]);
            }
        }

        File file = new File("ModelData_all_fitness_"+dayTime+"_"+index+".xls");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(file)){
            workbook.write(out);
            workbook.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }
    }

    /**输出算法的最后一代解所有代的最佳目标函数值*/
    public static void outputAllBestFitnessValue(double[] allBestFitness, int index, int dayTime)throws IOException{
        /*创建Workbook类*/
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ModelData_all_best_fitness_"+dayTime+"_"+index);
        /*写入数据*/
        for (int i = 0; i < allBestFitness.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(allBestFitness[i]);
        }

        File file = new File("ModelData_all_best_fitness_"+dayTime+"_"+index+".xls");

        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(file)){
            workbook.write(out);
            workbook.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }
    }



    /**存储每个时间片数据的Excel数据
     *  最优解的坐标、传输功率数据以及基站状态数据
     * */
    public static void outPutExcelByTime(int dayTime,int index,List<Double> bestSolutionPouts, List<Double[]> bestSolutionLocations) throws IOException {
        /*创建Workbook类*/
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ModelData_"+dayTime+"_"+index);
        /*创建表头*/
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Location_x");
        header.createCell(1).setCellValue("Location_y");
        header.createCell(2).setCellValue("P_out");
        header.createCell(3).setCellValue("Bs_state");
        /*写入数据*/
        for (int i = 0; i < bestSolutionPouts.size(); i++) {
            Row row = sheet.createRow(i+1);
            row.createCell(0).setCellValue(bestSolutionLocations.get(i)[0]);
            row.createCell(1).setCellValue(bestSolutionLocations.get(i)[1]);
            row.createCell(2).setCellValue(bestSolutionPouts.get(i));
            row.createCell(3).setCellValue(bestSolutionPouts.get(i)>0?1:0);
        }

        File file = new File("ModelData_"+dayTime+"_"+index+".xls");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(file)){
            workbook.write(out);
            workbook.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }

    }

    /**存储总评价指标最优解*/
    public static void outPutExcelValue(double[] bestValue,double[] bestGenerationNum) throws IOException {
        //创建Workbook类
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("ModelDataBestValue");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("EE最优目标函数值");
        header.createCell(1).setCellValue("DE迭代次数");

        int index = 1;
        for (int i = 0; i < bestValue.length; i++) {
            Row row = sheet.createRow(index++);
            row.createCell(0).setCellValue(bestValue[i]);
            row.createCell(1).setCellValue(bestGenerationNum[i]);
        }

        File file = new File("ModelDataBestValue.xls");
        if (!file.exists()) {
            file.createNewFile();
        }
        try (OutputStream out = new FileOutputStream(file)){
            workbook.write(out);
            workbook.close();
        }catch (FileNotFoundException e){
            throw  new RuntimeException(e.getMessage());
        }

    }


}
