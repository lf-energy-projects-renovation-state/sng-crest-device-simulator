// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-simulator"

include("application")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinLogging", "7.0.0")
            version("mockitoKotlin", "5.3.1")
            version("californium", "3.8.0")
            version("commons-codec", "1.17.0")

            library("californium-core", "org.eclipse.californium", "californium-core").versionRef("californium")
            library("californium-scandium", "org.eclipse.californium", "scandium").versionRef("californium")
            bundle("californium", listOf("californium-core", "californium-scandium"))

            library("logging", "io.github.oshai", "kotlin-logging-jvm").versionRef("kotlinLogging")

            library("commonsCodec", "commons-codec", "commons-codec").versionRef("commons-codec")

            library(
                "mockitoKotlin",
                "org.mockito.kotlin",
                "mockito-kotlin"
            ).versionRef("mockitoKotlin")
        }
    }
}
