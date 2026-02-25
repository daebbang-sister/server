package com.daebbang.daebbangapi.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.daebbang")
@EnableJpaRepositories(basePackages = "com.daebbang")
public class JpaConfig {
}
