plugins {
    kotlin("jvm")
}

group = "com.github.zjcong.metis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":metis-core"))
    implementation(kotlin("stdlib"))
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
