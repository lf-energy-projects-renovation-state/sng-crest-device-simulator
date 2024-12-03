// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

rootProject.name = "sng-crest-device-simulator"

include("application")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("californium", "3.12.1")
            version("kotlinLogging", "7.0.3")
            version("commons-codec", "1.17.1")

            // https://projects.eclipse.org/projects/iot.californium
            library("californium-core", "org.eclipse.californium", "californium-core").versionRef("californium")
            library("californium-scandium", "org.eclipse.californium", "scandium").versionRef("californium")
            bundle("californium", listOf("californium-core", "californium-scandium"))

            // https://github.com/oshai/kotlin-logging/releases
            library("logging", "io.github.oshai", "kotlin-logging-jvm").versionRef("kotlinLogging")

            library("commonsCodec", "commons-codec", "commons-codec").versionRef("commons-codec")

        }
      create("testLibs") {
          version("mockitoKotlin", "5.4.0")

          // https://github.com/mockito/mockito-kotlin/releases
          library(
              "mockitoKotlin",
              "org.mockito.kotlin",
              "mockito-kotlin"
          ).versionRef("mockitoKotlin")
      }
    }
}
