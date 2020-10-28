package com.tjj.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.ware.entity.WareInfoEntity;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:55:56
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

