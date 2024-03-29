package com.crane.domain;

import java.util.Map;

public class PersonFace {

    private String name;

    private String groupUuid;

    private Map<String,Object> imageData;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public Map<String, Object> getImageData() {
        return imageData;
    }

    public void setImageData(Map<String, Object> imageData) {
        this.imageData = imageData;
    }
}
