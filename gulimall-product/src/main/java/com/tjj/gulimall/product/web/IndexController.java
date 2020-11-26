package com.tjj.gulimall.product.web;

import com.tjj.gulimall.product.entity.CategoryEntity;
import com.tjj.gulimall.product.service.CategoryService;
import com.tjj.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Tjj
 * @Date: 2020/11/10
 * Description:
 */
@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){

        //查询一级分类
        List<CategoryEntity> categoryEntities = categoryService.findCategoryOneLevel();

        //将查询结果传给前端页面
        model.addAttribute("categories",categoryEntities);

        //视图解析器拼串
        //classpath:/templates/+返回结果+.html
        return "index";
    }


    //index/catalog.json
    @GetMapping("/index/catalog.json")
    @ResponseBody
    public Map<String,List<Catalog2Vo>> getCatalogJson(){

        Map<String,List<Catalog2Vo>> map = categoryService.getCatalogJson();

        return map;
    }

    /**
     * 测试Redisson分布式可重入锁
     * @return
     */
    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        //获取一把锁，只要锁名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");
        //加锁

        lock.lock(10, TimeUnit.SECONDS);//十秒自动解锁，自动解锁时间一定大于业务执行时间，不会自动续期
        //如果我们传递了锁的超时时间，就会发给redis执行脚本，进行占锁，默认超过时间就是指定时间
        //如果未指定超时时间时，就使用看门狗的默认时间30 *1000

        //只要占锁成功就会启动一个定时任务--》重新给锁设置过期时间，新的过期时间就是看门狗的默认时间，每隔10s就会自动续期

        //lock.lock();//阻塞式等待
        //1）、所自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2）、加锁的业务只要运行完成，就不会给当前锁自动续期，几时不手动释放锁，锁默认30s以后自动删除
        try {
            System.out.println("加锁成功。。。执行业务。。。"+Thread.currentThread().getId());
            Thread.sleep(30000);
        }catch (Exception e){

        }finally {
            //解锁
            //假如这个时候加锁的线程网络断掉了，不会发生死锁。Redisson有开门狗
            lock.unlock();
            System.out.println("释放锁。。。"+Thread.currentThread().getId());
        }

        return "hello";
    }


    /**
     * 测试Redisson分布式可读写锁
     * @return
     */
    @GetMapping("/write")
    @ResponseBody
    public String write(){
        String s = "";
        //加写锁，再执行业务
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        try {
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("lock",s);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            //释放锁
            rLock.unlock();
        }

        return s;
    }

    /**
     * 测试Redisson分布式可读写锁
     * @return
     */
    @GetMapping("/read")
    @ResponseBody
    public String read(){
        String s = "";
        //加写锁，再执行业务
        RReadWriteLock readWriteLock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.readLock();
        try {
            s = redisTemplate.opsForValue().get("lock");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            //释放锁
            rLock.unlock();
        }

        return s;
    }

    /**
     * 信号量测试
     * 可用作分布式限流
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore semaphore = redisson.getSemaphore("park");
        //semaphore.acquire();//申请一个信号，有则获取一个值，信号量减少一个，没有则一直等待到有
        boolean b = semaphore.tryAcquire();//申请一个信号，有则获取一个信号返回true，没有则返回false
        if (b){
            //执行业务
        }else {
            return "";
        }

        return "ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore semaphore = redisson.getSemaphore("park");
        semaphore.release();//释放一个信号

        return "ok";
    }


    /**
     * 测试分布式闭锁
     * 放假，锁门
     * 5个班全走了后锁门
     */
    @GetMapping("/door")
    @ResponseBody
    public String door() throws InterruptedException {
        RCountDownLatch lock = redisson.getCountDownLatch("door");
        lock.trySetCount(5);//5个班
        lock.await();//等待中
        return "放假了。。。锁大门。。。";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id)  {
        RCountDownLatch lock = redisson.getCountDownLatch("door");
        lock.countDown();//计数减一

        return id+"班走了！";
    }
}