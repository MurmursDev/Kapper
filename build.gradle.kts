plugins {
    id("maven-publish")
    kotlin("jvm") version "1.8.0"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
}

group = "dev.murmurs.kapper"
version = "0.0.1"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")
    implementation("com.squareup:kotlinpoet:1.10.1")
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenKoltin") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
}
