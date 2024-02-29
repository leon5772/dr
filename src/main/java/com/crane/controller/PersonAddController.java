package com.crane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
                            warning = "name repeat or pic not 'jpg' 'jpeg' 'png'.";
                        } else {
                            String imgDataJson = formatAndOnline(files);
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

    @GetMapping("/img/face/{imageName}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String imageName) throws IOException, IOException {
        Resource resource = new ClassPathResource("/metadata/img/face" + imageName);
        byte[] imageBytes = Files.readAllBytes(resource.getFile().toPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", imageName);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    private String formatAndOnline(File[] files) {

        Map<String, Object> imgDataMap = new HashMap<>();



        ObjectMapper objectMapper = new ObjectMapper();
        String re = "";
        try {
            re = objectMapper.writeValueAsString(imgDataMap);
        } catch (Exception ignored) {

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
