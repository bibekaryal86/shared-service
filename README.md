# shared-service

This will be the common module shared across repositories with shared services.

This module supports the following shared services:

* AppEnvProperty.java: Retrieves and manages dynamic application variables retrieved from an API
* Connector.java: OkHttp client helper utility
* Email.java: Utility to send email using Mailjet. Because of required Mailjet secrets, Mailjet client is required
  parameter

This will be published to Maven Central Repository after which it can be used as dependency in any project.

An account has been setup in Maven Central Repository (https://central.sonatype.com/publishing/namespaces) to publish to
the repo so that it can be used as a dependency in other services. SonaType does not support direct gradle publishing
without third party plugins, so currently this is a manual process. But next step is to utilize their Publishing API to
publish new versions manually.

### Publishing Process

* `./gradlew buildAndPublish` task command builds and creates the zip file to upload to SonaType to publish
* The task builds, creates and packages javadoc, packages sources and application jar, signs, applies gpg key
* gpg key details are added in `gradle.properties`
* In sonatype website
  * Click the button that says `Publish Component`
    * Deployment Name: `io.github.bibekaryal86`
    * Upload Your File: Select the `.zip` file from `buildAndPublish` output
    * Click `Publish Component` on the modal
    * That takes it to the Validation stage, which takes less than a minute to be validated
    * Once validated, click on the `Publish` button to actually publish it
