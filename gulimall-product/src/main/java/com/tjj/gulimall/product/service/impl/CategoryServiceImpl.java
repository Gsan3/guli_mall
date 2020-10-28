package com.tjj.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tjj.gulimall.product.service.CategoryBrandRelationService;
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