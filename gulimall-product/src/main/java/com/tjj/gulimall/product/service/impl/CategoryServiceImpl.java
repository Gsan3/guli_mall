package com.tjj.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tjj.gulimall.product.entity.CategoryBrandRelationEntity;
import com.tjj.gulimall.product.service.CategoryBrandRelationService;
import com.tjj.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjj.gulimall.common.utils.PageUtils;
import com.tjj.gulimall.common.utils.Query;

import com.tjj.gulimall.product.dao.CategoryDao;
import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //查询出所有的数据
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //先查询出所有的一级分类
        List<CategoryEntity> Level1Menu = categoryEntities.stream().filter(entities -> {
            return entities.getParentCid() == 0;
        }).map((menu) ->{
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).sorted((menu1,menu2) ->{
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());
        return Level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO
        //删除之前检查菜单是否被引用

        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCateLogPath(Long cataLogId) {

        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(cataLogId, paths);

        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 更新了三级分类数据
     * 启用失效模式：
     * @CacheEvict()
     * 1、同时进行多种缓存操作
     * @Caching（）
     * 2、指定删除讴歌分区下的所有数据
     * @CacheEvict(value = "category",allEntries = true)
     * 3、储存同一类型下的数据，都可以指定在同一分区。分区名默认缓存前缀
     * 4、@CachePut()双写模式
     * @param category
     *
     *
     * Spring Cache的不足
     *  1、读模式
     *      缓存穿透：查询一个null数据。解决，缓存空数据：spring.cache.redis.cache-null-values=true
     *      缓存穿击：大量并发进来同时查询一个正好过期的数据。解决：加锁，默认无加锁；sync = true（加锁，解决穿击）
     *      缓存雪崩：大量请求的key同时过期，加随机时间。加上过期时间：spring.cache.redis.time-to-live=360000
     *  2、写模式：（缓存与数据库一致性问题）
     *      1、读写加锁
     *      2、引入canal，感知mysql的更新去更新数据库
     *      3、读多写多，直接请求数据库查询
     */

    /*SpEL：字符串加单引号，否则默认动态取值*/
    @Caching(evict = {
            @CacheEvict(value = "category",key = "'findCategoryOneLevel'"),
            @CacheEvict(value = "category",key = "'getCatalogJson'")
    })
    @Transactional
    @Override
    public void updateDetail(CategoryEntity category) {
        this.updateById(category);

        categoryBrandRelationService.updateCascade(category.getCatId(), category.getName());

        //TODO

    }

    @Cacheable(value = "category",key = "#root.method.name")//指定放到哪个缓存分区中
    @Override
    public List<CategoryEntity> findCategoryOneLevel() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getCatLevel, 1));
        return categoryEntities;
    }

    //TODO 产生对外内存溢出
    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        //1、空结果缓存，解决缓存穿透
        //2、设置过期时间（加随机值），解决缓存雪崩
        //3、加锁，解决缓存击穿
        //只有一台服务器的话就用synchronized加锁（锁数据库），分布式时必须要用分布式锁

        //1、先从缓存中查询是否有数据
        String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
        //判断缓存中获取的数据是否为空
        if (StringUtils.isEmpty(catalogJson)){
            //缓存中没有获取到数据，则从数据库中获取
            Map<String, List<Catalog2Vo>> catalogJsonFromDb = this.getStringListMapFromDb();
            //将数据转换成json，再储存到缓存中
            String jsonString = JSON.toJSONString(catalogJsonFromDb);
            //设置一天过期
            redisTemplate.opsForValue().set("catalogJSON",jsonString,1, TimeUnit.DAYS);
            return catalogJsonFromDb;
        }
        //将Json转换成返回类型
        Map<String, List<Catalog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
        });
        return stringListMap;
    }
    /**
     * 分布式锁方法
     * 缓存数据如何保持与数据库一致
     * 缓存数据一致性问题
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        //锁的名字，锁的粒度越细越快
        RLock lock = redisson.getLock("getCatalogJson-lock");
        //加锁
        lock.lock();
        System.out.println("分布式锁获取成功。。。");
        //缺点在这块空隙时间可能网络断了，变死锁
        //加锁成功
        //设置过期时间
        //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
        //不管任务执行是否成功，都要释放锁
        Map<String, List<Catalog2Vo>> listMapFromDb;
        try {
            listMapFromDb = getStringListMapFromDb();
        } finally {
            //释放锁
            lock.unlock();
        }

        //解锁，删除缓存中的key,有缺陷
        //判断是否是自己的锁
            /*String lock1 = redisTemplate.opsForValue().get("lock");
            if (value.equals(lock1)) {
                redisTemplate.delete("lock");
            }*/
        return listMapFromDb;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //占用分布式锁，去redis占坑
        UUID uuid = UUID.randomUUID();
        String value = "111"+uuid;
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", value,300,TimeUnit.SECONDS);//这样就是占锁和设置过期时间原子性处理
        if(lock){
            System.out.println("分布式锁获取成功。。。");
            //缺点在这块空隙时间可能网络断了，变死锁
            //加锁成功
            //设置过期时间
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            //不管任务执行是否成功，都要释放锁
            Map<String, List<Catalog2Vo>> listMapFromDb;
            try{
                listMapFromDb = getStringListMapFromDb();
            }finally {
                // lua脚本，用来释放分布式锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                redisTemplate.execute(new DefaultRedisScript<Long>(script,Long.class),Arrays.asList("lock"),value);
            }

            //解锁，删除缓存中的key,有缺陷
            //判断是否是自己的锁
            /*String lock1 = redisTemplate.opsForValue().get("lock");
            if (value.equals(lock1)) {
                redisTemplate.delete("lock");
            }*/
            return listMapFromDb;
        }else {
            //休眠300ms
            System.out.println("分布式锁获取失败。。。重试中。。。");
            try{
                Thread.sleep(300);
            }catch (Exception e){

            }
            return this.getCatalogJsonFromDbWithRedisLock();
        }

    }

    //抽取的从数据库获取数据方法
    private Map<String, List<Catalog2Vo>> getStringListMapFromDb() {
        //拿到锁再去查一次缓存中有没有数据
        String catalogJson = redisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJson)) {
            //将Json转换成返回类型
            Map<String, List<Catalog2Vo>> stringListMap = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>() {
            });
            return stringListMap;
        }
        System.out.println("查询了数据库。。。。。。");

        //1.查询所有
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有1级分类
        List<CategoryEntity> categoryOneLevel = getCategoryEntities(selectList, 0L);

        //2、封装数据
        Map<String, List<Catalog2Vo>> collect1 = categoryOneLevel.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //查出该1级分类下的所有2级分类
            List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
            List<Catalog2Vo> catalogEntity = null;
            if (categoryEntities != null) {
                List<Catalog2Vo> catalog2 = categoryEntities.stream().map(l2 -> {
                    Catalog2Vo catalog2Vo = new Catalog2Vo();
                    catalog2Vo.setCatalog1Id(l2.getParentCid().toString());
                    catalog2Vo.setId(l2.getCatId().toString());
                    catalog2Vo.setName(l2.getName());
                    //查出该2级分类下所有的3级分类
                    List<CategoryEntity> catalog3 = getCategoryEntities(selectList, l2.getCatId());
                    if (catalog3 != null) {
                        List<Catalog2Vo.Catalog3Vo> collect = catalog3.stream().map(l3 -> {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                            catalog3Vo.setCatalog2Id(l3.getParentCid().toString());
                            catalog3Vo.setId(l3.getCatId().toString());
                            catalog3Vo.setName(l3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                        catalog2Vo.setCatalog3List(collect);
                    }
                    return catalog2Vo;
                }).collect(Collectors.toList());
                catalogEntity = catalog2;
            }

            return catalogEntity;
        }));

        //将数据转换成json，再储存到缓存中
        String jsonString = JSON.toJSONString(collect1);
        //设置一天过期
        redisTemplate.opsForValue().set("catalogJSON", jsonString, 1, TimeUnit.DAYS);
        return collect1;
    }


    /**
     * 本地锁方法
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        /**
         * 优化业务：将对数据库的多次查询变为1次
         *
         */
        synchronized (this) {
            //拿到锁再去查一次缓存中有没有数据
            return getStringListMapFromDb();
        }
    }

    //抽取的方法，多次用到
    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList,Long parentId) {
        //筛选出指定parentId的数据
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentId).collect(Collectors.toList());
        return collect;
        /*return this.baseMapper.selectList(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getParentCid, v.getCatId()));*/
    }

    private List<Long> findParentPath(Long cataLogId, List<Long> paths) {
        CategoryEntity byId = this.getById(cataLogId);
        paths.add(cataLogId);

        if (byId.getParentCid() != 0){
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //递归查询出所有的二级和三级分类
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntities -> {
            return categoryEntities.getParentCid() == root.getCatId();
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

}