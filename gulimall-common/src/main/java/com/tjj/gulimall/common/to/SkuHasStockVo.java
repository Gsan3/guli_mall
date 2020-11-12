package com.tjj.gulimall.common.to;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/2
 * Description:
 */
@Data
public class SkuHasStockVo {

    private Long skuId;

    private Boolean hasStock;
}