package com.huflit.studentmanagement.listeners;

import com.huflit.studentmanagement.models.Teacher;

public interface TeacherListener {
    void onTeacherClick(Teacher teacher);
    void onTeacherLongClick(Teacher teacher);
}
