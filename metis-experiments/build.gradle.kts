import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

group = "com.github.zjcong.metis"
version = "1.0-SNAPSHOT"

System.setProperty("jcuda.os", "linux")
System.setProperty("jcuda.arch", "x86_64")

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":metis-core"))

    implementation(project(":metis-extra"))

    implementation("org.ojalgo:ojalgo:49.2.1")
    implementation("com.formdev:flatlaf:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.10")
    implementation("org.knowm.xchart:xchart:3.8.1")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.2.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("com.github.zjcong.metis.experiments.coco.COCOExperimentKt")
}