package com.crane.domain;

import com.alibaba.excel.annotation.ExcelProperty;

public class OutputData {

    @ExcelProperty(value = "Result",index = 0)
    private String result;

    @ExcelProperty(value = "Time",index = 1)
    private String time;

    @ExcelProperty(value = "Camera",index = 2)
    private String camera;

    @ExcelProperty(value = "Type",index = 3)
    private String type;

    @ExcelProperty(value = "Attribute Text",index = 4)
    private String attribute;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
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
