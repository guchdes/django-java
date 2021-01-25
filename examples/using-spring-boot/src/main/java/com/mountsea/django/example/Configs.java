package com.mountsea.django.example;

import com.mountsea.django.core.cache.AbstractCachePlugin;
import com.mountsea.django.core.spring.DaoFactoryConfigurer;
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
