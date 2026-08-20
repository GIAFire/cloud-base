package com.cloud.module.AiFriend;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * 项目的启动类
 * @author Cloud
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AiFriendServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiFriendServerApplication.class, args);
    }

}
