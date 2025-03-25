plugins {
    id("java")
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.17.1"
}

group = "com.yourcompany"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Налаштування Java Toolchain для версії 21
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.javaparser:javaparser-core:3.25.1")
}

intellij {
    version.set("2023.3")
    type.set("IU") // IntelliJ IDEA Ultimate

    plugins.set(listOf(
            "com.intellij.java"
    ))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileJava {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("234")
    }
}