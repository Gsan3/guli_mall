package com.tjj.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjj.gulimall.product.entity.BrandEntity;
import com.tjj.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tjj.gulimall.product.service.CategoryBrandRelationService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * controller:处理请求，接受和校验数据
     * service：结合搜controller传送过来的数据，进行业务处理
     * controller：接收service处理完的数据，封装成页面指定的VO
     */

    /**
     * 获取品牌关联的分类
     * 参数：brandId
     * /product/categorybrandrelation/brands/list
     */
    @GetMapping("/brands/list")
    public R relationBrandList(@RequestParam(value = "catId",required = true) Long catId){
        List<BrandEntity> brandEntityList = categoryBrandRelationService.getBrandsByCatId(catId);

        List<BrandVo> collect = brandEntityList.stream().map(data -> {
            BrandVo brandVo = new BrandVo();
            brandVo.setBrandId(data.getBrandId());
            brandVo.setBrandName(data.getName());
            return brandVo;
        }).collect(Collectors.toList());

        return R.ok().put("data",collect);
    }

    /**
     * 获取品牌关联的所有分类列表
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam("brandId") Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.list(
                new LambdaQueryWrapper<CategoryBrandRelationEntity>().
                        eq(CategoryBrandRelationEntity::getBrandId, brandId)
        );

        return R.ok().put("data", data);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
