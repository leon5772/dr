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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class TransServiceImpl implements ITransService {

    private static Logger logger = LoggerFactory.getLogger(TransServiceImpl.class);

    public static String genesisToken = "";

    //第一次启动时，将tag重置，并从新订阅幻方消息
    private static boolean isAppFirstStartFlag = true;

    @Value("${tag_agent_config.tag_agent.address}")
    private String tagAgentAddress;

    @Value("${tag_agent_config.neuro.address}")
    private String neuroAddress;

    @Value("${tag_agent_config.genesis.address}")
    private String genesisAddress;
    @Value("${tag_agent_config.genesis.name}")
    private String genesisName;
    @Value("${tag_agent_config.genesis.password}")
    private String genesisPwd;

    @Value("${tag_agent_config.camera_rel.neuro_to_genesis}")
    public String cameraRelInfo;

    @Override
    @Async("asyncServiceExecutor")
    public void transJson(JSONObject inputJson) {

        //获取幻方给的通道名字
        String channelName = inputJson.getString("channelName");
        //根据通道名字，从配置拿到对应的genesis相机id
        String genesisCid = getTargetCamForGenesis(channelName);

        if (!genesisCid.equals("none")) {

            //获取它的类型
            //1: 识别消息（门禁，人脸）.2: 结构化消息.3: 算法仓消息.
            int recordType = inputJson.getIntValue("recordType");

            try {

                if (recordType == 1) {

                } else if (recordType == 2) {

                } else if (recordType == 3) {

                    //format json to genesis input ask
                    GenesisScene genesisBodyEntity = formatAlgoDetails(genesisCid, inputJson.getJSONObject("detail").getJSONObject("warehouseV20Events"));

                    //download img
                    JSONObject imgInfoJson = inputJson.getJSONObject("detail").getJSONArray("fullImages").getJSONObject(0);
                    String imgUid = imgInfoJson.getJSONObject("imageData").getString("value");
                    String sourceImgUrl = "http://" + neuroAddress + DataRouterConstant.NEURO_API + "/v1/storage/download/"+imgUid;
                    String imgSavePath = downloadPic(channelName, sourceImgUrl);

                    if (!imgSavePath.equals("failed")) {
                        boolean sendStatus = forwardToGenesis(genesisBodyEntity, imgSavePath);

                        if (sendStatus) {
                            try {
                                File f = new File(imgSavePath);
                                if (!f.delete()) {
                                    logger.error("send finished, delete pic failed: ");
                                }
                            } catch (Exception e) {
                                logger.error("send finished, delete pic error : ", e);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("trans json, json get value error: ", e);
            }
        }
    }

    /**
     * 下载幻方给的图片
     */
    private static String downloadPic(String channelName, String sourceImgUrl) {

        //图片指定的路径，一般下载完自动删掉
        String picSavedPath = "./metadata/data/img/";
        picSavedPath = picSavedPath + channelName + "-" + UUID.randomUUID() + ".jpeg";

        try {

            URL url = new URL(sourceImgUrl);

            try (ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream()); InputStream in = Channels.newInputStream(readableByteChannel)) {
                Files.copy(in, Paths.get(picSavedPath), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            logger.error("download pic error ", e);
            return "failed";
        }

        return picSavedPath;
    }

    private boolean forwardToGenesis(GenesisScene genesisBodyEntity, String imgSavePath) {

        try {

            String url = "http://" + genesisAddress + "/ainvr/api/scenes?requiredEngines=Unknown&forceSave=false";

            Header[] headers = {new BasicHeader("X-Auth-Token", genesisToken)};

            HttpPoolUtil.forwardPost(url, imgSavePath, JSON.toJSONString(genesisBodyEntity), headers);

        } catch (Exception e) {
            logger.error("send data to genesis error: ", e);
            return false;
        }

        return true;
    }

    private GenesisScene formatAlgoDetails(String genesisCid, JSONObject algoMsgJson) {

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
            JSONObject eventJson = algoMsgJson.getJSONArray("alarmEvents").getJSONObject(0);

            //获取报警的类型
            String inputEventType = eventJson.getString("eventType").toLowerCase();
            if (inputEventType.equals("fight")) {
                tagArray.add(DataRouterConstant.TAG_FIGHTING);
            } else if (inputEventType.equals("run")) {
                tagArray.add(DataRouterConstant.TAG_RUNNING);
            } else {
                return null;
            }

            //坐标
            JSONObject targetJson = eventJson.getJSONArray("targets").getJSONObject(0);
            List<Integer> cArray = getGenesisCoord(targetJson.getJSONArray("points"));
            sceneObject.setX(cArray.get(0));
            sceneObject.setY(cArray.get(1));
            sceneObject.setW(cArray.get(2));
            sceneObject.setH(cArray.get(3));

            //可信度
            sceneObject.setConfidence(targetJson.getFloatValue("targetScore"));

            //经纬度
            sceneObject.setLatitude(39.038F);
            sceneObject.setLongitude(-72.613F);

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
        BigDecimal cameraResWidBD = new BigDecimal("1280");
        BigDecimal cameraResHtBD = new BigDecimal("720");

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

            String[] relArray = oneRel.split("@");
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
        Header[] headers = {new BasicHeader("Content-Type", "application/json")};

        try {

            String res = HttpPoolUtil.post(url, bodyMap, headers);

            JSONObject resJson = JSON.parseObject(res);
            String strToken = resJson.getString("token");
            if (StringUtils.isNotBlank(strToken)) {
                genesisToken = strToken;
                logger.error("token-update-finished:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
                logger.error(genesisToken);
            }

            //第一次启动的话，重新将默认tag写入到genesis，并重新订阅幻方盒子
            if (isAppFirstStartFlag) {
                reSetTag();
                reSub();
                isAppFirstStartFlag = false;
            }

        } catch (Exception e) {
            logger.error("get genesisToken error:", e);
        }
    }

    /**
     * 开启重新设置tag(这个方法在token方法后)
     */
    public void reSetTag() {

        try {

            //先获取
            String url = "http://" + genesisAddress + "/ainvr/api/hashtags";
            Header[] headers = {new BasicHeader("X-Auth-Token", genesisToken)};
            String re = HttpPoolUtil.httpGet(url, headers);
            JSONArray tagJsonArray = JSON.parseArray(re);

            //删除旧的tag
            if (tagJsonArray != null && !tagJsonArray.isEmpty()) {
                for (Object oneOldTag : tagJsonArray) {
                    String tagStr = (String) oneOldTag;
                    HttpPoolUtil.httpDelete(url + "/" + tagStr, headers);
                }
            }

            //放入新的tag
            for (String tag : DataRouterConstant.TAG_SET) {
                HttpPoolUtil.noJsonPost(url, tag, headers);
            }

            logger.error("reset tag success");
        } catch (Exception e) {
            logger.error("on start re put tags error: ", e);
        }
    }

    /**
     * 每次重启会取消一次订阅，然后再重新开始订阅
     */
    public void reSub() {

        if (StringUtils.isBlank(neuroAddress) || StringUtils.isBlank(genesisAddress) || StringUtils.isBlank(tagAgentAddress)) {

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

        Header[] headers = {new BasicHeader("Content-Type", "application/json")};

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
        Header[] headers = {new BasicHeader("Content-Type", "application/json")};

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
        recordMap1.put("url", "http://" + tagAgentAddress + ":8502/Neuro");
        records.add(recordMap1);
        //通道是http，所以subType选2
        recordMap1.put("pushType", 2);
        //放入总json
        subMap.put("records", records);
        return subMap;
    }

}
