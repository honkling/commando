plugins {
    id("com.gradleup.shadow")
}

tasks.shadowJar {
    archiveClassifier.set(null as String?)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:1d0f512256")
    implementation(project(":common"))
}

kotlin {
    jvmToolchain(21)
}