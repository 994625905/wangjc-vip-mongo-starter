package vip.wangjc.mongo.base.entity;

import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author wangjc
 * @title: MongoBaseEntity
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 15:01
 */
public class MongoBaseEntity implements Serializable {

    private static final long serialVersionUID = -7399657281000131881L;


    /**
     * 在MongoDB中，存储于集合中的每一个文档都需要一个唯一的 _id 字段作为 primary_key。如果一个插入文档操作遗漏了``_id``
     * 字段，MongoDB驱动会自动为``_id``字段生成一个 ObjectId，
     */
    private ObjectId mongoId;

    public ObjectId getMongoId() {
        return mongoId;
    }

    public void setMongoId(ObjectId mongoId) {
        this.mongoId = mongoId;
    }
}
