package com.huflit.studentmanagement.listeners;

import com.huflit.studentmanagement.models.Student;

public interface StudentListener {
    void onStudentClick(Student student);
    void onStudentLongClick(Student student);
    void onStudentChange(Student student);
}
