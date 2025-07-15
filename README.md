# 苍穹外卖项目
## 项目概述
- 为了完成对刚刚学习完的 Javaweb 相关的课程而完成的相关内容，对于 Springboot 和企业开发流程进行一个全过程的认识。


## 项目技术栈
- 后端：JavaSE，Springboot，SpringMVC，JavaWeb，Spring, Maven
- 数据持久化框架：Mybatis(后期考虑使用增强化插件)
- 数据库：MySQL
- 前端：Vue3，Vite
- 中间件：Redis，JWT 分布认证， Nginx
- 项目部署：docker，Linux
- 版本控制：Git

## 学习阶段进展

- 2025.07.06 
  - 项目前端和后端的初始化，对代码进行了大概的了解，学习了企业级项目的规范和完整性和严谨性

- 2025.07.07 
  - 新增员工接口的开发，使用 JWT 和 ThreadLocal 完成了对当前操作员工 ID 的追踪，定义了异常处理器完善前端的报错返回信息
  - 分页查询的接口开发，使用 PageHelper 插件完成相关的开发，遇到的日期格式化问题使用扩展 MVC 的消息转换器进行全局的解决方法
  - 启用禁用员工接口开发
  - 编辑员工接口代码开发：查询回显 + 更新员工信息
  - 修改密码接口代码开发：使用ThreadLocal来获取相关的ID，为PasswordEditDTO中的empIdj进行赋值，然后根据实际合理的业务进行开发
- 2025.07.08
  - 分类管理接口代码开发
  - 公共字段自动填充的切面开发，使用了 **AOP切面编程**,见 `AutoFillAspect.java`
  - 文件上传接口的开发
- 2025.07.09
  - 新增菜品接口代码开发
  - 菜品分页查询接口代码开发
  - 修改菜品接口代码开发
  - 查询回显代码开发
- 2025.07.13
  - 套餐所有的接口代码开发
  - 菜品停售和起售的接口代码开发

- 2025.07.15
  - 店铺营业设置接口开发
  - 学习Redis



## 相关知识点
### ThreadLocal
不是 **线程**，只是线程中一个独立的变量，类似通道技术，对每个线程中存储的变量进行独立存储，在我们的项目中，每一次的请求都是一个新的线程，
我们这里通过 JWT 的解析，拿到前端发送请求中的 Header 中的 token 中的信息，通过进行解析 token 中的信息，将这个员工的 ID 存入当前线程的 ThreadLocal 中
完成用户 ID 的全局存储。

### MVC 消息转换器

```java
/**
 * 扩展MVC框架的消息转换器
 */
protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    log.info("扩展消息转换器");
    // 创建一个消息转换器
    MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
    mappingJackson2HttpMessageConverter.setObjectMapper(new JacksonObjectMapper());
    // 将我们自己的转换器加入容器中
    converters.add(0, mappingJackson2HttpMessageConverter);
}
```

- 消息转换器相关代码写在我们的 config 中的 WebMvcConfiguration 类中，这个类要继承 Springboot 的 WebMvcConfigurationSupport 这个类来实现在运行项目时的配置
- 具体概念：**MVC 消息转换器是 Spring MVC 框架中的一个核心组件，专门负责将 HTTP 请求的 body（正文）和 Java 对象之间进行双向转换**

- **MappingJackson2HttpMessageConverter**：这个是专门用来进行 JSON 转化的一个转换器，通过对他的设置，我们实现了 **统一全局的日期格式**。

### AOP切面编程

- 连接点：JoinPoint， 可以被 AOP 控制的方法
- 通知：Advice，指那些重复的逻辑
- 切入点：PointCut，切入点，匹配连接点的条件，通知仅会在切入点方法执行时被应用
- 切面：Aspect，描述通知与切入点的对应关系
- 目标对象：Target， 通知所应用的对象
- **execution**:`execution(访问修饰符？ 返回值 包名.类名.?方法名(方法参数) throw 异常？)`
- **定义切点可以使用自定义注解**：`@Pointcut("@annotation(com.itheima.anno.Log)")`

