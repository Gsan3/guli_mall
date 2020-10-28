package com.tjj.gulimall.member.dao;

import com.tjj.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:51:12
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
