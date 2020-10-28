package com.tjj.gulimall.product.feign;

import com.tjj.gulimall.common.to.SpuBoundsTo;
import com.tjj.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("")
    R saveSpuBounds(SpuBoundsTo spuBoundsTo);
}
