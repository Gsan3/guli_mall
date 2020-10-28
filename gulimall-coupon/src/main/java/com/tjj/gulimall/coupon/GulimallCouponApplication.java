package com.tjj.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 如何引入nacos做配置中心
 * 1.引入依赖
 * <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
 *         </dependency>
 * 2.创建一个bootstrap.properties配置文件
 *      spring.application.name=gulimall-coupon
 *      spring.cloud.nacos.config.server-addr=127.0.0.1:8848
 * 3.在配置中心默认添加一个叫数据集(Data Id),默认名称gulimall-coupon.properties，默认规则"应用名.properties"
 *4.给 应用名.properties 添加任何配置
 * 5.动态获取配置
 *      @RefreshScope  动态刷新和获取配置
 *      @Value("${配置项目名}")   获取配置的值
 *      如果配置中心和当前配置文件中都配置了同样的配置项目名，优先使用配置中心的配置
 *
 * 更多细节：
 * 1.命名空间：配置隔离
 *      默认：public（保留空间），默认所有新增配置都在public空间
 *      1）开发、测试、生产：利用命名空间进行环境隔离
 *          注意：在bootstrap.properties配置上，需要使用哪个命名空间下配置
 *          spring.cloud.nacos.config.namespace=命名空间ID
 *      2）每一个微服务之间互相隔离配置，在每一个微服务都创建自己的命名空间，只加载自己命名空间下的所有配置
 * 2.配置集：所有的配置集合
 *
 * 3.配置集Id：配置文件名
 *      Data Id：文件名
 *
 * 4.配置分组：
 *      默认所有的配置集都属于：DEFAULT_GROUP
 *
 * 5.每个服务创建自己的命名空间，使用配置分组区分环境：dev，test，prod
 *
 * 6.从配置中心同时加载多个配置集
 *      微服务任何配置信息，任何配置文件都可以放在配置中心中
 *      只需要在bootstrap.properties中说明加载配置中心中哪些配置文件
 *      @Value、@ConfigurationProperties，以前springboot任何方法都配置文件中获取信息都能使用
 * 7.默认配置中心有的优先使用配置中心的
 *
 */

@EnableDiscoveryClient
@SpringBootApplication
public class GulimallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCouponApplication.class, args);
    }

}
