package com.tjj.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.ware.dao.PurchaseDetailDao;
import com.tjj.gulimall.ware.entity.PurchaseDetailEntity;
import com.tjj.gulimall.ware.service.PurchaseDetailService;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseDetailEntity> wrapper = new LambdaQueryWrapper<>();
        //key: '华为',//检索关键字
        //   status: 0,//状态
        //   wareId: 1,//仓库id
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(data ->{
                data.eq(PurchaseDetailEntity::getPurchaseId,key).or().eq(PurchaseDetailEntity::getSkuId,key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)){
            wrapper.eq(PurchaseDetailEntity::getStatus,status);
        }

        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)){
            wrapper.eq(PurchaseDetailEntity::getWareId,wareId);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> findListPurchaseDetail(Long id) {
        PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
        purchaseDetailEntity.setPurchaseId(id);
        List<PurchaseDetailEntity> detailEntities = this.baseMapper.selectList(new LambdaQueryWrapper<PurchaseDetailEntity>(purchaseDetailEntity));
        return detailEntities;
    }

}