plugins {
    kotlin("jvm") version "1.9.20"
}

group = "de.makohn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

tasks.named("compileKotlin", org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask::class.java) {
    compilerOptions {
        freeCompilerArgs.addAll("-opt-in=kotlin.ExperimentalUnsignedTypes", "-Xcontext-receivers")
    }
}