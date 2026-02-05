import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "daebbang-common"

plugins {
    id("java-library")
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}

tasks.named<Jar>("jar") {
    enabled = true
}