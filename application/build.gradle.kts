// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.eclipse.californium:californium-core:3.8.0")
    implementation("org.eclipse.californium:scandium:3.8.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")

    runtimeOnly("org.springframework:spring-aspects")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Generate test and integration test reports
    jacocoAggregation(project(":application"))
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName.set("ghcr.io/osgp/gxf-service-template:${version}")
    if (project.hasProperty("publishImage")) {
        publish.set(true)
        docker {
            publishRegistry {
                username.set(System.getenv("GITHUB_ACTOR"))
                password.set(System.getenv("GITHUB_TOKEN"))
            }
        }
    }
}

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
                implementation("org.eclipse.californium:californium-core:3.8.0")
                implementation("org.eclipse.californium:scandium:3.8.0")
                implementation("org.awaitility:awaitility")
                implementation("org.springframework.boot:spring-boot-starter-test")
            }
        }
    }
}
