group = "ru.spbsu.kotlin"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.commons.compress)
    implementation(libs.logback)
    implementation(libs.coroutines.slf4j)
    implementation(libs.coroutines.core)
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.coroutines.test)
}

tasks.test {
    // dependsOn(tasks.withType<KtLintCheckTask>())
    // Enable JUnit 5 (Gradle 4.6+).
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(JavaVersion.VERSION_21.majorVersion.toInt())
}