![image-20250702203423180](http://img.wiretender.top/img/20250702203423814.webp)

### 动态SQL

- **foreach遍历**：

  ```sql
  <foreach collection="flavors" item="df">
      (#{df.dishId}, #{df.name}, #{df.value})
  </foreach>
  ```

- **返回主键值的xml写法**： `useGeneratedKeys="true" keyProperty="id"`
  - 这样的写法，就会使得我们在实体对象`dish.getId()`中可以得到插入后的主键值

```sql
<insert id="insert" useGeneratedKeys="true" keyProperty="id">
    insert into dish (name, category_id, price, image, description, create_time, update_time, create_user, update_user, status)
    values
    (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser}, #{status})
</insert>
```

## Redis

### 简介

- **Redis** 是一个基于**内存**的 key-value 结构数据库。
- 基于内存存储，读写性能高
- 适合存储热点数据
- 企业应用广泛

**主要特点：**

- 基于内存存储，读写性能高  
- 适合存储热点数据（热点商品、资讯、新闻）
- 企业应用广泛

Redis是用C语言开发的一个开源的高性能键值对(key-value)数据库，官方提供的数据是可以达到100000+的QPS（每秒内查询次数）。它存储的value类型比较丰富，也被称为结构化的NoSql数据库。

NoSql（Not Only SQL），不仅仅是SQL，泛指**非关系型数据库**。NoSql数据库并不是要取代关系型数据库，而是关系型数据库的补充。

**关系型数据库(RDBMS)：**

- Mysql
- Oracle
- DB2
- SQLServer

**非关系型数据库(NoSql)：**

- Redis
- Mongo db
- MemCached

### 数据类型

Redis存储的是key-value结构的数据，其中key是字符串类型，value有5种常用的数据类型：

- 字符串 string
- 哈希 hash
- 列表 list
- 集合 set
- 有序集合 sorted set / zset

**解释**

- 字符串(string)：普通字符串，Redis中最简单的数据类型
- 哈希(hash)：也叫散列，类似于Java中的HashMap结构
- 列表(list)：按照插入顺序排序，可以有重复元素，类似于Java中的LinkedList
- 集合(set)：无序集合，没有重复元素，类似于Java中的HashSet
- 有序集合(sorted set/zset)：集合中每个元素关联一个分数(score)，根据分数升序排序，没有重复元素

### 操作命令

Redis 中字符串类型常用命令：

- **SET** key value 					         设置指定key的值
- **GET** key                                        获取指定key的值
- **SETEX** key seconds value         设置指定key的值，并将 key 的过期时间设为 seconds 秒
- **SETNX** key value                        只有在 key    不存在时设置 key 的值

Redis hash 是一个string类型的 field 和 value 的映射表，hash特别适合用于存储对象，常用命令：

- **HSET** key field value             将哈希表 key 中的字段 field 的值设为 value
- **HGET** key field                       获取存储在哈希表中指定字段的值
- **HDEL** key field                       删除存储在哈希表中的指定字段
- **HKEYS** key                              获取哈希表中所有字段
- **HVALS** key                              获取哈希表中所有值

Redis 列表是简单的字符串列表，按照插入顺序排序，常用命令：

- **LPUSH** key value1 [value2]         将一个或多个值插入到列表头部
- **LRANGE** key start stop                获取列表指定范围内的元素
- **RPOP** key                                       移除并获取列表最后一个元素
- **LLEN** key                                        获取列表长度
- **BRPOP** key1 [key2 ] timeout       移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超    时或发现可弹出元素为止

Redis set 是string类型的无序集合。集合成员是唯一的，这就意味着集合中不能出现重复的数据，常用命令：

- **SADD** key member1 [member2]            向集合添加一个或多个成员
- **SMEMBERS** key                                         返回集合中的所有成员
- **SCARD** key                                                  获取集合的成员数
- **SINTER** key1 [key2]                                   返回给定所有集合的交集
- **SUNION** key1 [key2]                                 返回所有给定集合的并集
- **SREM** key member1 [member2]            移除集合中一个或多个成员

Redis有序集合是string类型元素的集合，且不允许有重复成员。每个元素都会关联一个double类型的分数。常用命令：

常用命令：

- **ZADD** key score1 member1 [score2 member2]     向有序集合添加一个或多个成员
- **ZRANGE** key start stop [WITHSCORES]                     通过索引区间返回有序集合中指定区间内的成员
- **ZINCRBY** key increment member                              有序集合中对指定成员的分数加上增量 increment
- **ZREM** key member [member ...]                                移除有序集合中的一个或多个成员

Redis的通用命令是不分数据类型的，都可以使用的命令：

- KEYS pattern 		查找所有符合给定模式( pattern)的 key 
- EXISTS key 		检查给定 key 是否存在
- TYPE key 		返回 key 所储存的值的类型
- DEL key 		该命令用于在 key 存在是删除 key

更多命令可以参考Redis中文网：https://www.redis.net.cn

