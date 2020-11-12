package com.tjj.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到cateLogId的完整路径
     * @param cateLogId
     * @return
     */
    Long [] findCateLogPath(Long cateLogId);

    void updateDetail(CategoryEntity category);

    List<CategoryEntity> findCategoryOneLevel();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

