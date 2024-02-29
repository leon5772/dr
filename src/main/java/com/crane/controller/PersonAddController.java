package com.crane.controller;

import com.crane.domain.PersonFace;
import com.crane.utils.DataRouterConstant;
import com.crane.utils.http.HttpPoolUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @GetMapping("")
    public String personPush(HttpServletRequest request) {

        return "personPush/index";
    }

    @GetMapping("go")
    public String go(HttpServletRequest request, ModelMap modelMap) {

        //消息
        String warning = "success";

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
                            String imgDataJson = uploadAndFormat(files);
                        }
                    } else {
                        warning = "empty folder";
                    }
                } else {
                    warning = "not a folder";
                }
            } catch (Exception e) {
                logger.error("not right folder ", e);
                warning = "not a folder";
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

    private String uploadAndFormat(File[] inputFileArr) {

        Map<String, Object> imgDataMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String re = "";

        try {

            //批量上传并拿到url
            for (File oneInputFile : inputFileArr) {
                String url = "http://" + DataRouterConstant.NEURO_API + "/v1/person/uploadImage";
                //Header[] headers = {new BasicHeader("X-Auth-Token", genesisToken)};
                Header[] headers = {new BasicHeader("Content-Type", "application/json")};
                HttpPoolUtil.uploadFace(url,oneInputFile.getAbsolutePath(),headers);
            }

            //形成幻方的格式
            List<PersonFace> fList = new ArrayList<>();
            File[] newFileArr = folder.listFiles();
            for (File oneNewFile : newFileArr) {
                PersonFace pf = new PersonFace();

                //去掉后缀名
                String oneFileName = oneNewFile.getName();
                int lastDotIndex = oneFileName.lastIndexOf(".");
                pf.setName(oneFileName.substring(0, lastDotIndex));
                Map<String, String>
            }


            re = objectMapper.writeValueAsString(imgDataMap);

        } catch (Exception e) {
            logger.error("format json error", e);
        }

        return re;
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
