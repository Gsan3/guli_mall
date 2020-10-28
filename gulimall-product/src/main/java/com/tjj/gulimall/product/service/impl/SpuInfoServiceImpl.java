package com.tjj.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.tjj.gulimall.common.to.SpuBoundsTo;
import com.tjj.gulimall.product.entity.*;
import com.tjj.gulimall.product.feign.CouponFeignService;
import com.tjj.gulimall.product.service.*;
import com.tjj.gulimall.product.vo.*;
import net.sf.jsqlparser.statement.select.Join;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuInfoService saveSkuInfo;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }


    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1、保存spu的基本信息：pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo,spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        //2、保存Spu的描述图片：pms_spu_info_desc
        List<String> descript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",",descript));
        spuInfoDescService.saveSpuInfoDescript(spuInfoDescEntity);

        //3、保存spu的图片集：pms_spu_images
        List<String> spuImages = vo.getImages();
        spuImagesService.saveSpuImages(spuInfoEntity.getId(),spuImages);

        //4、保存spu的规格参数：pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(data -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(data.getAttrId());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            productAttrValueEntity.setAttrValue(data.getAttrValues());
            AttrEntity id = attrService.getById(data.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());
            productAttrValueEntity.setQuickShow(data.getShowDesc());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatchProductAttrValue(collect);

        //5、保存spu的积分信息：gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds,spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        couponFeignService.saveSpuBounds(spuBoundsTo);

        //6、保存当前spu对应的所有sku信息
        //6.1）、sku的基本信息：pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() >0) {
            skus.forEach(sku -> {
                //查找出SkuDefaultImg
                String skuDefaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1){
                        skuDefaultImg = image.getImgUrl();
                    }
                }
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(skuDefaultImg);
                saveSkuInfo.saveSkuInfo(skuInfoEntity);

                //6.2）、sku的图片信息：pms_sku_images
                List<Images> images = sku.getImages();
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(image,skuImagesEntity);
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                skuImagesService.saveBatchSkuImages(skuImagesEntities);
                
                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(attr1 -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr1,skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(spuInfoEntity.getId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatchSkuSaleAttrValue(skuSaleAttrValueEntities);
            });
        }
        
        //6.4）、sku的优惠、满减等信息：gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}