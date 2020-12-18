package vip.wangjc.mongo.aop;

import org.reflections.Reflections;
import vip.wangjc.mongo.annotation.MongoDB;
import vip.wangjc.mongo.annotation.MongoTable;
import vip.wangjc.mongo.builder.service.IMongoDSBuilderService;
import vip.wangjc.mongo.factory.MongoClassFieldPoolFactory;
import vip.wangjc.mongo.factory.MongoCollectionPoolFactory;

import java.util.Set;

/**
 * 获取注解声明下的所有类
 * @author wangjc
 * @title: MongoAnnotationAdvisor
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 17:58
 */
public class MongoAnnotationAdvisor {

    private IMongoDSBuilderService mongoDSBuilderService;

    public MongoAnnotationAdvisor(IMongoDSBuilderService mongoDSBuilderService){
        this.mongoDSBuilderService = mongoDSBuilderService;
    }

    /**
     * 初始化
     */
    public void initAdvisor(){

        /** MongoDS的初始化 */
        this.mongoDSBuilderService.initMongoDS();

        /** 全路径扫描 */
        Reflections reflections = new Reflections("");

        Set<Class<?>> mongoSet = reflections.getTypesAnnotatedWith(MongoDB.class);
        for(Class<?> clazz:mongoSet){
            MongoDB mongoDB = clazz.getAnnotation(MongoDB.class);
            MongoTable mongoTable = clazz.getAnnotation(MongoTable.class);

            /** 设置类的字段数组到缓存池 */
            MongoClassFieldPoolFactory.setFieldsCache(clazz);

            if(mongoDB == null || mongoTable == null){
                continue;
            }
            /** 设置连接到缓存池 */
            MongoCollectionPoolFactory.setCollection(this.mongoDSBuilderService.getMongoDS(), mongoDB.value(), mongoTable.value());
        }


    }
}
