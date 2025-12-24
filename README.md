# Redis-Session
仿照 **spring-session-data-redis** 制作的**redis-session** 主要是为了自己写一个依赖。
## 解读
### 入口EnableCustomRedisHttpSession
这个类是唯一的一个注解类，其他项目引用的时候就引用这个注解即可。
### 配置类CustomRedisHttpSessionConfiguration
用于定义配置，包装了具体的功能类 **CustomRedisSessionRepository**
### 功能类CustomRedisSessionRepository
session的仓库，实现了 SessionRepository<CustomRedisSession> 接口。
### Bean类CustomRedisSession
实现了Session