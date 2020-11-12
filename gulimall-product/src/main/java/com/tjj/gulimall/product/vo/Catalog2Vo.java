package com.tjj.gulimall.product.vo;

import com.sun.tracing.dtrace.ArgsAttributes;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/10
 * Description:
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {

    private String catalog1Id;  //1级父分类id

    private List<Catalog3Vo> catalog3List;  //三级子分类

    private String id;

    private String name;

    /**
     * 静态内部类
     */
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
   public static class Catalog3Vo{
        private String catalog2Id;  //父分类id,2级分类id

        private String id;

        private String name;

    }

}