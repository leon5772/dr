//package com.crane.utils.Tasks;
//
//import com.crane.service.ITransService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.Scheduled;
//
//@Configuration
//public class GenesisTokenTask {
//
//    @Autowired
//    ITransService transService;
//
//    /**
//     * 每小时执行，更新genesis的token
//     * */
//    @Scheduled(cron = "0 0 0/1 * * ?")
//    public void getGenesisToken() {
//
//        transService.updateGenesisToken();
//    }
//
//}
