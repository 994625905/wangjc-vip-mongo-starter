package vip.wangjc.mongo.annotation;

import org.springframework.context.annotation.Import;
import vip.wangjc.mongo.register.ImportMongoScanRegister;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wangjc
 * @title: MongoScan
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/19 - 14:02
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(ImportMongoScanRegister.class)
public @interface MongoScanRegister {

    /**
     * 扫描的包路径
     * @return
     */
    String[] value() default {};

}
