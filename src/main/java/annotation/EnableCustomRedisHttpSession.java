package annotation;

import config.CustomRedisHttpSessionConfiguration;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author tifa
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(CustomRedisHttpSessionConfiguration.class)
public @interface EnableCustomRedisHttpSession {

  long maxInactiveIntervalInSeconds() default 1800L;
}
