import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "daebbang-core"

plugins {
    id("java-library")
    id("org.springframework.boot")
    id("com.epages.restdocs-api-spec") version "0.18.2"
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
    api("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    
    // mail
    implementation("org.springframework.boot:spring-boot-starter-mail")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // cool - sms
    implementation("com.solapi:sdk:1.0.3")

    // mysql
    runtimeOnly("com.mysql:mysql-connector-j")

    // test
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}