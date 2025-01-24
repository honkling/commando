plugins {
    id("com.gradleup.shadow") version "8.3.3"
    kotlin("jvm") version "2.0.20"
    `maven-publish`
}

group = "me.honkling.commando"
version = "3.0.0-Build12"

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "maven-publish")
    apply(plugin = "kotlin")
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    dependencies.implementation(dependencies.kotlin("reflect"))

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "me.honkling.commando"
                artifactId = project.name
                version = rootProject.version.toString()

                from(components["java"])
            }
        }
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
}

allprojects {
    repositories.mavenCentral()

    kotlin {
        jvmToolchain(21)
    }

    tasks.build {
        dependsOn("shadowJar", "publishToMavenLocal")
    }
}