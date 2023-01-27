import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun prop(key: String) = project.findProperty(key).toString()

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.10.1"
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
}

dependencies {
    implementation("io.sentry:sentry:6.12.1")
}

group = prop("pluginId")
version = prop("pluginVersion")

repositories {
    mavenCentral()
}

apply {
    plugin(net.rentalhost.plugins.gradle.ProjectTools::class)
}

intellij {
    pluginName.set(prop("pluginName"))
    version.set(prop("platformVersion"))
    type.set("IU")

    plugins.set(listOf("com.jetbrains.php:${prop("platformPhpBuild")}"))
}

tasks {
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }

    build {
        dependsOn("generatePluginXML")
        dependsOn("generateChangelog")
    }

    wrapper {
        gradleVersion = prop("gradleVersion")
    }
}
