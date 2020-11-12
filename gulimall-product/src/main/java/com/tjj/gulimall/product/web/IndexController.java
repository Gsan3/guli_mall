package com.tjj.gulimall.product.web;

import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.service.CategoryService;
import com.tjj.gulimall.product.vo.Catalog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/10
 * Description:
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //查询一级分类
        List<CategoryEntity> categoryEntities = categoryService.findCategoryOneLevel();

        //将查询结果传给前端页面
        model.addAttribute("categories",categoryEntities);

        //视图解析器拼串
        //classpath:/templates/+返回结果+.html
        return "index";
    }


    //index/catalog.json
    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        Map<String,List<Catalog2Vo>> map = categoryService.getCatalogJson();

        return map;
    }
}