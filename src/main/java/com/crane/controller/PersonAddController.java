package com.crane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping("/person_add_group")
public class PersonAddController {

    private static final String FACE_IMG_FOL = "./metadata/data/img/face/";

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
                            warning = "name repeat or pic not 'jpg' 'jpeg' 'png'.";
                        } else {
                            String imgDataJson = formatAndOnline(inputFolder, files);
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


    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource resource = new FileSystemResource(FACE_IMG_FOL.concat(filename));

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }


    private String formatAndOnline(String inputFolder, File[] files) {

        Map<String, Object> imgDataMap = new HashMap<>();

        ObjectMapper objectMapper = new ObjectMapper();
        String re = "";

        try {

            //清空旧的图片
            File folder = new File(FACE_IMG_FOL);
            File[] oldPics = folder.listFiles();
            if (oldPics!=null){
                for (File oneOld : oldPics) {
                    oneOld.delete();
                }
            }

            //拷贝新的图片
            FileUtils.copyDirectory(new File(inputFolder), new File(FACE_IMG_FOL));

            re = objectMapper.writeValueAsString(imgDataMap);

        } catch (Exception e) {
            logger.error("process pic error",e);
        }

        return re;
    }


    private static boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png"};
        for (String extension : imageExtensions) {
            if (fileName.toLowerCase().endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
