package com.crane.utils.Tasks;

import com.crane.service.ITransService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class GenesisTokenTask {

    @Autowired
    ITransService transService;

    /**
     * 每小时执行
     * */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void getGenesisToken() {

        transService.updateGenesisToken();
    }

}
