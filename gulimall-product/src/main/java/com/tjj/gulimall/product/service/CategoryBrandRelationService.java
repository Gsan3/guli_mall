package com.tjj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.product.entity.BrandEntity;
import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateDetail(Long brandId, String name);

    void updateCascade(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

