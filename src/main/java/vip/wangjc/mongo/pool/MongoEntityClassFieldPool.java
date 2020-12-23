package vip.wangjc.mongo.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.wangjc.mongo.util.MongoUtil;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mongo类字段的缓存池，项目启动时开始初始化
 * @author wangjc
 * @title: MongoFieldPool
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 15:21
 */
public class MongoEntityClassFieldPool {

    private static final Logger logger = LoggerFactory.getLogger(MongoEntityClassFieldPool.class);
    /**
     * 线程安全的map
     */
    private static final Map<String, Field[]> FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 通过类获取字段数组
     * @param clazz
     * @return
     */
    public static Field[] getFields(Class clazz){
        if(FIELDS_CACHE.containsKey(clazz.toString())){
            return FIELDS_CACHE.get(clazz.toString());
        }
        logger.error("Fields is null, class:[{}]",clazz.toString());
        return null;
    }

    /**
     * 通过类设置字段数组
     * @param clazz
     */
    public static void setFieldsCache(Class clazz){

        /** 加锁 */
        synchronized (FIELDS_CACHE){
            Field[] fields = MongoUtil.getFieldsDirectly(true, clazz);
            FIELDS_CACHE.put(clazz.toString(), fields);
        }
    }
}
