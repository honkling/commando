plugins {
    kotlin("jvm") version "1.9.20"
}

group = "me.honkling.commando"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}