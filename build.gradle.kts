import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.8.0"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
    id("org.jetbrains.dokka") version "1.8.10"
}

group = "dev.murmurs.kapper"
version = "0.0.1-alpha"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")
    implementation("com.squareup:kotlinpoet:1.10.1")
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
}

java {
    withJavadocJar()
    withSourcesJar()
}

val javadocTask = tasks.named<Javadoc>("javadoc").get()

tasks.withType<DokkaTask> {
    javadocTask.dependsOn(this)
    outputDirectory.set(javadocTask.destinationDir)
}

publishing {
    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            repositories {
                maven {
                    name = "OSSRH"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = System.getenv("OSSRH_USERNAME")
                        password = System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
            pom {
                name.set("Kapper")
                description.set("Kotlin annotation processor for mapping objects")
                url.set("https://github.com/MurmursDev/Kapper")
                licenses {
                    license {
                        name.set("GNU General Public License v3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                    }
                }
                developers {
                    developer {
                        id.set("murmursdev")
                        name.set("Murmurs Dev")
                        email.set("murmursdev@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/MurmursDev/Kapper.git")
                    developerConnection.set("scm:git:ssh://github.com/MurmursDev/Kapper.git")
                    url.set("https://github.com/MurmursDev/Kapper")
                }
            }
        }

    }
}

signing {
    sign(publishing.publications["mavenKotlin"])
}
