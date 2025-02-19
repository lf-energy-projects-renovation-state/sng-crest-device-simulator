// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    alias(libs.plugins.springBoot)
}

dependencies {
    implementation(libs.bundles.californium)
    implementation(libs.commonsCodec)
    implementation(libs.jacksonDataformatCbor)
    implementation(libs.jakartaXmlBindApi)
    implementation(libs.kotlinReflect)
    implementation(libs.logging)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterDataJpa)
    implementation(libs.springBootStarterWeb)
    implementation(libs.jacksonKotlinModule)

    runtimeOnly(libs.flyway)
    runtimeOnly(libs.micrometerPrometheusModule)
    runtimeOnly(libs.postgresql)

    testImplementation(libs.mockk)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.springmockk)

    testRuntimeOnly(libs.junitPlatformLauncher)

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
        val integrationTest by
            registering(JvmTestSuite::class) {
                useJUnitJupiter()
                dependencies {
                    implementation(project())
                    implementation(libs.awaitility)
                    implementation(libs.jacksonDataformatCbor)
                    implementation(libs.mockk)
                    implementation(libs.springBootStarterDataJpa)
                    implementation(libs.springBootStarterTest)
                    implementation.bundle(libs.bundles.californium)
                    runtimeOnly(libs.h2)
                }
            }
    }
}
