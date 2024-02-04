package com.crane.domain;

import org.apache.poi.ss.usermodel.Picture;

public class OutputData {

    private Picture result;
    private String time;
    private String camera;
    private String type;
    private String attribute;

    public Picture getResult() {
        return result;
    }

    public void setResult(Picture result) {
        this.result = result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCamera() {
        return camera;
    }

    public void setCamera(String camera) {
        this.camera = camera;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
