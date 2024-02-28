package com.crane.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<String> pushSuccessList = new ArrayList<>();
        List<String> pushFailedList = new ArrayList<>();

        pushSuccessList.add("a");
        pushSuccessList.add("b");
        pushSuccessList.add("c");

        pushFailedList.add("e");
        pushFailedList.add("f");
        pushFailedList.add("g");

        modelMap.put("su",pushSuccessList);
        modelMap.put("fa",pushFailedList);
        return "personPush/result";
    }

}
