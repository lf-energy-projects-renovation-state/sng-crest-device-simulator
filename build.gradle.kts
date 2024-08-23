// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask
import io.spring.gradle.dependencymanagement.internal.dsl.StandardDependencyManagementExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    id("org.springframework.boot") version "3.3.2" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.20" apply false
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1" apply false
    id("org.sonarqube") version "5.0.0.4638"
    id("eclipse")
}

version = System.getenv("GITHUB_REF_NAME")?.replace("/", "-")?.lowercase() ?: "develop"

sonar {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectKey", "OSGP_sng-crest-device-simulator")
        property("sonar.organization", "gxf")
        property("sonar.gradle.skipCompile", true)
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "eclipse")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "jacoco")
    apply(plugin = "jacoco-report-aggregation")

    group = "org.gxf.crestdevicesimulator"
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    extensions.configure<StandardDependencyManagementExtension> {
        imports { mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES) }
    }

    extensions.configure<KotlinJvmProjectExtension> {
        jvmToolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    tasks.withType<KotlinCompile> {
        dependsOn(tasks.withType<GenerateAvroJavaTask>())
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
