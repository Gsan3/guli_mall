package com.tjj.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.ware.controller.vo.MergeVo;
import com.tjj.gulimall.ware.controller.vo.PurchaseDoneVo;
import com.tjj.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author Gsan
 * @email 754323923@qq.com
 * @date 2020-08-02 02:55:56
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVo vo);

    void receivedPurchase(List<Long> items);

    void done(PurchaseDoneVo doneVo);
}

