package com.tjj.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:53:40
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

