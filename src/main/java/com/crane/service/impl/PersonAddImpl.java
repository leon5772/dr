package com.crane.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crane.domain.GenesisScene;
import com.crane.domain.SceneObject;
import com.crane.domain.TargetMetadata;
import com.crane.service.IPersonAddService;
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

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PersonAddImpl implements IPersonAddService {

    private static Logger logger = LoggerFactory.getLogger(PersonAddImpl.class);

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

}
