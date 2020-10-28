package com.tjj.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tjj.gulimall.product.service.CategoryBrandRelationService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.product.dao.BrandDao;
import com.tjj.gulimall.product.entity.BrandEntity;
import com.tjj.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<BrandEntity> brandEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (!StringUtils.isEmpty(key)){
            brandEntityLambdaQueryWrapper.eq(BrandEntity::getName,key).or().eq(BrandEntity::getBrandId,key);
        }

        IPage<BrandEntity> page = this.page(new Query<BrandEntity>().getPage(params), brandEntityLambdaQueryWrapper);

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void updateDetail(BrandEntity brand) {
        brandService.updateById(brand);

        categoryBrandRelationService.updateDetail(brand.getBrandId(), brand.getName());

        //TODO

    }

}