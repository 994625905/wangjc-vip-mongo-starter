package vip.wangjc.mongo.register;

import cn.hutool.db.nosql.mongo.MongoDS;
import cn.hutool.db.nosql.mongo.MongoFactory;
import cn.hutool.setting.GroupedMap;
import cn.hutool.setting.Setting;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import vip.wangjc.mongo.annotation.MongoDB;
import vip.wangjc.mongo.annotation.MongoScanRegister;
import vip.wangjc.mongo.annotation.MongoTable;
import vip.wangjc.mongo.pool.MongoCollectionPool;
import vip.wangjc.mongo.pool.MongoEntityClassFieldPool;

import java.util.*;

/**
 * mongo的注册器
 * @author wangjc
 * @title: MongoScanRegister
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/19 - 14:04
 */
public class ImportMongoScanRegister implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(ImportMongoScanRegister.class);
    private MongoDS mongoDS;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(MongoScanRegister.class.getName()));

        /** 获取指定的扫描包，配置文件的路径 */
        String[] packages = attributes.getStringArray("value");

        /** 初始化mongoDB连接 */
        this.initMongoDS();

        /** 开始注册 */
        if(packages == null || packages.length == 0){
            this.registerMongo(new Reflections("")); // 全路径扫描，从根路径开始
        }else{
            for(String pack:packages){
                this.registerMongo(new Reflections(pack));
            }
        }
    }

    /**
     * 注册业务
     * @param reflections
     */
    private void registerMongo(Reflections reflections){
        Set<Class<?>> mongoSet = reflections.getTypesAnnotatedWith(MongoDB.class);
        for(Class<?> clazz:mongoSet){
            MongoDB mongoDB = clazz.getAnnotation(MongoDB.class);
            MongoTable mongoTable = clazz.getAnnotation(MongoTable.class);

            /** 设置类的字段数组到缓存池 */
            MongoEntityClassFieldPool.setFieldsCache(clazz);

            if(mongoDB == null || mongoTable == null || this.mongoDS == null){
                continue;
            }
            /** 设置连接到缓存池 */
            MongoCollectionPool.setCollection(this.mongoDS, mongoDB.value(), mongoTable.value());
        }

    }

    /**
     * 初始化mongo配置
     */
    private void initMongoDS(){
        try {
            /** 配置文件的各个配置项 */
            Setting setting = new Setting("config/mongo.setting");
            GroupedMap groupsMap = setting.getGroupedMap();
            List<String> groups = new ArrayList<String>();
            Set<Map.Entry<String, LinkedHashMap<String, String>>> set = groupsMap.entrySet();
            for(Map.Entry<String,LinkedHashMap<String,String>> entry:set){
                if(entry.getKey() == null || entry.getKey() == ""){
                    continue;
                }
                String host = entry.getValue().get("host");
                if(host == null || host == ""){
                    continue;
                }
                logger.info("MongoDB初始化数据源[{}]信息",entry.getKey());
                logger.info("MongoDB初始化数据源host[{}]信息",host);
                groups.add(entry.getKey());
            }
            if(groups.size() > 0){
                this.mongoDS = MongoFactory.getDS(groups);
            }else{
                this.mongoDS = MongoFactory.getDS("master");
            }
        }catch (Exception e){
            logger.error("MongoDB初始化数据源失败");
            e.printStackTrace();
        }
    }
}
