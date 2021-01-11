package vip.wangjc.mongo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MongoDB的database名称注解
 * @author wangjc
 * @title: MongoDB
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 14:54
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
    public @interface MongoDB {

    /**
     * 名称：不可为null
     * @return
     */
    String value() ;

}
