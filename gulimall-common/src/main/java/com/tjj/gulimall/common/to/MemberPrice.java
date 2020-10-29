package com.tjj.gulimall.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/10/28
 * Description:
 */
@Data
public class MemberPrice {

    private Long id;
    private String name;
    private BigDecimal price;
}