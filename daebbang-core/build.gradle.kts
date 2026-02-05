import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "daebbang-core"

plugins {
    id("java-library")
    id("org.springframework.boot")
}

dependencies {
    // common dependency
    api(project(":daebbang-common"))

    // jpa
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}