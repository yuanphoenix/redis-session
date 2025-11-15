package session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.session.Session;

/**
 * @author tifa
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomRedisSession implements Session {

  private String id;
  private Map<String, Object> attributes = new HashMap<>();

  private Instant creationTime = Instant.now();
  private Instant lastAccessedTime = Instant.now();
  private long maxInactiveIntervalInSeconds = 1800;


  public void setId(String id) {
    this.id = id;
  }

  public Map<String, Object> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  public void setCreationTime(Instant creationTime) {
    this.creationTime = creationTime;
  }

  public long getMaxInactiveIntervalInSeconds() {
    return maxInactiveIntervalInSeconds;
  }

  public void setMaxInactiveIntervalInSeconds(long maxInactiveIntervalInSeconds) {
    this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
  }


  public CustomRedisSession() {
  }


  public static CustomRedisSession create() {
    CustomRedisSession s = new CustomRedisSession();
    s.id = UUID.randomUUID().toString();
    s.creationTime = Instant.now();
    s.lastAccessedTime = s.creationTime;
    s.maxInactiveIntervalInSeconds = 1800;
    return s;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String changeSessionId() {
    String newId = UUID.randomUUID().toString();
    this.id = newId;
    return newId;
  }

  @Override
  public <T> T getAttribute(String s) {
    return (T) this.attributes.get(s);
  }

  @Override
  public Set<String> getAttributeNames() {
    return new HashSet<>(this.attributes.keySet());
  }

  @Override
  public void setAttribute(String s, Object o) {
    attributes.put(s, o);
  }

  @Override
  public void removeAttribute(String s) {
    attributes.remove(s);
  }

  @Override
  public Instant getCreationTime() {
    return creationTime;
  }

  @Override
  public void setLastAccessedTime(Instant instant) {
    this.lastAccessedTime = instant;
  }

  @Override
  public Instant getLastAccessedTime() {
    return lastAccessedTime;
  }

  @Override
  public void setMaxInactiveInterval(Duration duration) {
    this.maxInactiveIntervalInSeconds = duration.toSeconds();
  }

  @Override
  public Duration getMaxInactiveInterval() {
    return Duration.ofSeconds(this.maxInactiveIntervalInSeconds);
  }

  @Override
  public boolean isExpired() {
    return Duration.between(lastAccessedTime, Instant.now()).compareTo(getMaxInactiveInterval())
        > 0;
  }
}
