# wangjc-vip-mongo-starter
一套MongoDB的Java快速集成方案

# 一、简介

- wangjc-vip-mongo-starter是一套专门针对mongoDB使用的Java快速集成方案。借鉴了mybatis-plus的设计理念，采用Hutool工具来进行重组封装。让开发环节不需要写复杂的连接和特定的业务代码（紧紧一些少量的配置项），通过继承提供的基类，即可完成丰富的增删改查操作。

- PS：由于mongoDB是面向文档的存储组件，大多存储一些富文本，长内容数据，于是就没有所谓的关联查询一说，所以提供的增删改查方法，都是基于当前实体的泛型，通过反射获取字段数组，然后内容填充……等一系列操作，在开发环节上比较简单。

## 1.我为什么要使用MongoDB

- 作为一个程序员小白，自己的代码洁癖还是很多的（算不上好事也不是坏事），包括但不限于注释的书写，业务分块，命名风格……还有数据库的建表建字段习惯。在日常开发过程中，数据表的文本字段习惯性的定义为varchar，长度不够就改为text，blog，继续不够再改为longtext（这里强烈建议一些status，is_delete……之类取值范围明确的状态字段用tinyint,具体细节可查看《阿里巴巴java开发规范》）。方便且简洁，无可厚非。但是有一个潜在的隐患就是，当业务表的数据量达到一定的高度，且表中还存在longtext这种字段时，那么在读写操作时长文本字段抢占了大量的IO，拖垮性能，效率将会变得很慢。

- ### 长文本字段的分离

  绝大多数业务场景中，长文本字段都是作为点击详情页信息的出现，是定向查询，一般不会出现大批量查。以博客文章为例，文章内容只能点击进去才可查阅，而首页放的文章列表（由文章图像，摘要，时间……组成）提供一个阅读的接口，所以没必要把内容字段放在一起表里来拖累无关阅读操作的功能，一般的做法是，将这种字段给分离出来单独一张表，采用id的形式来关联。

- 又有问题！假如MySQL应用做了读写分离，有集群管理的话，到也不要紧，可如果是单节点应用，所有相关的请求操作一瞬间全部到达MySQL的话，长文本的读写操作还是抢占了MySQL大量的性能，根据“木桶效应”来分析，数据库的短板成了整个系统的瓶颈上限。鸡蛋不能放在一个篮子里，应用资源要适当的均分和权重，且MySQL的定位是关系型数据的存储，并不适合太这种文本内容。所以我这里将富文本/长文本内容全部分离到MongoDB，采用MongoId来与MySQL关联，所有的图片也摒弃了臃肿的base码，采用fastDFS文件存储。

- ### MongoDB的建表规范

  简单提及一下。这里的表就是集合，为了更好的关联，以模块来分类，一个模块就是一个库，一个库下有多个集合。
  eg：笔记模块：DB：wangjc_note，下面的集合：wangjc_note_info，wangjc_note_comment…………

------------

# 二、MongoDB的安装

## 1.下载地址

- http://www.mongodb.org/downloads （我下载的版本是mongodb-linux-x86_64-4.0.11）

## 2.安装如下：

- ```java
  	//进入到指定的安装目录（我安装在/usr/local下），上传,解压，重命名
     rz mongodb-linux-x86_64-4.0.11.tgz
     tar -zxvf mongodb-linux-x86_64-4.0.11.tgz
     mv mongodb-linux-x86_64-4.0.11 mongodb
  
     //配置环境变量
     vim /etc/profile
     export PATH=/usr/local/mongodb/bin:$PATH
  
     //保存退出，重新加载
     source /etc/profile
  
     //创建MongoDB数据存放文件夹和日志记录文件夹，为后面的配置文件使用：
     mkdir -pv /usr/local/mongodb_file/data
     mkdir -pv /usr/local/mongodb_file/logs
  
     //创建配置文件,进入到MongoDB的bin目录下
     vim mongodb.conf（直接是new file）
  
     //编辑输入
     dbpath = /usr/local/mongodb_file/data #数据文件存放目录
     logpath = /usr/local/mongodb_file/logs/mongodb.log #日志文件存放目录
     port = 27017 #端口,强烈建议更改
     fork = true#以守护程序的方式启用，即在后台运行
     auth=true #需要认证。如果放开注释，就必须创建MongoDB的账号，使用账号与密码才可远程访问，第一次安装建议注释，后面放开
     bind_ip=127.0.0.1 #允许远程访问(0.0.0.0)，或者直接注释掉，127.0.0.1是只允许本地访问
  
     //启动MongoDB服务，进入bin目录下
     ./mongod  -f  mongodb.conf
  
     //连接mongo，进入bin目录下
     mongo
     //如果这里改了端口，则连接为：
     mongo localhost:port
  
     //关闭mongo，查出pid，使用kill杀死对应的pid
     ps -aux|grep mongo
     kill -9 pid
  ```

  

