package com.tjj.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tjj.gulimall.common.constant.ProductConstant;
import com.tjj.gulimall.common.to.SkuHasStockVo;
import com.tjj.gulimall.common.to.SkuReductionTo;
import com.tjj.gulimall.common.to.SpuBoundsTo;
import com.tjj.gulimall.common.to.es.SkuEsModel;
import com.tjj.gulimall.common.utils.R;
import com.tjj.gulimall.product.entity.*;
import com.tjj.gulimall.product.feign.CouponFeignService;
import com.tjj.gulimall.product.feign.SearchFeignService;
import com.tjj.gulimall.product.feign.WareFeignService;
import com.tjj.gulimall.product.service.*;
import com.tjj.gulimall.product.vo.*;
import net.sf.jsqlparser.statement.select.Join;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    //TODO 高级部分完善
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
        R r1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (r1.getCode() != 0){
            log.error("远程保存积分信息异常！");
        }

        //6、保存当前spu对应的所有sku信息
        //6.1）、sku的基本信息：pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() >0) {
            for (Skus sku : skus) {//查找出SkuDefaultImg
                String skuDefaultImg = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
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
                    BeanUtils.copyProperties(image, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                    return skuImagesEntity;
                }).filter(entity ->{
                    //返回true就是需要，false就是剔除
                   return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatchSkuImages(skuImagesEntities);

                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(attr1 -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr1, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(spuInfoEntity.getId());
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatchSkuSaleAttrValue(skuSaleAttrValueEntities);


                //6.4）、sku的优惠、满减等信息：gulimall_sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                if (skuReductionTo.getFullCount() >0 && skuReductionTo.getFullPrice().compareTo(new BigDecimal("0") ) == 1) {
                    R r = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r.getCode() != 0) {
                        log.error("远程保存优惠信息异常！");
                    }
                }
            }
        }

    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByParams(Map<String, Object> params) {
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<>();

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)){
            wrapper.and(data ->{
                data.eq(SpuInfoEntity::getId,key).or().like(SpuInfoEntity::getSpuName,key);
            });
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status) && !"0".equalsIgnoreCase(status)){
            wrapper.eq(SpuInfoEntity::getPublishStatus,status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq(SpuInfoEntity::getBrandId,brandId);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        //1、查出当前spuId对应的所有sku信息
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //TODO 4、查询当前sku可以被检索的所有规格属性信息
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(entity -> {
            return entity.getAttrId();
        }).collect(Collectors.toList());

        //过滤attrIds，筛选所有可以检索的attrsIds
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);

        //去重
        Set<Long> idSet = new HashSet<>(searchAttrIds);

        //提取Attrs
        List<SkuEsModel.Attrs> collect = productAttrValueEntities.stream().filter(data -> {
            return idSet.contains(data.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        //TODO 1、发送远程调用，库存系统查询是否有库存
        //如果远程调用失败，默认有库存
        Map<Long, Boolean> stockMap = null;

        try{
            R r = wareFeignService.getSkusHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId,item->item.getHasStock()));
        }catch (Exception e){
            log.error("库存服务查询异常：原因{}",e);
        }


        Map<Long, Boolean> finalstockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(data -> {
            //组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();

            //skuPrice、skuImg
            BeanUtils.copyProperties(data, skuEsModel);
            skuEsModel.setSkuPrice(data.getPrice());
            skuEsModel.setSkuImg(data.getSkuDefaultImg());

            //hasStock、hotScore
            //设置库存信息
            if (finalstockMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalstockMap.get(data.getSkuId()));
            }

            //TODO 2、热度评分，0，
            skuEsModel.setHotScore(0L);

            //TODO 3、品牌的图片、名字、分类名字信息
            BrandEntity brand = brandService.getById(data.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());

            CategoryEntity category = categoryService.getById(data.getCatalogId());
            skuEsModel.setCatelogName(category.getName());

            //设置检索属性
            skuEsModel.setAttrs(collect);

            return skuEsModel;
        }).collect(Collectors.toList());

        //TODO 5、将数据发送给es进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //远程调用成功
            //TODO 6、修改当前spu的状态为上架
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //远程调用失败
            //TODO  7、重复调用问题，接口幂等性，重试机制
            //feign调用流程
            /**
             * 1、构造请求数据，将对象转为json
             * RequestTemplate template = buildTemplateFromArgs.create(argv);
             * 2、发送请求，进行执行(执行成功会解码相应数据)
             * executeAndDecode(template)
             * 3、执行请求会有重试机制
             * while(true){
             *      try{
             *          executeAndDecode(template
             *      }catch{
             *          try{
             *              retryer.continueOrPropagate(e);
             *          }catch{
             *              throw ex;
             *          }
             *          continue;
             *      }
             *  }
             */
        }
    }

}