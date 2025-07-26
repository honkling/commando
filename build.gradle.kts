plugins {
    id("com.gradleup.shadow") version "8.3.3"
    kotlin("jvm") version "2.0.20"
    `maven-publish`
    signing
    id("com.gradleup.nmcp.aggregation") version "1.0.1"
    `java-library`
}

group = "me.honkling.commando"
version = "3.0.3"

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "com.gradleup.nmcp")
    apply(plugin = "maven-publish")
    apply(plugin = "java-library")
    apply(plugin = "kotlin")
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        compileOnly(kotlin("stdlib"))
        compileOnly(kotlin("reflect"))
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }
}

allprojects {
    apply(plugin = "signing")
    repositories.mavenCentral()

    kotlin {
        jvmToolchain(21)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "io.github.honkling.commando"
                artifactId = project.name
                version = rootProject.version.toString()
                from(components["java"])

                pom {
                    name = "commando"
                    url = "https://github.com/honkling/commando"
                    description = "A magic Kotlin command framework."

                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/license/mit/"
                        }
                    }

                    developers {
                        developer {
                            id = "honkling"
                            name = "rosalyn"
                            url = "https://github.com/honkling"
                        }
                    }

                    scm {
                        connection = "scm:git:git://github.com/honkling/commando.git"
                        developerConnection = "scm:git:ssh://github.com:honkling/commando.git"
                        url = "https://github.com/honkling/commando"
                    }
                }
            }
        }
    }

    signing {
        val privateKey = System.getenv("GPG_PRIVATE_KEY")
        val passphrase = System.getenv("GPG_PASSWORD")
        useInMemoryPgpKeys(privateKey, passphrase)

        sign(publishing.publications["maven"])
    }

    tasks.build {
        dependsOn("shadowJar", "publishToMavenLocal")
    }

//    tasks.register<Checksum>("createChecksums") {
//        inputFiles.setFrom(configurations.archives.get().allArtifacts)
//        outputDirectory.set(layout.buildDirectory.asFile.get())
//        checksumAlgorithm.set(Checksum.Algorithm.MD5)
//    }
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        // or if you want to publish automatically
        publishingType = "AUTOMATIC"
    }

    dependencies {
        subprojects {
            nmcpAggregation(project)
        }
    }
}

//nexusPublishing {
//    useStaging = true
//    packageGroup = "io.github.honkling"
//
//    repositories.sonatype {
//        nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
//        snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
//
//        if (System.getenv("SONATYPE_USERNAME") != null) {
//            username.set(System.getenv("SONATYPE_USERNAME"))
//            password.set(System.getenv("SONATYPE_PASSWORD"))
//        }
//    }
//}