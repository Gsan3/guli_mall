package com.tjj.gulimall.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/1
 * Description:
 */
@Data
public class SkuEsModel {

    private Long skuId;

    private Long spuId;

    private String skuTitle;

    private BigDecimal skuPrice;

    private String skuImg;

    private Long saleCount;

    private Boolean hasStock;

    private Long hotScore;

    private Long brandId;

    private Long catelogId;

    private String brandName;

    private String brandImg;

    private String catelogName;

    private List<Attrs> attrs;

    @Data
    public static class Attrs{

        private Long attrId;

        private String attrName;

        private String attrValue;
    }
}