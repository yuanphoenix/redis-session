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
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.SessionRepositoryFilter;
import session.CustomRedisSession;
import session.CustomRedisSessionRepository;

/**
 * @author tifa
 */
@Configuration
@ConditionalOnBean(annotation = EnableCustomRedisHttpSession.class)
@ConditionalOnClass(RedisTemplate.class)
public class CustomRedisHttpSessionConfiguration implements ImportAware {

  private RedisSerializer<Object> defaultSerializer;
  private RedisConnectionFactory redisConnectionFactory;
  private DefaultCookieSerializer defaultCookieSerializer;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private long maxInactiveIntervalInSeconds = 1800L;

  public CustomRedisHttpSessionConfiguration() {
    logger.info("CustomRedisHttpSessionConfiguration 加载成功");
  }


  @Autowired
  private ObjectProvider<RedisConnectionFactory> redisConnectionFactoryProvider;

  public RedisConnectionFactory getRedisConnectionFactory() {
    if (this.redisConnectionFactory == null) {
      this.redisConnectionFactory = this.redisConnectionFactoryProvider.getIfAvailable();
    }
    return this.redisConnectionFactory;
  }


  @Autowired(required = false)
  public void setDefaultRedisSerializer(
      @Qualifier("springSessionDefaultRedisSerializer") RedisSerializer<Object> defaultRedisSerializer) {
    this.defaultSerializer = defaultRedisSerializer;
  }

  protected RedisSerializer<Object> getDefaultRedisSerializer() {
    logger.info("加载的redisSertializer是{}", this.defaultSerializer);
    return this.defaultSerializer;
  }

  @Bean
  public CustomRedisSessionRepository customRedisSessionRepository() {
    logger.info("创建 CustomRedisSessionRepository bean");
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setKeySerializer(RedisSerializer.string());
    redisTemplate.setHashKeySerializer(RedisSerializer.string());
    if (getDefaultRedisSerializer() != null) {
      redisTemplate.setDefaultSerializer(getDefaultRedisSerializer());
    }
    redisTemplate.setConnectionFactory(getRedisConnectionFactory());
    redisTemplate.afterPropertiesSet();

    CustomRedisSessionRepository repository = new CustomRedisSessionRepository(redisTemplate);
    repository.setDefaultMaxInactiveInterval(this.maxInactiveIntervalInSeconds);
    return repository;
  }

  @Bean
  public HttpSessionIdResolver httpSessionIdResolver(
      @Qualifier("cookieSerializer") CookieSerializer cookieSerializer) {
    CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
    resolver.setCookieSerializer(cookieSerializer);
    return resolver;
  }


  @Bean
  public SessionRepositoryFilter<CustomRedisSession> springSessionRepositoryFilter(
      CustomRedisSessionRepository repository,
      HttpSessionIdResolver httpSessionIdResolver) {
    SessionRepositoryFilter<CustomRedisSession> filter = new SessionRepositoryFilter<>(repository);
    filter.setHttpSessionIdResolver(httpSessionIdResolver);
    return filter;
  }

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
