description = "daebbang-root"

plugins {
    java
    id("org.springframework.boot") version "4.0.2" apply false
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

    // Fix GHSA-72hv-8253-57qq: Allocation of Resources Without Limits or Throttling in jackson-core
    // - tools.jackson.core:jackson-core  >= 3.0.0, < 3.1.0 → fixed in 3.1.0
    // - com.fasterxml.jackson.core:jackson-core >= 2.19.0, < 2.21.1 → fixed in 2.21.1
    // CVE는 jackson-core에만 해당하므로 jackson-core만 업그레이드 (jackson-databind는 Spring Boot 4.0.2 호환성 유지)
    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        dependencies {
            dependency("tools.jackson.core:jackson-core:3.1.0")
            dependency("com.fasterxml.jackson.core:jackson-core:2.21.1")
        }
    }

}

tasks.named<Jar>("jar") {
    enabled = false
}