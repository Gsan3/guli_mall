package com.tjj.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tjj.gulimall.product.service.CategoryBrandRelationService;
import com.tjj.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.product.dao.CategoryDao;
import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查询出所有的数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //先查询出所有的一级分类
        List<CategoryEntity> Level1Menu = categoryEntities.stream().filter(entities -> {
            return entities.getParentCid() == 0;
        }).map((menu) ->{
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1,menu2) ->{
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return Level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO
        //删除之前检查菜单是否被引用

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCateLogPath(Long cateLogId) {

        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(cateLogId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);

        categoryBrandRelationService.updateCascade(category.getCatId(), category.getName());

        //TODO

    }

    @Override
    public List<CategoryEntity> findCategoryOneLevel() {
        return this.baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getCatLevel,1));
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {

        /**
         * 优化业务：将对数据库的多次查询变为1次
         *
         */
        //1.查询所有
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有1级分类
        List<CategoryEntity> categoryOneLevel = getCategoryEntities(selectList,0L);

        //2、封装数据
        Map<String, List<Catalog2Vo>> collect1 = categoryOneLevel.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //查出该1级分类下的所有2级分类
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList,v.getCatId());
            List<Catalog2Vo> catalogEntity = null;
            if (categoryEntities != null) {
                List<Catalog2Vo> catalog2 = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo();
                    catalog2Vo.setCatalog1Id(l2.getParentCid().toString());
                    catalog2Vo.setId(l2.getCatId().toString());
                    catalog2Vo.setName(l2.getName());
                    //查出该2级分类下所有的3级分类
                    List<CategoryEntity> catalog3 = getCategoryEntities(selectList,l2.getCatId());
                    if (catalog3 != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = catalog3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                            catalog3Vo.setCatalog2Id(l3.getParentCid().toString());
                            catalog3Vo.setId(l3.getCatId().toString());
                            catalog3Vo.setName(l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
                catalogEntity = catalog2;
            }

            return catalogEntity;
        }));

        return collect1;
    }

    //抽取的方法，多次用到
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList,Long parentId) {
        //筛选出指定parentId的数据
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentId).collect(Collectors.toList());
        return collect;
        /*return this.baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getParentCid, v.getCatId()));*/
    }

    private List<Long> findParentPath(Long cateLogId, List<Long> paths) {
        CategoryEntity byId = this.getById(cateLogId);
        paths.add(cateLogId);

        if (byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //递归查询出所有的二级和三级分类
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntities -> {
            return categoryEntities.getParentCid() == root.getCatId();
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}