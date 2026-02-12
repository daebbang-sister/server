package com.daebbang.daebbangapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = "com.daebbang")
@EnableJpaRepositories(basePackages = "com.daebbang")
@SpringBootApplication(scanBasePackages = "com.daebbang")
public class DaebbangApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaebbangApiApplication.class, args);
    }

}
