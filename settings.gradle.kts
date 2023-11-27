// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "crest-device-simulator"

include("application")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("californium", "3.8.0")
            library("californium-core", "org.eclipse.californium", "californium-core").versionRef("californium")
            library("californium-scandium", "org.eclipse.californium", "scandium").versionRef("californium")
            bundle("californium", listOf("californium-core", "californium-scandium"))

            library("postgresql", "org.postgresql", "postgresql").version("42.5.4")
            library("flyway", "org.flywaydb", "flyway-core").version("9.22.3")
            bundle("data", listOf("postgresql", "flyway"))

            library("logging", "io.github.microutils", "kotlin-logging-jvm").version("3.0.5")
        }
        create("integrationTestLibs") {
            library("h2", "com.h2database", "h2").version("2.2.224")
        }
    }
}
