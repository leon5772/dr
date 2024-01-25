package com.crane.domain;

import java.util.Map;

public class TargetMetadata {

    private  String[] colors;

    private Map<String,Object> face;

    public String[] getColors() {
        return colors;
    }

    public void setColors(String[] colors) {
        this.colors = colors;
    }

    public Map<String, Object> getFace() {
        return face;
    }

    public void setFace(Map<String, Object> face) {
        this.face = face;
    }
}

