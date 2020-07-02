package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zzwang
 * @email zzwang@gmail.com
 * @date 2020-07-02 17:21:24
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
