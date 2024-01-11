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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RequestMapping("/Neuro")
@RestController
public class ForwardController {

    private static Logger logger = LoggerFactory.getLogger(ForwardController.class);

    @Autowired
    ITransService transService;

    @Value("${dl_data_router.genesis.address}")
    private String address;

    @PostMapping("")
    public String forwardRequest(HttpServletRequest request) {

        JSONObject inJson = getBodyJson(request);
        if (Objects.isNull(inJson)) {
            return "{\"code\":500}";
        }

        System.out.println(inJson.toJSONString());

        //将幻方盒子得到的数据，转为genesis的格式
        transService.transJson(inJson);

        return "{\"code\":200}";
    }

    //获取参数
    public static JSONObject getBodyJson(HttpServletRequest request) {

        try {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;

            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
            return JSONObject.parseObject(responseStrBuilder.toString());

        } catch (Exception e) {
            return null;
        }
    }

}
