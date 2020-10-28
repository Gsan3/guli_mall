package com.tjj.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 整合mybatis-plus
 *1.导入依赖
 *      <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.3.2</version>
 *         </dependency>
 * 2.配置
 *      1)配置数据源：
 *          1.导入数据库驱动：
 *                  <dependency>
 *                      <groupId>mysql</groupId>
 *                      <artifactId>mysql-connector-java</artifactId>
 *                      <version>8.0.17</version>
 *                  </dependency>
 *          2.在application.yml中配置数据源相关信息
 *       2）配置mybatis-plus
 *          1.使用MapperScan
 *          2.告诉mybatis-plus，sql映射文件配置
 * 3、使用逻辑删除
 *      1）在entity中添加@TableLogic注解 ：两个元素value表示不删除的，delval表示删除的
 *
 * 4、JSR303：
 *     1）给Bean添加校验注解：javax.validation.constraints，并自定义message信息
 *     2)开启校验功能@Valid
 *     3)分组校验：
 *           1.@NotBlank(message = "品牌名必须提交", groups = {AddGroup.class, UpdateGroup.class})
 *             给注解上标注什么情况需要进行校验
 *           2.@Validated({AddGroup.class})
 *           3.默认没有指定分组的校验注解，在分组校验的情况@Vaildated({AddGroup.class})下不生效
 *     4）自定义校验：
 *          1.编写一个自定义校验注解
 *          2.编写一个自定义校验器
 *          3。关联自定义的校验注解和校验器
 *          @Documented
 *          @Constraint(validatedBy = {ListValueConstraintValidator.class})
 *          @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
 *          @Retention(RetentionPolicy.RUNTIME)
 *
 * 5、统一的异常处理
 * @controllerAdvice
 *     1)、
 */
@EnableFeignClients(basePackages = "com.tjj.gulimall.product.feign")  //开启远程调用功能
@MapperScan("com.tjj.gulimall.product.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
