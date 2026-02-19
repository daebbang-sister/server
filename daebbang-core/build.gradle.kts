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
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    // flyway
    implementation("org.flywaydb:flyway-mysql")
    implementation("org.springframework.boot:spring-boot-starter-flyway")

    // jwt
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

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