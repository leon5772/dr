package com.crane.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crane.service.ITransService;
import com.crane.utils.DataRouterConstant;
import com.crane.utils.http.HttpPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransServiceImpl implements ITransService {

    private static Logger logger = LoggerFactory.getLogger(TransServiceImpl.class);

    public static String genesisToken = "";

    @Value("${dl_data_router.address}")
    private String drAddress;

    @Value("${dl_data_router.neuro.address}")
    private String neuroAddress;

    @Value("${dl_data_router.genesis.address}")
    private String genesisAddress;
    @Value("${dl_data_router.genesis.name}")
    private String genesisName;
    @Value("${dl_data_router.genesis.password}")
    private String genesisPwd;

    @Override
    public String transJson(JSONObject inputJson) {
        System.out.println("112233");
        return null;
    }

    @PostConstruct
    @Override
    public void updateGenesisToken() {

        //根据账号密码获取token
        JSONObject body = new JSONObject();
        body.put("username", genesisName);
        body.put("password", genesisPwd);

        //genesis 地址
        String url = "http://" + genesisAddress + "/ainvr/api/auth";

        //头部
        Header[] headers = {
                new BasicHeader("Content-Type", "application/json")
        };

        try {

            String res = HttpPoolUtil.post(url, body.toJSONString(), headers);

            JSONObject resJson = JSON.parseObject(res);
            String strToken = resJson.getString("token");
            if (StringUtils.isNotBlank(strToken)) {
                genesisToken = strToken;
                logger.error("token-update-finished:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                logger.error(genesisToken);
                logger.error("token-update-finished:<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }

        } catch (Exception e) {
            logger.error("get genesisToken error:", e);
        }
    }

    /**
     * 我们使用http通道订阅
     */
    @PostConstruct
    public void startSub() {

        if (StringUtils.isBlank(neuroAddress) || StringUtils.isBlank(genesisAddress) || StringUtils.isBlank(drAddress)) {

            //没填写ip地址直接返回
            logger.error("ip less");

        } else {

            //step1,先取消所有的订阅 ----------------------------------------------------/
            String unSubResult = unSubMsg();
            if (StringUtils.isNotBlank(unSubResult)) {
                //取消订阅后，step2，订阅http到genesis的机器 ----------------------------------------------------/
                subMsg();
            }
        }
    }

    public String unSubMsg() {

        String unSubQuery = "http://" + neuroAddress + DataRouterConstant.NEURO_API + "/v1/event/record/unsubscribe";

        Map<String, Object> unSubMap = new HashMap<>();
        unSubMap.put("channelUuid", "*");

        Header[] headers = {
                new BasicHeader("Content-Type", "application/json")
        };

        try {
            String re = HttpPoolUtil.post(unSubQuery, JSON.toJSONString(unSubMap), headers);
            if (StringUtils.isNotBlank(re) && (re.contains("0,") || re.contains("100005,"))) {
                logger.error("unSub success");
                return "1";
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("dr on start,unSub query error:", e);
            return null;
        }
    }

    public void subMsg() {

        //取消的api
        String subQuery = "http://" + neuroAddress + DataRouterConstant.NEURO_API + "/v1/event/record/subscribe";

        //所有摄像头就是所有通道
        Map<String, Object> subMap = new HashMap<>();
        subMap.put("channelUuid", "*");

        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> recordMap1 = new HashMap<>();
        //genesis跟dr装同一个机器，所以ip指向到genesis即可
        recordMap1.put("url", "http://" + drAddress + ":8502/Neuro");
        records.add(recordMap1);
        //通道是http，所以subType选2
        recordMap1.put("pushType", 2);
        //放入总json
        subMap.put("records", records);

        //头部
        Header[] headers = {
                new BasicHeader("Content-Type", "application/json")
        };

        try {

            String re = HttpPoolUtil.post(subQuery, JSON.toJSONString(subMap), headers);
            if (StringUtils.isNotBlank(re) && re.contains("sessionId")) {
                logger.error("sub success");
            } else {
                logger.error("sub failed");
            }

        } catch (Exception e) {
            logger.error("sub error:", e);
        }

    }

}
