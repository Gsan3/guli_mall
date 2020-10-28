package com.tjj.gulimall.product.vo;

import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/10/15
 * Description:
 */

@Data
public class AttrRespVo extends AttrVo{
    /**
     * 所属分组分组名称
     */
    private String groupName;

    /**
     * 所属分类名称
     */
    private String catelogName;

    /**
     * 分组路径
     */
    private Long[] catelogPath;
}