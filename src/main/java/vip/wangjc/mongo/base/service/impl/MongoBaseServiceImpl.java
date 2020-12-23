package vip.wangjc.mongo.base.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.Block;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.wangjc.mongo.annotation.MongoDB;
import vip.wangjc.mongo.annotation.MongoTable;
import vip.wangjc.mongo.base.entity.MongoBaseEntity;
import vip.wangjc.mongo.base.service.IMongoBaseService;
import vip.wangjc.mongo.pool.MongoCollectionPool;
import vip.wangjc.mongo.pool.MongoEntityClassFieldPool;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjc
 * @title: MongoBaseServiceImpl
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/18 - 9:28
 */
public class MongoBaseServiceImpl<T extends MongoBaseEntity> implements IMongoBaseService<T> {

    private static final Logger logger = LoggerFactory.getLogger(MongoBaseServiceImpl.class);
    private static final String FIELD_MONGOID = "mongoId";
    private static final String FIELD_SERIAL_UID = "serialVersionUID";

    /**
     * 当前集合的连接
     */
    private MongoCollection<Document> collection;

    /**
     * 当前泛型的class
     */
    private Class<T> entityClass;

    @Override
    public MongoCollection<Document> getCollection() {
        if(collection == null){

            synchronized (this){
                MongoDB mongoDB = this.getEntityClass().getAnnotation(MongoDB.class);
                MongoTable mongoTable = this.getEntityClass().getAnnotation(MongoTable.class);
                this.collection = MongoCollectionPool.getCollection(mongoDB.value(),mongoTable.value());
            }
        }
        return this.collection;
    }

    @Override
    public T selectByMongoId(String mongoId) {
        Document first = this.getCollection().find(this.getMongoIdQuery(mongoId)).first();
        if(first != null){
            logger.debug("==============================selectByMongoId get query mongoId is:[{}]",mongoId);
            return this.getEntityByDocument(first);
        }
        return null;
    }

    @Override
    public T selectByMongoId(ObjectId mongoId) {
        return this.selectByMongoId(mongoId.toHexString());
    }

