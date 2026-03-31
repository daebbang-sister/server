import org.springframework.boot.gradle.tasks.bundling.BootJar

description = "daebbang-api"

plugins {
    id("org.springframework.boot")
    id("com.epages.restdocs-api-spec") version "0.18.2"
}

dependencies {
    // core dependency
    implementation(project(":daebbang-core"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // mapstruct
    implementation("org.mapstruct:mapstruct:1.5.3.Final")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.5.3.Final")

    // oauth2
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // test
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-restdocs")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
}

tasks.named<BootJar>("bootJar") {
    enabled = true
}

tasks.named<Jar>("jar") {
    enabled = false
}

openapi3 {
    setServer("http://localhost:8080")
    title = "대빵 언니 API 서버 API 명세서"
    description = "대빵 언니 API 서버 명세서입니다."
    version = "1.0.0"
    format = "yaml"
}

// openapi3 생성 후 운영 서버 URL을 servers 목록 맨 앞에 추가
tasks.register("patchOpenApiServers") {
    dependsOn("openapi3")
    doLast {
        val file = layout.buildDirectory.file("api-spec/openapi3.yaml").get().asFile
        val content = file.readText().replace("\r\n", "\n")
        val modified = content.replace(
            "servers:\n- url: http://localhost:8080\n",
            "servers:\n" +
            "- url: https://api.daebbang-sister.shop\n" +
            "  description: 운영 서버\n" +
            "- url: http://localhost:8080\n" +
            "  description: 로컬 개발 서버\n"
        )
        file.writeText(modified)
    }
}

afterEvaluate {
    tasks.named("openapi3") {
        dependsOn("test")
    }
}

tasks.register<Copy>("copyOasToSwagger") {
    delete("src/main/resources/static/swagger-ui/openapi3.yaml")
    from(layout.buildDirectory.file("api-spec/openapi3.yaml"))
    into("src/main/resources/static/swagger-ui/.")
    dependsOn("patchOpenApiServers")
}