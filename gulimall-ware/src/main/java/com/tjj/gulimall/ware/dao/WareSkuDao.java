package com.tjj.gulimall.ware.dao;

import com.tjj.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:55:56
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    //有一个参数可以不用@Param，有多个参数一定要用@Param

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId,@Param("skuNum") Integer skuNum);

    Long getSkuStock(Long sku);
}
