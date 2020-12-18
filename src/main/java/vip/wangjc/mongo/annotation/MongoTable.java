package vip.wangjc.mongo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MongoDB的集合名称注解
 * @author wangjc
 * @title: MongoTable
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 14:56
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoTable {

    /**
     * 名称：不可为null
     * @return
     */
    String value() ;

}
