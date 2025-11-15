package session;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.session.SessionRepository;

/**
 * @author tifa
 */
public class CustomRedisSessionRepository implements SessionRepository<CustomRedisSession> {

  private static final Logger log = LoggerFactory.getLogger(CustomRedisSessionRepository.class);
  private final RedisTemplate<String, Object> redisTemplate;
  private final String prefix = "custom:session:";
  private Long defaultMaxInactiveIntervalInSeconds = 1800L;

  public CustomRedisSessionRepository(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public CustomRedisSession createSession() {
    CustomRedisSession session = CustomRedisSession.create();
    session.setMaxInactiveInterval(Duration.ofSeconds(this.defaultMaxInactiveIntervalInSeconds));
    return session;
  }

  public String changeSessionId(CustomRedisSession session) {
    String oldId = session.getId();
    // 生成新 ID
    String newId = session.changeSessionId();
    redisTemplate.delete(prefix + oldId);
    redisTemplate.opsForValue()
        .set(prefix + newId, session, session.getMaxInactiveInterval());
    return newId;
  }

  private String sessionKey(String id) {
    return prefix + id;
  }


  @Override
  public void save(CustomRedisSession session) {
    redisTemplate.opsForValue()
        .set(sessionKey(session.getId()), session, session.getMaxInactiveInterval());
  }

  @Override
  public CustomRedisSession findById(String id) {
    GenericJackson2JsonRedisSerializer defaultSerializer = (GenericJackson2JsonRedisSerializer) redisTemplate.getDefaultSerializer();
    log.info("反序列化的地址{}", defaultSerializer);
    log.info("key是{}", prefix + id);
    Object value = redisTemplate.opsForValue()
        .get(sessionKey(id));
    CustomRedisSession session = defaultSerializer.deserialize(
        defaultSerializer.serialize(value), CustomRedisSession.class);
    return session;
//    byte[] data = (byte[]) redisTemplate.opsForValue().get(sessionKey(id));
//    if (data == null) {
//      return null;
//    }
//
//    GenericJackson2JsonRedisSerializer serializer =
//        (GenericJackson2JsonRedisSerializer) redisTemplate.getValueSerializer();
//    return serializer.deserialize(data, CustomRedisSession.class);
  }

  @Override
  public void deleteById(String id) {
    redisTemplate.delete(sessionKey(id));
  }

  public void setDefaultMaxInactiveInterval(Long maxInactiveIntervalInSeconds) {
    this.defaultMaxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
  }
}
