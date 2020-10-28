package com.tjj.gulimall.member.feign;

import com.tjj.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 这是一个声明式远程调用
 */
@FeignClient(value = "gulimall-coupon", path = "/coupon/coupon")
public interface CouponFeignService {
    @RequestMapping("/member/list")
    public R memberCoupons();
}
