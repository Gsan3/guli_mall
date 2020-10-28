package com.tjj.gulimall.product.dao;

import com.tjj.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
