package com.atguigu.gulimall.product.controller;

import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName IndexController
 * @Description
 * @Author wangzuzhen
 * @Create 2020-07-12 16:14
 * @Version 1.0
 */
@RestController
@RequestMapping("/product")
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @GetMapping("/helloRediss")
    public String helloRediss() {
        //1.获取一把锁, 只要锁名字一样, 就是同一把锁
        RLock lock = redisson.getLock("myLock");
        //2.加锁
        lock.lock(); //阻塞式等待
        try {
            System.out.println("加锁成功, 执行业务..." + Thread.currentThread().getId());
            try { TimeUnit.SECONDS.sleep(30); } catch (InterruptedException e) { e.printStackTrace(); }
        } catch (Exception e) {

        } finally {
            //3.解锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }

        return "helloRediss";
    }


}
