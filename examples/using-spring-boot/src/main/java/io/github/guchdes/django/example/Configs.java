package io.github.guchdes.django.example;

import io.github.guchdes.django.core.cache.AbstractCachePlugin;
import io.github.guchdes.django.core.spring.DaoFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configs {

    @Bean
    public DaoFactoryConfigurer daoFactoryConfigurer() {
        return factoryBuilder -> {
//            factoryBuilder.getDefaultDaoConfig().cachePlugin(...);
        };
    }
}
