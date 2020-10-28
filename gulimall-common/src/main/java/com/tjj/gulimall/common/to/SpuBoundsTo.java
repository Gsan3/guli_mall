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
public class SpuBoundsTo {

    private Long spuId;

    private BigDecimal buyBounds;

    private BigDecimal growBounds;
}