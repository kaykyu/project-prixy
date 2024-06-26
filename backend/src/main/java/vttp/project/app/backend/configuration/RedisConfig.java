package vttp.project.app.backend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import vttp.project.app.backend.model.LineItem;
import vttp.project.app.backend.model.Login;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private Integer redisPort;

    @Value("${spring.redis.username}")
    private String redisUser;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.database.users}")
    private Integer redisUsersDatabase;

    @Value("${spring.redis.database.receipts}")
    private Integer redisReceiptsDatabase;

    public JedisConnectionFactory createConnectionFactory(Integer database) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(database);

        if (!"NOT_SET".equals(redisUser.trim())) {
            config.setUsername(redisUser);
            config.setPassword(redisPassword);
        }

        JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();
        JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
        jedisFac.afterPropertiesSet();

        return jedisFac;
    }

    @Bean
    public RedisTemplate<String, Login> redisUsers() {

        RedisTemplate<String, Login> template = new RedisTemplate<>();
        template.setConnectionFactory(createConnectionFactory(redisUsersDatabase));

        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, LineItem> redisReceipts() {

        RedisTemplate<String, LineItem> template = new RedisTemplate<>();
        template.setConnectionFactory(createConnectionFactory(redisReceiptsDatabase));

        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }
}
