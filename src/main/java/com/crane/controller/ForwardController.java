package com.crane.controller;

import com.alibaba.fastjson.JSONObject;
import com.crane.service.ITransService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RequestMapping("/Neuro")
@RestController
public class ForwardController {

    private static Logger logger = LoggerFactory.getLogger(ForwardController.class);

    @Autowired
    ITransService transService;

    @Value("${dl_data_router.genesis.address}")
    private String genesisAddress;

    @PostMapping("")
    public String forwardRequest(HttpServletRequest request) {

        JSONObject inJson = getBodyJson(request);
        if (Objects.isNull(inJson)) {
            return "{\"code\":500}";
        }

        //将幻方盒子得到的数据，转为genesis的格式
        transService.transJson(inJson);

        //发给genesis
        dataToGenesis();

        return "{\"code\":200}";
    }

    private void dataToGenesis() {

    }

    //获取body参数
    public static JSONObject getBodyJson(HttpServletRequest request) {
        try (InputStream inputStream = request.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            byte[] requestBodyBytes = outputStream.toByteArray();
            String requestBody = new String(requestBodyBytes, StandardCharsets.UTF_8);
            return JSONObject.parseObject(requestBody);
        } catch (Exception e) {
            return null;
        }
    }

}
