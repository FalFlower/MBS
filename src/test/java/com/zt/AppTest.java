package com.zt;

import static org.junit.Assert.assertTrue;

import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.zt.MBS.Model;
import com.zt.Tools.ExcelOperate;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() throws IOException {
        int eeWeight = 2;
        int dayTime = 8;
        double[][] userLocationArray = ExcelOperate.getUserLocation(dayTime);
        Model model = new Model(eeWeight,userLocationArray);

        List<Double> solutionPowerOut = new ArrayList<>();
        try {
            //1、获取文件输入流
            InputStream inputStream = new FileInputStream("ModelData_last_generation_power_out_0.xls");
            //2、获取Excel工作簿对象
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            //3、得到Excel工作表对象
            HSSFSheet sheetAt = workbook.getSheetAt(0);
            //4、循环读取表格数据
            Row row = sheetAt.getRow(1);
            for (int i = 0; i < 25; i++) {
                solutionPowerOut.add(row.getCell(i).getNumericCellValue());
            }
            //5、关闭流
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Double aDouble : solutionPowerOut) {
            System.out.print(aDouble+" ");
        }
        List<Double[]> solutionLocation =new ArrayList<>();
        try {
            //1、获取文件输入流
            InputStream inputStream = new FileInputStream("ModelData_last_generation_location_0.xls");
            //2、获取Excel工作簿对象
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            //3、得到Excel工作表对象
            HSSFSheet sheetAt = workbook.getSheetAt(0);
            //4、循环读取表格数据
            Row row = sheetAt.getRow(1);
            for (int i = 0; i < 25*2; i+=2) {
                solutionLocation.add(new Double[]{row.getCell(i).getNumericCellValue(),row.getCell(i+1).getNumericCellValue()});
            }
            //5、关闭流
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println();
        for (Double[] aDouble : solutionLocation) {
            System.out.print(aDouble[0]+"_"+aDouble[1]+" ");
        }

        double bestValue = model.countEE(solutionLocation,solutionPowerOut);
        System.out.println(bestValue);

    }
}
