package com.tjj.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.member.entity.MemberLevelEntity;

import java.util.Map;

/**
 * 会员等级
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:51:12
 */
public interface MemberLevelService extends IService<MemberLevelEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

