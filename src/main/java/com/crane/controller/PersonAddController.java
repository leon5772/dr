package com.crane.controller;

import com.crane.domain.PersonFace;
import com.crane.utils.DataRouterConstant;
import com.crane.utils.http.HttpPoolUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/person_add_group")
public class PersonAddController {

    private static Logger logger = LoggerFactory.getLogger(PersonAddController.class);

    @Value("${tag_agent_config.neuro.address}")
    private String magAddress;

    @GetMapping("")
    public String personPush(HttpServletRequest request) {

        return "personPush/index";
    }

    @GetMapping("go")
    public String go(HttpServletRequest request, ModelMap modelMap) {

        //消息
        String warning = "success";
        ObjectMapper objectMapper = new ObjectMapper();

        //拿到输入的路径
        String inputFolder = request.getParameter("folder");
        if (StringUtils.isBlank(inputFolder)) {
            warning = "not right folder";
        } else {
            //验证是否是个文件夹
            try {
                File folder = new File(inputFolder);

                if (folder.exists() && folder.isDirectory()) {
                    File[] files = folder.listFiles();
                    if (files != null) {
                        //文件名字放入set去重
                        Set<String> nameSet = new HashSet<>();
                        for (File onePicFile : files) {
                            //拿到名字并判断是图片
                            String onePicName = onePicFile.getName();
                            if (onePicFile.isFile() && isImageFile(onePicName)) {
                                //截取人的名字
                                int lastDotIndex = onePicName.lastIndexOf(".");
                                nameSet.add(onePicName.substring(0, lastDotIndex));
                            }
                        }

                        //如果图片类型不一样，但是名字重复了，或是文件夹
                        if (nameSet.size() < files.length) {
                            warning = "name repeat or pic not 'jpg|jpeg|bmp|png|JPG|JPEG|BMP|PNG'.";
                        } else {

                            //去拿组的id，如果没有不继续
                            String groupUUID = "";
                            String url = "http://" + magAddress + DataRouterConstant.NEURO_API + "/v1/group/list";
                            Header[] headers = {new BasicHeader("Content-Type", "application/json")};
                            Map<String,Object> jsonBodyMap = new HashMap<>();
                            jsonBodyMap.put("pageSize",20);
                            String res = HttpPoolUtil.post(url, jsonBodyMap, headers);
                            JsonNode resNode = objectMapper.readTree(res);
                            //读取响应数据
                            JsonNode groupListNode = resNode.get("data").get("list");
                            for (JsonNode oneGroupNode : groupListNode) {
                                String oneGroupName = oneGroupNode.get("name").asText();
                                if (oneGroupName.equals("POI")) {
                                    groupUUID = oneGroupNode.get("uuid").asText();
                                    break;
                                }
                            }

                            if (StringUtils.isNotBlank(groupUUID)) {

                                //先上传图片
                                List<PersonFace> pfList = uploadAndFormat(files, groupUUID);

                                while (!pfList.isEmpty()) {

                                    List<PersonFace> batch = pfList.subList(0, Math.min(50, pfList.size()));

                                    // 调用接口处理数据
                                    String onceInsert = sendBatch(batch);
                                    System.out.println(onceInsert);

                                    // 从原list中移除已处理数据
                                    pfList.removeAll(batch);

                                }

                            } else {
                                warning = "no group name called poi";
                            }
                        }
                    } else {
                        warning = "empty folder";
                    }
                } else {
                    warning = "not a folder";
                }
            } catch (Exception e) {
                logger.error("batch error ", e);
                warning = "batch error".concat(e.getMessage());
            }
        }

        //
        List<String> pushSuccessList = new ArrayList<>();
        List<String> pushFailedList = new ArrayList<>();


        modelMap.put("wa", "Notice : " + warning);
        modelMap.put("su", pushSuccessList);
        modelMap.put("fa", pushFailedList);
        return "personPush/result";
    }

    public String sendBatch(List<PersonFace> face) {
        //拿到图片链接，发送给批量写库接口
        String batchInsertUrl = "http://" + magAddress + DataRouterConstant.NEURO_API + "/v1/person/batch_add";
        Header[] batchInsertHeaders = {new BasicHeader("Content-Type", "application/json")};
        return HttpPoolUtil.post(batchInsertUrl, face, batchInsertHeaders);
    }

//    @GetMapping("/images/{filename}")
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//
//        Resource resource = new FileSystemResource(FACE_IMG_FOL.concat(filename));
//
//        return ResponseEntity
//                .ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }

    private List<PersonFace> uploadAndFormat(File[] inputFileArr, String groupUUID) {

        Map<String, String> imgDataMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        List<PersonFace> pfList = new ArrayList<>();

        try {

            //批量上传并拿到url
            for (File oneInputFile : inputFileArr) {

                //先批量上传照片
                String url = "http://" + magAddress + DataRouterConstant.NEURO_API + "/v1/person/uploadImage";
                String res = HttpPoolUtil.uploadFace(url, oneInputFile.getAbsolutePath());

                //读取结果并放入字典
                JsonNode resNode = objectMapper.readTree(res);
                if(resNode.get("code").asInt()==0){
                    String uploadUri = resNode.get("data").get("uri").asText();
                    imgDataMap.put(oneInputFile.getName(), uploadUri);
                }else{
                    continue;
                }
            }

            //形成幻方的格式
            imgDataMap.forEach((key, value) -> {

                //一个实体类
                PersonFace pf = new PersonFace();

                //名字的信息
                int lastDotIndex = key.lastIndexOf(".");
                String oneImgName = key.substring(0, lastDotIndex);
                pf.setName(oneImgName);

                //分组id
                pf.setGroupUuid(groupUUID);

                //图片的信息
                Map<String, Object> imgInfoMap = new HashMap<>();
                imgInfoMap.put("imageType", 1);
                imgInfoMap.put("imageUri", value);
                pf.setImageData(imgInfoMap);

                pfList.add(pf);
            });

            return pfList;

        } catch (Exception e) {
            logger.error("format json error", e);
        }

        return pfList;
    }


    private static boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", "bmp"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
