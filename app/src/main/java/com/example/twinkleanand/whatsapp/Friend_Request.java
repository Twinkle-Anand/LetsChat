package com.example.twinkleanand.whatsapp;

/**
 * Created by Twinkle Anand on 1/28/2018.
 */

public class Friend_Request{
    String request_type;


    public Friend_Request(){

    }
    public Friend_Request(String request_type){
        this.request_type = request_type;

    }
    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }

}
