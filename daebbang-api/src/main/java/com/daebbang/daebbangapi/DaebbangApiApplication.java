package com.daebbang.daebbangapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan("com.daebbang.daebbangapi.domain.security")
@SpringBootApplication(scanBasePackages = "com.daebbang")
public class DaebbangApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaebbangApiApplication.class, args);
    }

}
