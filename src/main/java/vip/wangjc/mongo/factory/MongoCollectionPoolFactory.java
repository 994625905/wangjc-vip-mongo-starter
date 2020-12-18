package vip.wangjc.mongo.factory;

import cn.hutool.db.nosql.mongo.MongoDS;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MongoDB的连接缓存池，项目启动时开始初始化，
 * @author wangjc
 * @title: MongoCollectionPool
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 16:56
 */
public class MongoCollectionPoolFactory {

    private static final Logger logger = LoggerFactory.getLogger(MongoCollectionPoolFactory.class);

    /**
     * 连接的缓存池
     */
    private static Map<String,Map<String,MongoCollection<Document>>> COLLECTION_POOL = new ConcurrentHashMap<>();

    /**
     * 获取连接
     * @param dbName
     * @param tableName
     * @return
     */
    public static MongoCollection<Document> getCollection(String dbName,String tableName){
        if(COLLECTION_POOL.containsKey(dbName)){

            if(COLLECTION_POOL.get(dbName).containsKey(tableName)){
                return COLLECTION_POOL.get(dbName).get(tableName);
            }
        }
        logger.error("MongoCollection is null, dbName:[{}],tableName:[{}]",dbName,tableName);
        return null;
    }

    /**
     * 添加连接，给COLLECTION_POOL加锁
     * @param mongoDS
     * @param dbName
     * @param tableName
     */
    public static void setCollection(MongoDS mongoDS, String dbName, String tableName){

        if(dbName != null && tableName != null){
            /** 建立连接 */
            MongoCollection<Document> collection = mongoDS.getCollection(dbName, tableName);

            synchronized (COLLECTION_POOL){
                if(COLLECTION_POOL.containsKey(dbName)){
                    COLLECTION_POOL.get(dbName).put(tableName,collection);
                }else{

                    Map<String,MongoCollection<Document>> collectionMap = new HashMap<>();
                    collectionMap.put(tableName,collection);

                    COLLECTION_POOL.put(dbName,collectionMap);
                }
                logger.info("MongoCollection has been established ,dbName:[{}],tableName:[{}]",dbName,tableName);
            }
        }
    }

}
