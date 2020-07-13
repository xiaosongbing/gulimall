package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName Catelog2Vo
 * @Description
 * @Author wangzuzhen
 * @Create 2020-07-12 16:31
 * @Version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
//2级分类vo
public class Catelog2Vo {
    private String catalog1Id; //1级父分类id
    private List<Catelog3Vo> catalog3List; //三级子分类
    private String id;
    private String name;

    //3级分类vo
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {
        private String catalog2Id; //父分类, 2级分类id
        private String id;
        private String name;
    }
}
