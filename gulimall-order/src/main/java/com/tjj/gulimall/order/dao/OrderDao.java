package com.tjj.gulimall.order.dao;

import com.tjj.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:53:40
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
