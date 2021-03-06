package com.tjj.gulimall.product;

import com.aliyun.oss.OSSClient;
import com.tjj.gulimall.product.entity.BrandEntity;
import com.tjj.gulimall.product.service.BrandService;
import com.tjj.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 1、引入oss-starter
 *      <dependency>
 *             <groupId>com.alibaba.cloud</groupId>
 *             <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
 *         </dependency>
 * 2、配置key、endpoint信息
 *      alicloud:
 *       access-key: LTAI4GKvwNfcaak5KFcu1Zvk
 *       secret-key: 4kLFitu2UVFB0B8GKtqhnPFGuJvEZI
 *       oss:
 *         endpoint: oss-cn-shenzhen.aliyuncs.com
 * 3、使用ossClient进行相关操作
 */
@Slf4j
@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void testRedisson(){
        System.out.println(redissonClient);
    }

    @Test
    public void testRedis(){
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello","world"+ UUID.randomUUID());
        String s = ops.get("hello");
        System.out.println("数据："+s);
    }

    @Test
    public void findCateLogPath(){
        Long[] cateLogPath = categoryService.findCateLogPath(225L);
        log.info("完整路径：{}", Arrays.asList(cateLogPath));
    }

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("联想");
        brandService.save(brandEntity);
        System.out.println("保存成功！");
    }

}
