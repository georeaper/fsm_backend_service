//build.gradle.kts under ktor-sample project
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
   //id("com.github.johnrengelman.shadow") version "8.1.1"
   id("com.gradleup.shadow") version "8.3.4"
}

group = "com.example"
version = "0.0.1"

application {
//    mainClass.set("io.ktor.server.netty.EngineMain")
    mainClass.set("com.example.ApplicationKt")
    //val isDevelopment: Boolean = project.hasProperty("development")
    //applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
    applicationDefaultJvmArgs = listOf("-Dktor.config=application.yaml")
}

repositories {
    mavenCentral()

}
tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("fsm-backend")
    archiveClassifier.set("") // removes "-all" suffix
    archiveVersion.set("0.0.1")
    manifest {
        attributes["Main-Class"] = "com.example.ApplicationKt"
    }
}
dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.postgresql)
    implementation(libs.h2)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    implementation(libs.ktor.server.cors)


    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("io.ktor:ktor-server-auth-jwt:2.0.0")

    implementation("com.google.code.gson:gson:2.8.8") // Add this line for Gson

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Additional Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:0.43.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.43.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.43.0")

    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.43.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    //Jackson Dependencies

    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
}
