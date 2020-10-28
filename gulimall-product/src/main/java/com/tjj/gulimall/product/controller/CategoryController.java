package com.tjj.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.service.CategoryService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.R;



/**
 * 商品三级分类
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     */
    @RequestMapping("/list/tree")
    public R list(){
        List<CategoryEntity> categoryEntities = categoryService.listWithTree();
        return R.ok().put("data", categoryEntities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category){
		categoryService.save(category);

        return R.ok();
    }

    /**
     * 批量修改
     */
    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category){

        //同时更新其它关联冗余字段
        if (!StringUtils.isEmpty(category.getName())){
            categoryService.updateDetail(category);
        }

        return R.ok();
    }

    /**
     * 删除
     * @RequestBody：获取请求体，必须发Post请求
     * SpringMVC自动将请求体的{json}转换成我们的对象
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
		//categoryService.removeByIds(Arrays.asList(catIds));
        //删除之前检查该菜单是否被引用
        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

}
