import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.61"
}

group = "io.xol"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compile("org.joml", "joml", "1.9.11")
    compile("org.l33tlabs.twl", "pngdecoder", "1.0")

    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}