## 3.用户密码权限的配置

- 一定要重视网络安全管理，推荐改端口，添加用户密码，权限分配，设置ip白名单

- ```java
   //连接mongo
    mongo localhost:****//如果没改端口，直接mongo，如果改了端口就指定端口连接
    use admin
    db.createUser({ user: "user", pwd: "password", roles: [{ role: "userAdminAnyDatabase",db: "admin" }] })
    //userAdminAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的userAdmin权限
    //验证权限
    db.auth("user","password")
    //修改权限
    db.updateUser("user",{roles: [ "role","readWriteAnyDatabase",db: "admin"]})
    //readWriteAnyDatabase：只在admin数据库中可用，赋予用户所有数据库的读写权限
    //其他的权限请自行百度或查询官方文档，我这里为方便，直接用readWriteAnyDatabase
  ```

  

------------

# 三、代码结构说明

- 包结构前缀统一为：vip.wangjc.mongo。然后下面根据职责细分：注解，base基类，异常类，构建工厂，缓存池，注册器，工具。其实很多连接配置的工作Hutool提供者已经帮忙做好了（但是它竟然在MongoDS.java中把配置文件路径给写死了。。。。），所以省去了很大一部分开发量。

  ## 结构如下：

  ```java
  ├─src
  	│  └─main
  	│      ├─java
  	│      │  └─vip
  	│      │      └─wangjc
  	│      │          └─mongo
  	│      │              ├─annotation
  	│      │              │      MongoDB.java （注解：作用于实体，设置DBName）
  	│      │              │      MongoScanRegister.java (注解：作用于配置代码，设置包的扫描路径--核心！)
  	│      │              │      MongoTable.java （注解：作用于实体，设置tableName）
  	│      │              │
  	│      │              ├─base
  	│      │              │  ├─entity
  	│      │              │  │      MongoBaseEntity.java （mongo集合映射的实体基类，提供mongoId，提供泛型约束）
  	│      │              │  │
  	│      │              │  └─service
  	│      │              │      │  IMongoBaseService.java (mongo实体对应的service接口，提供统一的增删改查功能)
  	│      │              │      │
  	│      │              │      └─impl
  	│      │              │              MongoBaseServiceImpl.java （接口的实现类，只对外暴露上述实现方法）
  	│      │              │
  	│      │              ├─exception
  	│      │              │      MongoException.java （异常）
  	│      │              │
  	│      │              ├─factory
  	│      │              │      MongoClassFieldPoolFactory.java （字段数据缓存池：初始化时反射获取并存放每个mongo实体的字段数组，）
  	│      │              │      MongoCollectionPoolFactory.java （mongo连接的缓存池，初始化时对每一个mongo实体建立并存放连接）
  	│      │              │
  	│      │              ├─register
  	│      │              │      ImportMongoScanRegister.java （MongoDB的注册类，包括加载配置文件，初始化数据源，初始化缓存池……核心！）
  	│      │              │
  	│      │              └─util
  	│      │                      MongoUtil.java （工具类，多半服务于反射操作）
  ```

  ## UML如下：

  ![wangjc-vip-mongo-starter](http://www.wangjc.vip/group1/M00/00/01/rBAAD1_dvV6AHXlgAAHTALxYASU147.png "wangjc-vip-mongo-starter")

------------

# 四、使用方式

## 1.添加依赖

```xml
	<dependency>
	  <groupId>vip.wangjc</groupId>
	  <artifactId>wangjc-vip-mongo-starter</artifactId>
	  <version>1.1.0</version>
	</dependency>
```

## 2.添加配置代码

由于提供MongoScanRegister注解，设置包的扫描路径（可以是数组），就是mongo实体的包路径，层级高越级不要紧，只要能在该路径下迭代遍历get得到就行，所以需要在项目中提供配置代码，如下即可

```java
@Configuration
@MongoScanRegister("包路径")
public class MongoConfig {

}
```

## 3.忽略springboot关于MongoDB的自动配置

springboot默认自动配置了支持mongodb。在启动项目时会自动实例化一个mongo实例，我这里由于一开始就没有使用它，现在为了避免引入mongo报错，需要在启动时忽略mongoDB的自动化配置:

```java
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
```

如上所示，SpringBootApplication注解过滤掉MongoAutoConfiguration和MongoDataAutoConfiguration类，后期考虑向自动化配置靠拢兼容

## 4.提供配置文件

这里就是吐槽一下Hutool那位代码贡献者，ta在配置中把连接地址给写死了，多处写的都是纯字符串地址，我本来想在此基础上重写的，但作者提供的代码不是面向接口的风格，继承要写一大套逻辑，还关联了其他的类，就只好妥协。于是乎，配置文件的路径一定得为resources下面的config/mongo.setting。mongo.setting文件的内容如下所示：

```java
#--------------------------------------
# MongoDB 连接设定
#--------------------------------------
#每个主机答应的连接数（每个主机的连接池大小），当连接池被用光时，会被阻塞住 ，默以为10 --int
connectionsPerHost=100
#线程队列数，它以connectionsPerHost值相乘的结果就是线程队列最大值。如果连接线程排满了队列就会抛出“Out of semaphores to get db”错误 --int
threadsAllowedToBlockForConnectionMultiplier=10
#被阻塞线程从连接池获取连接的最长等待时间（ms） --int
maxWaitTime = 120000
#在建立（打开）套接字连接时的超时时间（ms），默以为0（无穷） --int
connectTimeout=0
#套接字超时时间;该值会被传递给Socket.setSoTimeout(int)。默以为0（无穷） --int
socketTimeout=0
#是否打开长连接. defaults to false --boolean
socketKeepAlive=false

#用户密码登录，需要先分配读写权限（我这里创建了一个超级权限用户）
user=用户名
pass=密码
database=数据库

#---------------------------------- MongoDB实例连接：主从集群
[master]
host=地址
port=端口
[slave]
host=地址
port=端口
#-----------------------------------------------------
```

## 5.代码测试

### 实体：

```java
@MongoDB("i4_data_cloud_system")
@MongoTable("rich_text")
public class MongoRichText extends MongoBaseEntity {
	private static final long serialVersionUID = -262263187751267271L;

	/**
	 * 内容，作用富文本编辑器
	 */
	private String content;

	/**
	 * md内容，作用Markdown编辑器
	 */
	private String mdContent;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMdContent() {
		return mdContent;
	}

	public void setMdContent(String mdContent) {
		this.mdContent = mdContent;
	}

}
```

### 接口：

```java
public interface IMongoRichTextService extends IMongoBaseService<MongoRichText> {

}
```

### 实现类：

```java
@Service
public class MongoRichTextServiceImpl extends MongoBaseServiceImpl<MongoRichText> implements IMongoRichTextService {
	
}
```

### 配置代码

```java
@Configuration
@MongoScanRegister("vip.wangjc.test.mongo.entity")
public class MongoConfig {
	
}
```

### 配置文件

如上（3.提供配置文件）所示！

### 测试

```java
@Autowired
private IMongoRichTextService mongoRichTextService;
 
@RequestMapping("selectAll")
public Object selectAll(){
    List<MongoRichText> richTexts = mongoRichTextService.selectAll();
    return richTexts;
}

@RequestMapping("selectOne")
public Object selectOne(){
    MongoRichText richTexts = mongoRichTextService.selectByMongoId("5fc0a6dd4c35c34dac8eab96");
    return richTexts;
}
```
