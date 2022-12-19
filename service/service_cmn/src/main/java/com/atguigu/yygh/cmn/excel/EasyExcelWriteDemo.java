package com.atguigu.yygh.cmn.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenyj
 * @create 2022-12-02 12:37
 */
public class EasyExcelWriteDemo {

// 方式一：往单个sheet中写数据
//    public static void main(String[] args) {
//    List<Student> students = new ArrayList<>();
//    students.add(new Student(1,"朱晓溪",18,true));
//    students.add(new Student(2,"常永亮",19,true));
//    students.add(new Student(3,"段磊",20,true));
//    students.add(new Student(4,"田佳",21,true));

//        //这种方式: 只能往单个sheet中写数据
//        EasyExcel.write("C:\\Users\\chenyj\\Desktop\\hello.xlsx", Student.class).sheet("学生列表1").doWrite(students);
//    }

    //往多个sheet中写数据
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student(1,"朱晓溪",18,true));
        students.add(new Student(2,"常永亮",19,true));
        students.add(new Student(3,"段磊",20,true));
        students.add(new Student(4,"田佳",21,true));

        List<Student> studentList = new ArrayList<>();
        studentList.add(new Student(5,"梁启晨",18,true));
        studentList.add(new Student(6,"王志锋",19,true));

        ExcelWriter excelWriter = EasyExcel.write("C:\\Users\\chenyj\\Desktop\\abc.xlsx", Student.class).build();
        WriteSheet sheet1 = EasyExcel.writerSheet(0, "学生列表1").build();
        WriteSheet sheet2 = EasyExcel.writerSheet(1, "学生列表2").build();

        excelWriter.write(students, sheet1);
        excelWriter.write(studentList, sheet2);

        // 千万别忘记finish 会帮忙关闭流
        excelWriter.finish();;
    }
}
