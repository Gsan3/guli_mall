package com.tjj.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.tjj.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 测试存储数据index
     */
    @Test
    void IndexData() throws IOException {
        //在users索引下存储数据
        IndexRequest indexRequest = new IndexRequest("users");
        //设置数据的id，不设置就会默认生成一个id
        indexRequest.id("1");
        //indexRequest.source("userName","zhangsan","age",18,"gender","男"); //第一种存储数据方式
        //第二种存储数据的方式（常用）
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("男");
        String string = JSON.toJSONString(user);
        indexRequest.source(string, XContentType.JSON);

        //执行保存操作
        IndexResponse index = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //提取响应数据
        System.out.println(index);
    }

    /**
     * 测试检索数据index
     */
    @Test
    void SearchData() throws IOException {
        //创建检索对象,并指定索引
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //创建检索条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        searchSourceBuilder.query();
//        searchSourceBuilder.aggregation();
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        System.out.println(searchSourceBuilder.toString());

        searchRequest.source(searchSourceBuilder);

        //执行操作
        SearchResponse search = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(search);
    }

    @Data
    class User{
        private String userName;
        private Integer age;
        private String gender;
        
    }

    @Test
    void contextLoads() {
        System.out.println(restHighLevelClient);
    }

}
