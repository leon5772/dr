package com.crane.domain;

import java.util.Map;

public class SceneObject {

    private String objectType;
    private Integer x;
    private Integer y;
    private Integer w;
    private Integer h;
    private Float confidence;

    private TargetMetadata metadata;

    private Float latitude;

    private Float longitude;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public Float getConfidence() {
        return confidence;
    }

    public void setConfidence(Float confidence) {
        this.confidence = confidence;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public TargetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TargetMetadata metadata) {
        this.metadata = metadata;
    }
}
