plugins {
    kotlin("jvm") version "1.9.20"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

subprojects {
    apply(plugin = "java")
}

group = "me.honkling.commando"
version = "0.1.0"