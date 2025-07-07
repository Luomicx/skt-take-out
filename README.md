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
  - 完成了项目前端和后端的初始化，对代码进行了大概的了解，学习了企业级项目的规范和完整性和严谨性

- 2025.07.07 
  - 完成了新增员工接口的开发，使用 JWT 和 ThreadLocal 完成了对当前操作员工 ID 的追踪，定义了异常处理器完善前端的报错返回信息
  - 完成分页查询的接口开发，使用 PageHelper 插件完成相关的开发，遇到的日期格式化问题使用扩展 MVC 的消息转换器进行全局的解决方法


## 相关知识点
### ThreadLocal
不是 **线程**，只是线程中一个独立的变量，类似通道技术，对每个线程中存储的变量进行独立存储，在我们的项目中，每一次的请求都是一个新的线程，
我们这里通过 JWT 的解析，拿到前端发送请求中的 Header 中的 token 中的信息，通过进行解析 token 中的信息，将这个员工的 ID 存入当前线程的 threadLocal 中
完成用户 ID 的全局存储。

### MVC消息转换器

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

- 消息转换器相关代码写在我们的config中的WebMvcConfiguration类中，这个类要继承Springboot的WebMvcConfigurationSupport这个类来实现在运行项目时的配置
- 具体概念：**MVC消息转换器是 Spring MVC 框架中的一个核心组件，专门负责将 HTTP 请求的 body（正文）和 Java 对象之间进行双向转换**

- **MappingJackson2HttpMessageConverter**：这个是专门用来进行JSON转化的一个转换器，通过对他的设置，我们实现了 **统一全局的日期格式**。
