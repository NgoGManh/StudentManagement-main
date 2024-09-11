package com.huflit.studentmanagement.models;
import java.io.Serializable;

public class Image implements Serializable {
    public String image;

    public int id;

    public Image() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
