package com.cloud.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 * @author Cloud
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${com.cloud.info.base-package}
@SpringBootApplication(scanBasePackages = {"${com.cloud.info.base-package}.server", "${com.cloud.info.base-package}.module"},
        excludeName = {
            // RPC 相关
//            "org.springframework.cloud.openfeign.FeignAutoConfiguration",
//            "com.cloud.module.system.framework.rpc.config.RpcConfiguration"
        })
public class ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
