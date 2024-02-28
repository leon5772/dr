package com.crane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/person_add_group")
public class PersonAddController {

    private static Logger logger = LoggerFactory.getLogger(PersonAddController.class);

    @GetMapping("")
    public String personPush(HttpServletRequest request) {

        return "personPush/index";
    }

    @PostMapping("go")
    @ResponseBody
    public String go(HttpServletRequest request) {

        Map<String, String> pushResultMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        pushResultMap.put("push_finished_person", "a,b,c");
        pushResultMap.put("push_failed_person", "d,e,f");

        try {
            String re = objectMapper.writeValueAsString(pushResultMap);
            return re;
        } catch (Exception e) {
            return "{\"error\":\"404\"}";
        }
    }

}
