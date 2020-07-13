package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import sun.java2d.pipe.SpanIterator;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

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
        //1. 查出所有分类
        List<CategoryEntity> entities = this.baseMapper.selectList(null);
        //2. 组装成父子的树形结构
        //2.1 找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1、检查当前删除的菜单，是否被别的地方引用
        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        //保证冗余字段的数据一致
        this.updateById(category);
        if(StringUtils.isNotBlank(category.getName())) {
            //同步更新其他关联表中的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            //TODO 更新其他关联
        }


    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
//        1.空结果缓存:解决缓存穿透；
//        2.设置过期时间(加随机值): 解决缓存雪崩;
//        3.加锁:解决缓存击穿;


        //1.缓存数据
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if(StringUtils.isEmpty(catalogJson)) {
            //2.缓存中没有, 查询数据库
            System.out.println("缓存不命中....将要查询数据库....");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();
            //3. 查到数据, 将对象转为JSON放在缓存中(这里存在时间误差, 释放锁跟存在redis不在同一个锁操作中)
//            String jsonString = JSON.toJSONString(catalogJsonFromDb);
//            redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);
//            return catalogJsonFromDb;
        }

        System.out.println("缓存命中....直接返回");
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性
     * 1) 双写模式
     * 2) 失效模式
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {
        // 1.锁的名字。锁的粒度, 越细越快.
        //锁的粒度: 具体缓存的是某个数据, 11-商品; product-11-lock product-12-lock
        RLock lock = redisson.getLock("CatalogJson");
        lock.lock();
        Map<String, List<Catelog2Vo>> resMap = null;
        try {
            resMap = catalogJsonHandler();
        } finally {
            lock.unlock();
        }
        return resMap;

    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //分布式核心：加锁 跟 解锁 都是原子性
        //1.占分布式锁, 去redis占坑 --原子命令setnxex
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 60, TimeUnit.SECONDS);
        if(lock) {
            System.out.println("获取分布式锁成功.....");
            //加锁成功, 执行业务
            //设置过期时间
//            redisTemplate.expire("lock", 30, TimeUnit.SECONDS);// 占锁后直接宕机导致死锁
            Map<String, List<Catelog2Vo>> resMap = null;
            try {
                resMap = catalogJsonHandler();
            } finally {
                //获取值对比+对比成功删除=原子操作 lua脚本解锁
                String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then return redis.call(\"del\",KEYS[1]) else return 0 end";
                //删除锁-原子
                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock", uuid));
                /*String lockValue = redisTemplate.opsForValue().get("lock");
                if(uuid.equals(lockValue)) {
                    //删除自己的锁 ---- 若程序执行业务时间过长， redis设置过期时间过期了, 别人抢占占位锁成功后, 避免删除其他线程的锁
                    redisTemplate.delete("lock"); //删除锁
                }*/
            }
            return resMap;
        } else {
            //加锁失败, 重试
            System.out.println("获取分布式锁失败.....等待重试.....");
            try { TimeUnit.SECONDS.sleep(5); } catch (InterruptedException e) { e.printStackTrace(); }
            return getCatalogJsonFromDbWithRedisLock();
        }

    }



    private Map<String, List<Catelog2Vo>> catalogJsonHandler() {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            //缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        System.out.println("查询了数据库....");
        Map<String, List<Catelog2Vo>> parent_cid = getCatalogDataFromDB();

        //3. 查到数据, 将对象转为JSON放在缓存中
        String jsonString = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catalogJson", jsonString, 1, TimeUnit.DAYS);

        return parent_cid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {

        //只要是同一把锁, 就能锁住需要这个锁的所有线程
        //1.synchronized (this) :Springboot所有的组件在容器中都是单例的
        // TODO 本地锁: synchronized, JUC(Lock) 在分布式情况下, 必须使用分布式锁

        synchronized (this) {
            //得到锁后，我们应该在去缓存中确定一次, 如果没有才需要继续查询
            return catalogJsonHandler();
        }

    }

    private Map<String, List<Catelog2Vo>> getCatalogDataFromDB() {
        //1.将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1.查出所有1级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //2.封装数据
        return level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1.每一个的一级分类, 查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            //封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;

            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, item2.getCatId().toString(), item2.getName());
                    // 1.找到当前二级分类的三级分类装成VO
                    List<CategoryEntity> level3CatelogList = getParent_cid(selectList, item2.getCatId());
                    if (level3CatelogList != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3CatelogList.stream().map(item3 -> {
                            // 2.封装成指定格式
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item2.getCatId().toString(), item3.getCatId().toString(), item3.getName().toString());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }


                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
       // return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return collect;
    }

    //递归查找路径 225,25,2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), paths);
        }
        return paths;
    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
           return categoryEntity.getParentCid().equals(root.getCatId());
        }).map((menu) -> {
            //1.找到子菜单
            menu.setChildren(getChildren(menu, all));
            return menu;
        }).sorted((menu1, menu2) -> {
            //2、菜单的排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }


}