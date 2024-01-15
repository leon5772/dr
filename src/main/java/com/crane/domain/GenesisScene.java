package com.crane.domain;

import java.util.List;

public class GenesisScene {

    private Integer cameraId;

    private List<SceneObject> sceneObjects;

    private List<String> hashtags;


    public Integer getCameraId() {
        return cameraId;
    }

    public void setCameraId(Integer cameraId) {
        this.cameraId = cameraId;
    }

    public List<SceneObject> getSceneObjects() {
        return sceneObjects;
    }

    public void setSceneObjects(List<SceneObject> sceneObjects) {
        this.sceneObjects = sceneObjects;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public void setHashtags(List<String> hashtags) {
        this.hashtags = hashtags;
    }
}
