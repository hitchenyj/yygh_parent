package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;

/**
 * @author chenyj
 * @create 2022-12-02 14:19
 */
public class EasyExcelReadDemo {
//    public static void main(String[] args) {
//        EasyExcel.read("C:\\Users\\chenyj\\Desktop\\hello.xlsx", Student.class, new StudentReadListener()).sheet(0).doRead();
//    }

    //读取excel文件中的多个sheet
    public static void main(String[] args) {
        ExcelReader excelReader = EasyExcel.read("C:\\Users\\chenyj\\Desktop\\abc.xlsx").build();
        ReadSheet sheet1 = EasyExcel.readSheet(0).head(Student.class).registerReadListener(new StudentReadListener()).build();
        ReadSheet sheet2 = EasyExcel.readSheet(1).head(Student.class).registerReadListener(new StudentReadListener()).build();

        excelReader.read(sheet1, sheet2);
        excelReader.finish();
    }
}


