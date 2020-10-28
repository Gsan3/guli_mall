package com.tjj.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tjj.gulimall.product.entity.AttrEntity;
import com.tjj.gulimall.product.service.AttrAttrgroupRelationService;
import com.tjj.gulimall.product.service.AttrService;
import com.tjj.gulimall.product.service.CategoryService;
import com.tjj.gulimall.product.vo.AttrGroupRelationVo;
import com.tjj.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tjj.gulimall.product.entity.AttrGroupEntity;
import com.tjj.gulimall.product.service.AttrGroupService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.R;



/**
 * 属性分组
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:45:47
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 获取分类下所有分组&关联属性valueType
     * /product/attrgroup/{catelogId}/withattr
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        //1、根据catelogId获取分类下的所有分组
        //2、获取分组下的所有属性
        List<AttrGroupWithAttrsVo> attrGroupWithAttrsVos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data",attrGroupWithAttrsVos);

    }


    /**
     * 添加属性与分组关联关系
     * /product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> vos){
        attrAttrgroupRelationService.saveBatch(vos);

        return R.ok();
    }

    /**
     * 获取属性分组没有关联的其他属性
     * /product/attrgroup/{attrgroupId}/noattr/relation
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation (@PathVariable("attrgroupId") Long attrgroupId,
                             @RequestParam Map<String, Object> params){
        PageUtils page = attrService.getNoRelationAttr(attrgroupId,params);
        System.out.println(page.getList());
        return R.ok().put("data",page);
    }

    /**
     * 删除属性与分组的关联关系
     * /product/attrgroup/attr/relation/delete
     */
    @PostMapping("/attr/relation/delete")
    public R deleteRelation (@RequestBody AttrGroupRelationVo[] attrGroupRelationVos){
        attrAttrgroupRelationService.deleteRelation(attrGroupRelationVos);
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId){
//        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);

        return R.ok().put("page", page);
    }

    ///product/attrgroup/{attrgroupId}/attr/relation
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId){
        List<AttrEntity> attrEntityList = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data",attrEntityList);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

		Long cateLogId = attrGroup.getCatelogId();

        Long[] categoryPath = categoryService.findCateLogPath(cateLogId);

        attrGroup.setCatelogPath(categoryPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);



        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
