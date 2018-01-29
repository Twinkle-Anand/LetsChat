package com.example.twinkleanand.whatsapp;

/**
 * Created by Twinkle Anand on 11/24/2017.
 */

public class Users {
    public String name ;
    public String image;
    public String status;
    public String thumbnail;
    public Users(){

    }

    public Users(String name, String image, String status,String thumb_nail) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumbnail = thumb_nail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_nail() {
        return thumbnail;
    }

    public void setThumb_nail(String thumb_nail) {
        this.thumbnail = thumb_nail;
    }
}
