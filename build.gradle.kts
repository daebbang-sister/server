description = "daebbang-root"

plugins {
    java
    id("org.springframework.boot") version "4.0.6" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {

    group = "com.daebbang"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}

subprojects {

    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        dependencies {
            dependency("org.springframework.security:spring-security-web:7.0.4")
            dependency("org.apache.tomcat.embed:tomcat-embed-core:11.0.20")
            dependency("io.netty:netty-codec-http:4.2.11.Final")
            dependency("io.netty:netty-codec-http2:4.2.11.Final")
        }
    }

}

tasks.named<Jar>("jar") {
    enabled = false
}