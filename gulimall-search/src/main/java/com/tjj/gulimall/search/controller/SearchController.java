package com.tjj.gulimall.search.controller;

import com.tjj.gulimall.search.service.MallSearchService;
import com.tjj.gulimall.search.vo.SearchParam;
import com.tjj.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/16
 * Description:
 */
@Controller
public class SearchController {

    @Autowired
    private MallSearchService searchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定对象
     * @param param
     * @return
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request){

        //获取url后面报文体
        String queryString = request.getQueryString();
        param.set_queryString(queryString);
        //根据传递来的页面查询参数，去es中检索商品
        SearchResult result = searchService.search(param);
        model.addAttribute("result",result);

        return "list";
    }
}