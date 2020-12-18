package vip.wangjc.mongo.base.service;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import vip.wangjc.mongo.base.entity.MongoBaseEntity;
import vip.wangjc.mongo.exception.MongoException;

import java.util.List;

/**
 * 封装base service
 * @author wangjc
 * @title: IMongoBaseService
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 18:27
 */
public interface IMongoBaseService<T extends MongoBaseEntity> {

    /**
     * 获取MongoDB的连接
     * @return
     */
    MongoCollection<Document> getCollection();

    /**
     * 根据mongoID查询
     * @param mongoId
     * @return
     */
    T selectByMongoId(String mongoId);

    /**
     * 根据MongoID查询
     * @param mongoId
     * @return
     */
    T selectByMongoId(ObjectId mongoId);

    /**
     * 根据实体对象构造的字段值，获取单条
     * @param entity
     * @return
     */
    T selectOne(T entity);

    /**
     * 根据实体对象构造的字段值，获取列表
     * @param entity：为null差获取全部，不推荐
     * @return
     * @throws MongoException
     */
    List<T> selectList(T entity);

    /**
     * 根据实体对象构造的字段值，获取列表，并排序
     * @param entity：为null差获取全部，不推荐
     * @param field：排序字段
     * @param order：排序规则，1升序（默认），-1降序
     * @return
     * @throws MongoException
     */
    List<T> selectList(T entity, String field, Integer order);


    /**
     * 查询所有
     * @return
     * @throws MongoException
     */
    List<T> selectAll();

    /**
     * 查询所有并排序
     * @param field：排序字段
     * @param order：排序规则，1升序（默认），-1降序
     * @return
     * @throws MongoException
     */
    List<T> selectAll(String field, Integer order);

    /**
     * 插入单条记录
     * @param entity
     * @return
     */
    String insertOne(T entity);

    /**
     * 根据mongoID更新
     * @param entity
     * @return
     */
    Boolean updateOne(T entity);

    /**
     * 批量更新
     * @param query：查询体
     * @param update：更新体
     * @return
     */
    Long updateBatch(T query, T update);

    /**
     * 根据mongoID删除
     * @param mongoId
     * @return
     * @throws MongoException
     */
    Boolean deleteOne(ObjectId mongoId);

    /**
     * 根据mongoID删除
     * @param mongoId
     * @return
     * @throws MongoException
     */
    Boolean deleteOne(String mongoId);

    /**
     * 批量删除
     * @param entity：删除体
     * @return
     * @throws MongoException
     */
    Long deleteByEntity(T entity);
}
