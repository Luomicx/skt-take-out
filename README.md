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

## 技术要点（后续会写）

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
