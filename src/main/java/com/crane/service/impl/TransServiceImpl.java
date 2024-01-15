package com.crane.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crane.domain.GenesisScene;
import com.crane.domain.SceneObject;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Value("${camera_rel.neuro_to_genesis}")
    public String cameraRelInfo;

    @Override
    public String transJson(JSONObject inputJson) {

        //获取幻方给的通道名字
        String channelName = inputJson.getString("channelName");
        //根据通道名字，从配置拿到对应的genesis相机id
        String genesisCid = getTargetCamForGenesis(channelName);
        if (genesisCid.equals("none")) {
            return null;
        }

        //获取它的类型
        //1: 识别消息（门禁，人脸）.2: 结构化消息.3: 算法仓消息.
        int recordType = inputJson.getIntValue("recordType");

        try {
            if (recordType == 1) {

            } else if (recordType == 2) {

            } else if (recordType == 3) {
                GenesisScene genesisBodyEntity = formatAlgoDetails(genesisCid, inputJson.getJSONObject("detail").getJSONObject("warehouseV20Events"));
                return JSON.toJSONString(genesisBodyEntity);
            }
        } catch (Exception e) {
            logger.error("trans json, json get value error: ", e);
            return null;
        }

        return null;
    }

    private GenesisScene formatAlgoDetails(String genesisCid, JSONObject inputJsonObj) {

        //场景
        GenesisScene resultScene = new GenesisScene();
        resultScene.setCameraId(Integer.valueOf(genesisCid));

        //对象
        List<SceneObject> resultObjects = new ArrayList<>();
        SceneObject sceneObject = new SceneObject();
        sceneObject.setObjectType("Person");

        //标签
        List<String> tagArray = new ArrayList<>();

        try {

            //只取第一个
            JSONObject eventJson = inputJsonObj.getJSONArray("alarmEvents").getJSONObject(0);

            //获取报警的类型
            String inputEventType = eventJson.getString("eventType").toLowerCase();
            if (inputEventType.equals("fight")) {
                tagArray.add("fight");
            } else if (inputEventType.equals("run")) {
                tagArray.add("run");
            } else {
                return null;
            }

            //坐标
            JSONObject targetJson = inputJsonObj.getJSONArray("targets").getJSONObject(0);
            List<Integer> cArray = getGenesisCoord(targetJson.getJSONArray("points"));
            sceneObject.setX(cArray.get(0));
            sceneObject.setY(cArray.get(1));
            sceneObject.setW(cArray.get(2));
            sceneObject.setH(cArray.get(3));


        } catch (Exception e) {
            return null;
        }

        resultObjects.add(sceneObject);
        resultScene.setSceneObjects(resultObjects);
        resultScene.setHashtags(tagArray);
        return resultScene;
    }

    private List<Integer> getGenesisCoord(JSONArray pointsJsonArray) {

        List<Integer> resultCoord = new ArrayList<>();

        //获取左上角，右下角的坐标
        JSONObject upperLeftJson = pointsJsonArray.getJSONObject(0);
        JSONObject bottomRightJson = pointsJsonArray.getJSONObject(1);

        //旷视的分辨率默认解析为1080p
        BigDecimal cameraResWidBD = new BigDecimal("1920");
        BigDecimal cameraResHtBD = new BigDecimal("1080");

        //左上角的像素x坐标
        BigDecimal upperLeftXBD = cameraResWidBD.multiply(BigDecimal.valueOf(upperLeftJson.getFloatValue("x")));
        //左上角的像素y坐标
        BigDecimal upperLeftYBD = cameraResHtBD.multiply(BigDecimal.valueOf(upperLeftJson.getFloatValue("y")));
        //右下角的像素x坐标
        BigDecimal bottomRightXBD = cameraResWidBD.multiply(BigDecimal.valueOf(bottomRightJson.getFloatValue("x")));
        //右下角的像素y坐标
        BigDecimal bottomRightYBD = cameraResHtBD.multiply(BigDecimal.valueOf(bottomRightJson.getFloatValue("y")));

        //Genesis的整数坐标
        Integer genesisX = upperLeftXBD.setScale(0, RoundingMode.HALF_UP).intValue();
        resultCoord.add(genesisX);

        Integer genesisY = upperLeftYBD.setScale(0, RoundingMode.HALF_UP).intValue();
        resultCoord.add(genesisY);

        BigDecimal targetWidBD = bottomRightXBD.subtract(upperLeftXBD);
        resultCoord.add(targetWidBD.setScale(0, RoundingMode.HALF_UP).intValue());

        BigDecimal targetHtBD = bottomRightYBD.subtract(upperLeftYBD);
        resultCoord.add(targetHtBD.setScale(0, RoundingMode.HALF_UP).intValue());

        //返回
        return resultCoord;
    }

    private String getTargetCamForGenesis(String channelName) {

        String genesisCid = "none";

        String[] rel = cameraRelInfo.split(",");

        for (String oneRel : rel) {

            String[] relArray = oneRel.split("=");
            if (channelName.equals(relArray[0])) {
                genesisCid = relArray[1];
                break;
            }
        }

        return genesisCid;
    }

    /**
     * 启动的时候会更新一下genesis的token，避免第一次安装没有token
     */
    @PostConstruct
    @Override
    public void updateGenesisToken() {

        //根据账号密码获取token
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", genesisName);
        bodyMap.put("password", genesisPwd);

        //genesis 地址
        String url = "http://" + genesisAddress + "/ainvr/api/auth";

        //头部
        Header[] headers = {
                new BasicHeader("Content-Type", "application/json")
        };

        try {

            String res = HttpPoolUtil.post(url, bodyMap, headers);

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
     * 每次重启会取消一次订阅，然后再重新开始订阅
     */
    @PostConstruct
    public void reSub() {

        if (StringUtils.isBlank(neuroAddress) || StringUtils.isBlank(genesisAddress) || StringUtils.isBlank(drAddress)) {

            //没填写ip地址直接返回
            logger.error("on start re-sub some ip less");

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
            String re = HttpPoolUtil.post(unSubQuery, unSubMap, headers);
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
        Map<String, Object> subMap = getStringObjectMap();

        //头部
        Header[] headers = {
                new BasicHeader("Content-Type", "application/json")
        };

        try {

            String re = HttpPoolUtil.post(subQuery, subMap, headers);
            if (StringUtils.isNotBlank(re) && re.contains("sessionId")) {
                logger.error("sub success");
            } else {
                logger.error("sub failed");
            }

        } catch (Exception e) {
            logger.error("sub error:", e);
        }

    }

    private Map<String, Object> getStringObjectMap() {
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
        return subMap;
    }

}
