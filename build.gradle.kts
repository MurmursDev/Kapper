import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    signing
    kotlin("jvm") version "1.8.0"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
    id("org.jetbrains.dokka") version "1.8.10"
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
}

group = "dev.murmurs.kapper"
version = "0.1.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")
    api("com.squareup:kotlinpoet:1.10.1")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.5.0")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.5.0")
    testImplementation("com.google.guava:guava:31.0.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.0")
    testImplementation("com.facebook:ktfmt:0.43")

}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
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

nexusPublishing {
    repositories {
        create("myNexus") {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME")) // defaults to project.properties["myNexusUsername"]
            password.set(System.getenv("OSSRH_PASSWORD")) // defaults to project.properties["myNexusPassword"]
        }
    }
}

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project

    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    sign(publishing.publications["mavenKotlin"])
}
