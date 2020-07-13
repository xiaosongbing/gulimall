package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.entity.SpuInfoDescEntity;
import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.atguigu.gulimall.product.service.SpuInfoDescService;
import com.atguigu.gulimall.product.service.SpuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
class GulimallProductApplicationTests {

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void testRedissClient() {
        System.out.println(redissonClient);
    }
    @Test
    void contextLoads() {
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
//        spuInfoDescEntity.setSpuId(11L);
//        spuInfoDescEntity.setDecript("test说明");
//        spuInfoDescService.save(spuInfoDescEntity);

//        QueryWrapper<SpuInfoDescEntity> wrapper = new QueryWrapper<>();
//        wrapper.eq("spu_id", 11);
//        List<SpuInfoDescEntity> list = spuInfoDescService.list(wrapper);
//        list.forEach(System.out::println);

    }

}
