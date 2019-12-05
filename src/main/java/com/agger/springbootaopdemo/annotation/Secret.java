package com.agger.springbootaopdemo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @classname: Secret
 * @description: 自定义注解，用来标识请求类 或者方法是否使用AOP加密解密
 * @author chenhx
 * @date 2019-12-05 13:48:03
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Secret {

    //是否进行加密解密
    boolean value() default true;
}
