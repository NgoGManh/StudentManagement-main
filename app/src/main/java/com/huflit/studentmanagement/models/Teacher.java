package com.huflit.studentmanagement.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;


public class Teacher implements Serializable {

    public String id, name, gender, dob, phone, email, subject, image;

    private Timestamp dobTimestamp;

    public Teacher() {
    }

    public Timestamp getDobTimestamp() {
        return dobTimestamp;
    }

    public void setDobTimestamp(Timestamp dobTimestamp) {
        this.dobTimestamp = dobTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
