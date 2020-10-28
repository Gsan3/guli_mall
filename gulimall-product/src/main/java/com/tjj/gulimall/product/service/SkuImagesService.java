package com.tjj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.product.entity.SkuImagesEntity;
import com.tjj.gulimall.product.entity.SkuInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatchSkuImages(List<SkuImagesEntity> skuImagesEntities);
}

