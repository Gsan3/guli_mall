package com.tjj.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 想要远程调用别的服务
 * 1.引入open-feign
 *      <dependency>
 *             <groupId>org.springframework.cloud</groupId>
 *             <artifactId>spring-cloud-starter-openfeign</artifactId>
 *         </dependency>
 * 2.编写一个接口，告诉SpringCloud这个接口需要调用远程服务（@FeignClient)
 *      1）声明接口的每个方法都是调用哪个服务的那个请求
 *  3.开启远程调用功能（@EnableFeignClients）
 *
 */

@EnableFeignClients(basePackages = "com.tjj.gulimall.member.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallMemberApplication.class, args);
    }

}
