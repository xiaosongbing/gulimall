package com.atguigu.gulimall.gateway;

import org.apache.commons.configuration.DatabaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import javax.swing.*;

/**
 * @version 1.0
 * @ClassName: GulimallGateWayApplication
 * @Description: TODO
 * @Author zzwang<br />
 * @Date: 2020/7/3 14:09
 */
/**
 * 1、开启服务注册发现
 *  (配置nacos的注册中心地址)
 * 2、编写网关配置文件
 */
//因依赖common包中引入了mp, 默认配置DataSource, 这里过滤
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient
public class GulimallGateWayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallGateWayApplication.class);
    }
}
