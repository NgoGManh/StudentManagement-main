package com.huflit.studentmanagement.models;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Student implements Serializable {

    public String id, name, gender, dob, phone, email, address, image;
    public Boolean isPaid;
    private Timestamp dobTimestamp;
    private List<Map<String, String>> classes;

    public Student() {
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Map<String, String>> getClasses() {
        return classes;
    }

    public void setClasses(List<Map<String, String>> classes) {
        this.classes = classes;
    }
}