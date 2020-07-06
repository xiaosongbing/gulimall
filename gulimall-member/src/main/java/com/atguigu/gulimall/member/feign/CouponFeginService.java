package com.atguigu.gulimall.member.feign;

import com.atguigu.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @version 1.0
 * @ClassName: CouponFeginService
 * @Description: 优惠券fegin调用服务
 * @Author zzwang<br />
 * @Date: 2020/7/2 17:52
 */
@FeignClient("gulimall-coupon")
public interface CouponFeginService {
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
