package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zzwang
 * @email zzwang@gmail.com
 * @date 2020-07-02 17:17:29
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
