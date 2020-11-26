package com.tjj.gulimall.search.service;

import com.tjj.gulimall.search.vo.SearchParam;
import com.tjj.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param param 检索的所有参数
     * @return 返回检索结果
     */
    SearchResult search(SearchParam param);
}
