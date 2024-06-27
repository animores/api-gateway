package animores.api_gateway.config;

import animores.api_gateway.security.BlackListToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }


    @Bean
    ReactiveValueOperations<String, BlackListToken> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<BlackListToken> serializer = new Jackson2JsonRedisSerializer<>(BlackListToken.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, BlackListToken> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, BlackListToken> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context).opsForValue();
    }
}

