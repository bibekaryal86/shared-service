# shared-service

This will be the common module shared across repositories with shared services.

This module supports the following shared services:

* AppEnvProperty.java: Retrieves and manages dynamic application variables retrieved from an API
* Connector.java: OkHttp client helper utility
* Email.java: Utility to send email using Mailjet. Because of required Mailjet secrets, Mailjet client is required
  parameter

This will be published to Maven Central Repository after which it can be used as dependency in any project.

GitHub Repository: https://github.com/bibekaryal86/shared-service

Follow these steps (to be simplified/automated later) to generate the files required to publish to Maven Central
* ./gradlew clean
* ./gradlew jar
* ./gradlew sourcesJar
* ./gradlew javadocJar
* ./gradlew signArchives
* ./gradlew generatePomFileForMavenPublication
* ABOVE ALL REPLACED BY SINGLE ./gradlew buildAndPublish

