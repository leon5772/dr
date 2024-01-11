package com.crane.service;

import com.alibaba.fastjson.JSONObject;

public interface ITransService {

    String transJson(JSONObject inputJson);

    void updateGenesisToken();
}
