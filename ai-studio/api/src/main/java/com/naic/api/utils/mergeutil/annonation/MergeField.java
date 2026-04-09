package com.naic.api.utils.mergeutil.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * description 标记需要跨服务查询字段
 * @author fx
 * @date 2021/9/16 下午8:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD})
public @interface MergeField {

    Class<?> value() default Object.class;

    /**
     * entity中field name
     * 默认相同名字读取
     * @return string
     */
    String alias() default "";

    /**
     * 实体中包含还是直接返回
     * @return bool
     */
    boolean isEntity() default false;

    /**
     * 当有多次调用且均为直接返回数据时,key用来区分取那次调用的结果
     * @return string
     */
    String key() default "";

}
