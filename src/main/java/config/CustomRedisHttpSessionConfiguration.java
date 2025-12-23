package config;


import annotation.EnableCustomRedisHttpSession;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;
import session.CustomRedisSession;
import session.CustomRedisSessionRepository;

/**
 * @author tifa
 */
//声明这是一个 Spring 配置类，里面的 @Bean 会被注册到容器
@Configuration
//项目是否已经显式启用，并且已经配置了 RedisConnectionFactory 就是redis的host之类的信息
@ConditionalOnBean(annotation = EnableCustomRedisHttpSession.class)

//某个 Class 是否存在
@ConditionalOnClass(RedisTemplate.class)
public class CustomRedisHttpSessionConfiguration implements ImportAware {

    //因为已经ConditionalOnBean了，所以一定会存在 redisConnectionFactory 的
    private final RedisConnectionFactory redisConnectionFactory;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private long maxInactiveIntervalInSeconds = 1800L;

    public CustomRedisHttpSessionConfiguration(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
        logger.info("CustomRedisHttpSessionConfiguration 加载成功");
    }


    @Bean
    public CustomRedisSessionRepository customRedisSessionRepository(@Qualifier("springSessionDefaultRedisSerializer") @Autowired(required = false) RedisSerializer<Object> redisSerializer) {
        if (redisSerializer == null) {
            redisSerializer = new GenericJackson2JsonRedisSerializer();
        }
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setDefaultSerializer(redisSerializer);
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();

        CustomRedisSessionRepository repository = new CustomRedisSessionRepository(redisTemplate);
        repository.setDefaultMaxInactiveInterval(this.maxInactiveIntervalInSeconds);
        logger.info("创建 CustomRedisSessionRepository bean");

        return repository;
    }

    //负责告如何从请求中获取或写入 session id
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver(CookieSerializer cookieSerializer) {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        resolver.setCookieSerializer(cookieSerializer);
        logger.info("创建resolver");
        return resolver;
    }


    //这个 bean 是整个 Spring Session 的核心，把 Redis 存储和自定义 session 与 web 请求关联起来。
    @Bean
    public SessionRepositoryFilter<CustomRedisSession> springSessionRepositoryFilter(
            CustomRedisSessionRepository repository,
            HttpSessionIdResolver httpSessionIdResolver) {
        SessionRepositoryFilter<CustomRedisSession> filter = new SessionRepositoryFilter<>(repository);
        filter.setHttpSessionIdResolver(httpSessionIdResolver);
        logger.info("注册 filter");
        return filter;
    }


    //用于读取元数据
    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        if (importMetadata.hasAnnotation(EnableCustomRedisHttpSession.class.getName())) {
            Map<String, Object> annotationAttributes = importMetadata.getAnnotationAttributes(
                    EnableCustomRedisHttpSession.class.getName());
            Number num = (Number) annotationAttributes.get("maxInactiveIntervalInSeconds");
            this.maxInactiveIntervalInSeconds = num.longValue();
        }
    }
}
