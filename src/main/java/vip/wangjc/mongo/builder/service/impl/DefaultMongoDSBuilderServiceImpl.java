package vip.wangjc.mongo.builder.service.impl;

import cn.hutool.db.nosql.mongo.MongoDS;
import cn.hutool.db.nosql.mongo.MongoFactory;
import cn.hutool.setting.GroupedMap;
import cn.hutool.setting.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vip.wangjc.mongo.builder.service.IMongoDSBuilderService;

import java.util.*;

/**
 * @author wangjc
 * @title: DefaultMongoDSServiceImpl
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 16:45
 */
public class DefaultMongoDSBuilderServiceImpl implements IMongoDSBuilderService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMongoDSBuilderServiceImpl.class);

    private MongoDS ds = null;

    @Override
    public void initMongoDS() {
        try {
            /** 配置文件的各个配置项 */
            Setting setting = new Setting(MongoDS.MONGO_CONFIG_PATH);
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
                ds = MongoFactory.getDS(groups);
            }else{
                ds = MongoFactory.getDS("master");
            }
        }catch (Exception e){
            logger.error("MongoDB初始化数据源失败");
            e.printStackTrace();
        }
    }

    @Override
    public MongoDS getMongoDS() {
        if(this.ds != null){
            return ds;
        }
        return null;
    }

    @Override
    public void closeMongoDS() {
        if(this.ds != null){
            MongoFactory.closeAll();
        }
    }
}
