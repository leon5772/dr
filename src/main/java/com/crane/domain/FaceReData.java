package com.crane.domain;

public class FaceReData {

    private String faceImgUrl;
    private Double similarity;
    private Integer Age;
    private Integer Gender;
    private String targetImgUrl;
    private String matchName;
    private String listName;
    private String description;
    private String cameraName;
    private String time;

    public String getFaceImgUrl() {
        return faceImgUrl;
    }

    public void setFaceImgUrl(String faceImgUrl) {
        this.faceImgUrl = faceImgUrl;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public Integer getAge() {
        return Age;
    }

    public void setAge(Integer age) {
        Age = age;
    }

    public Integer getGender() {
        return Gender;
    }

    public void setGender(Integer gender) {
        Gender = gender;
    }

    public String getTargetImgUrl() {
        return targetImgUrl;
    }

    public void setTargetImgUrl(String targetImgUrl) {
        this.targetImgUrl = targetImgUrl;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
