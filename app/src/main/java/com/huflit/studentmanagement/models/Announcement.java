package com.huflit.studentmanagement.models;
import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.Date;

public class Announcement implements Serializable {
    public String id, title, content, time, classId;

    private Timestamp dateToTimestamp;


    public Announcement() {
    }


    public java.lang.String getId() {
        return id;
    }

    public void setId(java.lang.String id) {
        this.id = id;
    }
    public java.lang.String getTitle() {
        return title;
    }

    public void setTitle(java.lang.String title) {
        this.title = title;
    }

    public java.lang.String getContent() {
        return content;
    }

    public void setContent(java.lang.String content) {
        this.content = content;
    }
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public Timestamp getDateToTimestamp() {
        return dateToTimestamp;
    }

    public void setDateToTimestamp(Timestamp dateToTimestamp) {
        this.dateToTimestamp = dateToTimestamp;
    }
}
