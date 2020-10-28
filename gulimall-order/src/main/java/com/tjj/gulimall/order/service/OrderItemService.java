package com.tjj.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:53:40
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

