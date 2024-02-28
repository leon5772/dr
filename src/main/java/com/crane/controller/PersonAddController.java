package com.crane.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

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
        }

        List<String> pushSuccessList = new ArrayList<>();
        List<String> pushFailedList = new ArrayList<>();

        pushSuccessList.add("a");
        pushSuccessList.add("b");
        pushSuccessList.add("c");

        pushFailedList.add("e");
        pushFailedList.add("f");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");
        pushFailedList.add("g");

        modelMap.put("wa", "Process Result: " + warning);
        modelMap.put("su", pushSuccessList);
        modelMap.put("fa", pushFailedList);
        return "personPush/result";
    }

}
