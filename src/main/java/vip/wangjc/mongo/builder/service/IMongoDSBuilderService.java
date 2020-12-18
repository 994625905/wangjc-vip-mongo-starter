package vip.wangjc.mongo.builder.service;

import cn.hutool.db.nosql.mongo.MongoDS;

/**
 * 获取MongoDB连接的service
 * @author wangjc
 * @title: IMongoDSService
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 16:39
 */
public interface IMongoDSBuilderService {

    /**
     * 初始化MongoDS
     */
    void initMongoDS();

    /**
     * 获取MongoDB连接
     * @return
     */
    MongoDS getMongoDS();

    /**
     * 关闭连接
     */
    void closeMongoDS();
}
