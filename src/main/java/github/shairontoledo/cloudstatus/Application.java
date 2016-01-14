package github.shairontoledo.cloudstatus;

import github.shairontoledo.cloudstatus.persistence.DataObjectRepository;
import github.shairontoledo.cloudstatus.persistence.GenericRedisDataObjectRepository;
import github.shairontoledo.cloudstatus.task.Seed;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@SpringBootApplication
public class Application {

    @Value("${redis.hostname:localhost}")
    private String redisHostName;

    @Value("${redis.port:6379}")
    private int redisPort;

    @Bean
    TokenManager tokenManager(){ return new TokenManager(); }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPort(redisPort);
        factory.setHostName(redisHostName);
        return factory;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/services/**");
                registry.addMapping("*/*");
                registry.addMapping("/**");
                registry.addMapping("/**/**");
            }
        };
    }

    @Bean
    RedisTemplate< String, Long > redisTemplate() {
        final RedisTemplate template =  new RedisTemplate();
        template.setConnectionFactory( jedisConnectionFactory() );
        template.setKeySerializer( new StringRedisSerializer() );
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    DataObjectRepository dataObjectRepository() {
        return new GenericRedisDataObjectRepository();
    }
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    Seed seed(){ return new Seed(); }

    @Bean
    CommandLineRunner runner (){
        return new CommandLineTools();
    }

}