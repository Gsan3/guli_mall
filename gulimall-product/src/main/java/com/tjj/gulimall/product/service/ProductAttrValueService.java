package com.tjj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatchProductAttrValue(List<ProductAttrValueEntity> collect);
}

