package com.tjj.gulimall.ware.controller.vo;

import lombok.Data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/10/28
 * Description:
 */
@Data
public class MergeVo {
    //purchaseId: 1, //整单id
    //  items:[1,2,3,4] //合并项集合
    private Long purchaseId;
    private List<Long> items;
}