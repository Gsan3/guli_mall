package com.tjj.gulimall.ware.service.impl;

import com.tjj.gulimall.common.constant.WareConstants;
import com.tjj.gulimall.ware.controller.vo.MergeVo;
import com.tjj.gulimall.ware.controller.vo.PurchaseDoneVo;
import com.tjj.gulimall.ware.controller.vo.PurchaseItemDoneVo;
import com.tjj.gulimall.ware.entity.PurchaseDetailEntity;
import com.tjj.gulimall.ware.service.PurchaseDetailService;
import com.tjj.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.ware.dao.PurchaseDao;
import com.tjj.gulimall.ware.entity.PurchaseEntity;
import com.tjj.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByUnreceivePurchase(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>().eq("status", WareConstants.PurchaseEnum.CREATED.getCode())
                        .or().eq("status", WareConstants.PurchaseEnum.ASSIGNED.getCode())
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo vo) {
        Long purchaseId = vo.getPurchaseId();
        if (vo.getPurchaseId() == null){
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setStatus(WareConstants.PurchaseEnum.CREATED.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }


        //确认采购项的状态是0,1才能合并
        List<Long> items = vo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(i -> {
            PurchaseDetailEntity byId = purchaseDetailService.getById(i);
            if (byId.getStatus() == WareConstants.PurchaseDetailEnum.CREATED.getCode() || byId.getStatus() == WareConstants.PurchaseDetailEnum.ASSIGNED.getCode() ) {
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                purchaseDetailEntity.setId(i);
                purchaseDetailEntity.setPurchaseId(finalPurchaseId);
                purchaseDetailEntity.setStatus(WareConstants.PurchaseDetailEnum.ASSIGNED.getCode());
                return purchaseDetailEntity;
            }else{
                return null;
            }
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        //采购单有改变，时间跟随改变
        PurchaseEntity entity = new PurchaseEntity();
        entity.setId(purchaseId);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        this.updateById(entity);
    }

    @Override
    public void receivedPurchase(List<Long> items) {
        //1、修改采购单的状态变为已领取
        List<PurchaseEntity> collect = items.stream().map(i -> {
            PurchaseEntity purchaseEntity = this.getById(i);
            //确认采购单状态是否是创建状态或者是已分配状态
            return purchaseEntity;
        }).filter(entity ->{
            if (entity.getStatus() == WareConstants.PurchaseEnum.CREATED.getCode() || entity.getStatus() == WareConstants.PurchaseEnum.ASSIGNED.getCode()){
                return true;
            }
            return false;
        }).map(data ->{
            data.setStatus(WareConstants.PurchaseEnum.RECEIVED.getCode());
            data.setUpdateTime(new Date());
            return data;
        }).collect(Collectors.toList());
        this.updateBatchById(collect);

        //2、修改采购详情状态变为正在采购
        collect.forEach(data->{
            List<PurchaseDetailEntity> detailEntities = purchaseDetailService.findListPurchaseDetail(data.getId());
            List<PurchaseDetailEntity> collect1 = detailEntities.stream().map(entity -> {
                entity.setStatus(WareConstants.PurchaseDetailEnum.BUYING.getCode());
                return entity;
            }).collect(Collectors.toList());

            purchaseDetailService.updateBatchById(collect1);

        });


    }

    @Transactional
    @Override
    public void done(PurchaseDoneVo doneVo) {

        Long id = doneVo.getId();


        //2、改变采购项的状态
        Boolean flag = true;
        List<PurchaseItemDoneVo> items = doneVo.getItems();

        List<PurchaseDetailEntity> updates = new ArrayList<>();
        for (PurchaseItemDoneVo item : items) {
            PurchaseDetailEntity detailEntity = new PurchaseDetailEntity();
            if(item.getStatus() == WareConstants.PurchaseDetailEnum.HASERROR.getCode()){
                flag = false;
                detailEntity.setStatus(item.getStatus());
            }else{
                detailEntity.setStatus(WareConstants.PurchaseDetailEnum.FINISH.getCode());
                ////3、将成功采购的进行入库
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                wareSkuService.addStock(entity.getSkuId(),entity.getWareId(),entity.getSkuNum());

            }
            detailEntity.setId(item.getItemId());
            updates.add(detailEntity);
        }

        purchaseDetailService.updateBatchById(updates);

        //1、改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(id);
        purchaseEntity.setStatus(flag?WareConstants.PurchaseEnum.FINISH.getCode():WareConstants.PurchaseEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);

    }

}