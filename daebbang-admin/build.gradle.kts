import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "daebbang-admin"

plugins {
    id("org.springframework.boot")
}

dependencies {
    // core dependency
    implementation(project(":daebbang-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}