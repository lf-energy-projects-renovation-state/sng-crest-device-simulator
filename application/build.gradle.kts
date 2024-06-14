// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.springframework.boot")
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation(kotlin("reflect"))
    implementation(libs.bundles.californium)
    implementation(libs.logging)

    implementation(libs.commonsCodec)

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api")

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockitoKotlin)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Generate test and integration test reports
    jacocoAggregation(project(":application"))
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootBuildImage> {
    imageName.set("ghcr.io/osgp/sng-crest-device-simulator:${version}")
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
                implementation.bundle(libs.bundles.californium)
                implementation("org.awaitility:awaitility")
                implementation("org.springframework.boot:spring-boot-starter-test")
                implementation("org.springframework.boot:spring-boot-starter-data-jpa")
                runtimeOnly("com.h2database:h2")
            }
        }
    }
}