    @Override
    public T selectOne(T entity) {
        List<T> list = this.selectList(entity);
        if(list != null && list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<T> selectList(T entity) {
        return this.selectList(entity,null,null);
    }

    @Override
    public List<T> selectList(T entity, String field, Integer order) {
        try {
            Document query = getDocumentByEntity(entity);
            logger.debug("==============================selectList get query document is:[{}]",query.toJson());
            List<T> res = new ArrayList<>();

            Block<Document> processBlock = new Block<Document>() {
                public void apply(final Document document) {
                    res.add(getEntityByDocument(document));
                }
            };

            if(field == null){
                this.getCollection().find(query).forEach(processBlock);
            }else{
                Document sort = new Document();
                sort.append(field,order == null?1:order);
                this.getCollection().find(query).sort(sort).forEach(processBlock);
            }
            return res;
        }catch (Exception e){
            logger.error("selectList error,entity:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<T> selectAll() {
        return this.selectAll(null,null);
    }

    @Override
    public List<T> selectAll(String field, Integer order) {
        return this.selectList(null,field,order);
    }

    @Override
    public String insertOne(T entity) {
        if(entity != null){
            Document document = Document.parse(JSON.toJSONString(entity));//转为json格式
            document.remove(FIELD_MONGOID);
            document.put(FIELD_MONGOID,new ObjectId());
            this.getCollection().insertOne(document);

            logger.debug("==============================insertOne get insert document is:[{}]",document.toJson());
            return document.get(FIELD_MONGOID).toString();
        }
        return null;
    }

    @Override
    public Boolean updateOne(T entity) {
        try {
            Document document = this.getUpdateSet(entity);
            logger.debug("==============================updateOne get update set document is :[{}]",document.toJson());
            logger.debug("==============================mongoId is :[{}]",entity.getMongoId().toHexString());

            UpdateResult result = this.getCollection().updateMany(getMongoIdQuery(entity.getMongoId()), document);
            if(result.getModifiedCount() > 0){
                return true;
            }
            return false;
        }catch (Exception e){
            logger.error("updateOne error,entity:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Long updateBatch(T query, T update) {
        try {
            Document queryDocument = this.getDocumentByEntity(query);
            logger.debug("==============================updateBatch get query set document is :[{}]",queryDocument.toJson());

            Document updateDocument = this.getUpdateSet(update);
            logger.debug("==============================updateBatch get update set document is :[{}]",updateDocument.toJson());

            UpdateResult result = this.getCollection().updateMany(queryDocument, updateDocument);
            logger.debug("==============================updateBatch ModifiedCount :[{}]",result.getModifiedCount());

            return result.getModifiedCount();
        }catch (Exception e){
            logger.error("updateOne error,entity:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public Boolean deleteOne(ObjectId mongoId) {
        return this.deleteOne(mongoId.toHexString());
    }

    @Override
    public Boolean deleteOne(String mongoId) {
        DeleteResult result = this.getCollection().deleteOne(this.getMongoIdQuery(mongoId));
        logger.debug("==============================deleteOne mongoId:[{}],count:[{}]",mongoId,result.getDeletedCount());
        return result.getDeletedCount() > 0;
    }

    @Override
    public Long deleteByEntity(T entity) {
        if(entity == null){
            logger.error("deleteByEntity error: update entity is null");
            return 0L;
        }
        try {
            Document deleteDocument = this.getDocumentByEntity(entity);
            logger.debug("==============================deleteByEntity get delete document is :[{}]",deleteDocument.toJson());

            DeleteResult result = getCollection().deleteMany(deleteDocument);
            logger.debug("==============================deleteByEntity count :[{}]",result.getDeletedCount());

            return result.getDeletedCount();
        }catch (Exception e){
            logger.error("deleteByEntity error,entity:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    private Class<T> getEntityClass(){
        if(this.entityClass == null){

            synchronized (this){
                ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
                this.entityClass = (Class<T>) type.getActualTypeArguments()[0];
            }
        }
        return this.entityClass;

    }

    /**
     * 通过mongoID获取document
     * @param mongoId
     * @return
     */
    private Document getMongoIdQuery(String mongoId){
        return this.getMongoIdQuery(new ObjectId(mongoId));
    }

    /**
     * 通过mongoID获取document
     * @param mongoId
     * @return
     */
    private Document getMongoIdQuery(ObjectId mongoId){
        Document query = new Document();
        query.append(FIELD_MONGOID,mongoId);
        return query;
    }

    /**
     * 通过实体获取更新后的document
     * @param entity
     * @return
     */
    private Document getUpdateSet(T entity){
        Document set = this.getDocumentByEntity(entity);
        set.remove(FIELD_MONGOID);
        return new Document("$set",set);
    }

    /**
     * 根据实体获取document
     * @param entity
     * @return
     */
    private Document getDocumentByEntity(T entity) {
        try {
            Document query = new Document();
            if(entity == null){
                return query;
            }
            Field[] fields = MongoEntityClassFieldPool.getFields(this.getEntityClass());
            for(Field f:fields){

                /** 过滤掉序列号ID */
                if(FIELD_SERIAL_UID.equals(f.getName())){
                    continue;
                }
                f.setAccessible(true);//设置可见性
                Object value = f.get(entity);

                /** 过滤掉空值字段 */
                if(value == null){
                    continue;
                }
                query.append(f.getName(),value);
            }
            return query;
        }catch (IllegalAccessException e){
            logger.error("getDocumentByEntity error entity:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据Document获取实体
     * @param document
     * @return
     */
    private T getEntityByDocument(Document document) {
        try {
            T t = this.getEntityClass().newInstance();
            Field[] fields = MongoEntityClassFieldPool.getFields(this.getEntityClass());
            for(Field f:fields){

                if(document.get(f.getName()) == null){
                    continue;
                }
                Object value = document.get(f.getName());
                if(value == null){
                    continue;
                }
                if(FIELD_MONGOID.equals(f.getName())){
                    value = new ObjectId(String.valueOf(value));
                }
                this.setFieldValue(t,f,value);
            }
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error("getEntityByDocument error,class:[{}]",this.getEntityClass().getName());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * get实体对应字段设置值
     * @param t
     * @param field
     * @param value
     */
    private void setFieldValue(T t,Field field,Object value){
        try {
            field.setAccessible(true);//设置可见性
            field.set(t,value);
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccess异常,entity:[{}],field:[{}],value:[{}]",t.getClass(),field.getName(),value);
            e.printStackTrace();
        }
    }
}
