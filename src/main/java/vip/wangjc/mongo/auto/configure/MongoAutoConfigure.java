package vip.wangjc.mongo.auto.configure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vip.wangjc.mongo.aop.MongoAnnotationAdvisor;
import vip.wangjc.mongo.builder.service.IMongoDSBuilderService;
import vip.wangjc.mongo.builder.service.impl.DefaultMongoDSBuilderServiceImpl;

/**
 * @author wangjc
 * @title: MongoAutoConfigure
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 17:38
 */
@Configuration
public class MongoAutoConfigure {

    @Bean(value = "mongoDSService")
    @ConditionalOnMissingBean
    public IMongoDSBuilderService mongoDSService(){
        return new DefaultMongoDSBuilderServiceImpl();
    }

    @Bean(value = "mongoAnnotationAdvisor")
    @ConditionalOnMissingBean
    public MongoAnnotationAdvisor mongoAnnotationAdvisor(IMongoDSBuilderService mongoDSBuilderService){
        MongoAnnotationAdvisor advisor = new MongoAnnotationAdvisor(mongoDSBuilderService);

        /** 完成初始化 */
        advisor.initAdvisor();
        return advisor;
    }

}
