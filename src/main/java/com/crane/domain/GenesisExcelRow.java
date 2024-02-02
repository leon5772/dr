package com.crane.domain;

public class GenesisExcelRow {

    private String sceneId;
    private Integer oldIndex;

    private Integer newIndex;
    private Integer objectId;
    private String Type;
    private String attr;

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public Integer getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(Integer oldIndex) {
        this.oldIndex = oldIndex;
    }

    public Integer getObjectId() {
        return objectId;
    }

    public void setObjectId(Integer objectId) {
        this.objectId = objectId;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public Integer getNewIndex() {
        return newIndex;
    }

    public void setNewIndex(Integer newIndex) {
        this.newIndex = newIndex;
    }
}
