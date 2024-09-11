package com.huflit.studentmanagement.utilities;

import android.content.Context;
import android.os.Environment;
import com.huflit.studentmanagement.models.Grade;
import com.huflit.studentmanagement.models.Student;
import com.huflit.studentmanagement.models.Teacher;
import com.huflit.studentmanagement.models.Timetable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtils {
    public ExcelUtils() {
    }

    public static List<Teacher> readTeachersFromExcel(InputStream inputStream) {
        List<Teacher> teachers = new ArrayList();

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator var4 = sheet.iterator();

            while(var4.hasNext()) {
                Row row = (Row)var4.next();
                if (row.getRowNum() != 0) {
                    Teacher teacher = new Teacher();
                    teacher.setId(row.getCell(0).getStringCellValue());
                    teacher.setName(row.getCell(1).getStringCellValue());
                    teacher.setPhone(row.getCell(2).getStringCellValue());
                    teacher.setEmail(row.getCell(3).getStringCellValue());
                    teacher.setSubject(row.getCell(4).getStringCellValue());
                    teacher.setGender(row.getCell(5).getStringCellValue());
                    teacher.setDob(row.getCell(6).getStringCellValue());
                    teacher.setImage(row.getCell(7).getStringCellValue());
                    teachers.add(teacher);
                }
            }

            workbook.close();
            inputStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return teachers;
    }

    public static List<Student> readStudentsFromExcel(InputStream inputStream) {
        List<Student> students = new ArrayList();

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator var4 = sheet.iterator();

            while(var4.hasNext()) {
                Row row = (Row)var4.next();
                if (row.getRowNum() != 0) {
                    Student student = new Student();
                    student.setId(row.getCell(0).getStringCellValue());
                    student.setName(row.getCell(1).getStringCellValue());
                    student.setPhone(row.getCell(2).getStringCellValue());
                    student.setEmail(row.getCell(3).getStringCellValue());
                    student.setAddress(row.getCell(4).getStringCellValue());
                    student.setGender(row.getCell(5).getStringCellValue());
                    student.setDob(row.getCell(6).getStringCellValue());
                    student.setImage(row.getCell(7).getStringCellValue());
                    students.add(student);
                }
            }

            workbook.close();
            inputStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return students;
    }

    public static List<Grade> readGradesFromExcel(InputStream inputStream) {
        List<Grade> grades = new ArrayList();

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator var4 = sheet.iterator();

            while(var4.hasNext()) {
                Row row = (Row)var4.next();
                if (row.getRowNum() != 0) {
                    Grade grade = new Grade();
                    grade.setId(row.getCell(0).getStringCellValue());
                    grade.setBand(row.getCell(1).getStringCellValue());
                    grade.setType(row.getCell(2).getStringCellValue());
                    grade.setSubject(row.getCell(3).getStringCellValue());
                    grade.setStudentId(row.getCell(4).getStringCellValue());
                    grade.setClassId(row.getCell(5).getStringCellValue());
                    grades.add(grade);
                }
            }

            workbook.close();
            inputStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return grades;
    }

    public static List<Timetable> readTimetablesFromExcel(InputStream inputStream) {
        List<Timetable> timetables = new ArrayList();

        try {
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator var4 = sheet.iterator();

            while(var4.hasNext()) {
                Row row = (Row)var4.next();
                if (row.getRowNum() != 0) {
                    Timetable timetable = new Timetable();
                    timetable.setId(row.getCell(0).getStringCellValue());
                    timetable.setClassId(row.getCell(1).getStringCellValue());
                    timetable.setDay(row.getCell(2).getStringCellValue());
                    timetable.setPeriod(row.getCell(3).getStringCellValue());
                    timetable.setSubject(row.getCell(4).getStringCellValue());
                    timetable.setTeacher(row.getCell(5).getStringCellValue());
                    timetables.add(timetable);
                }
            }

            workbook.close();
            inputStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }

        return timetables;
    }

    public static void exportStudentsToExcel(Context context, List<Student> students, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Name");
        headerRow.createCell(2).setCellValue("Phone");
        headerRow.createCell(3).setCellValue("Email");
        headerRow.createCell(4).setCellValue("Address");
        headerRow.createCell(5).setCellValue("Gender");
        headerRow.createCell(6).setCellValue("DOB");
        headerRow.createCell(7).setCellValue("Image");
        int rowNum = 1;
        Iterator var7 = students.iterator();

        while(var7.hasNext()) {
            Student student = (Student)var7.next();
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(student.getId());
            row.createCell(1).setCellValue(student.getName());
            row.createCell(2).setCellValue(student.getPhone());
            row.createCell(3).setCellValue(student.getEmail());
            row.createCell(4).setCellValue(student.getAddress());
            row.createCell(5).setCellValue(student.getGender());
            row.createCell(6).setCellValue(student.getDob());
            row.createCell(7).setCellValue(student.getImage());
        }

        try {
            File file = new File(Environment.getExternalStorageDirectory(), fileName + ".xlsx");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
            workbook.close();
            Utils.ShowToast(context, "File saved: " + file.getAbsolutePath());
        } catch (Exception var10) {
            var10.printStackTrace();
            Utils.ShowToast(context, "Failed to save file!");
        }

    }
}