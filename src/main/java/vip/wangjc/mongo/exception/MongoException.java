package vip.wangjc.mongo.exception;

/**
 * 异常类
 * @author wangjc
 * @title: MongoException
 * @projectName wangjc-vip-mongo-starter
 * @date 2020/12/17 - 16:36
 */
public class MongoException extends Exception{

    public MongoException(){
        super();
    }

    public MongoException(String message){
        super(message);
    }
}
