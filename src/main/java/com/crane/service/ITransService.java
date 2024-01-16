package com.crane.service;

import com.alibaba.fastjson.JSONObject;

public interface ITransService {

    void transJson(JSONObject inputJson);

    void updateGenesisToken();
}
