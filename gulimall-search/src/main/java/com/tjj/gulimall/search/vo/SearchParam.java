package com.tjj.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/16
 * Description: 封装页面所有可能传递过来的查询条件
 */
@Data
public class SearchParam {
    /**
     * 全文检索条件分析
     * 1、全文检索：skuTitle->keyWord
     * 2、排序：saleCount(销量)、hotScore(热度分)、skuPrice(价格)
     * 3、过滤：hasStock、skuPrice区间、brandId、catalog3Id、attrs
     * 4、聚合：attrs
     */

    //页面传递过来的全文匹配关键字
    private String keyWord;

    //三级分类Id
    private Long catalog3Id;

    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    //排序条件
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock(是否有货)、skuPrice(区间)、brandId
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     */
    //是否有货
    private Integer hasStock;

    //价格区间查询
    private String skuPrice;

    //按品牌选择
    private List<Long> brandId;

    //按照属性进行筛选
    private List<String> attrs;

    //页码查询
    private Integer pageNum = 1;

    /**
     * 原生的所有查询条件
     */
    private String _queryString;

}