import org.gradle.kotlin.dsl.dependencies as dependencies1

plugins {
    kotlin("jvm") version "1.9.22"
}

group = "com.rgarage"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies1 {
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    implementation(platform("org.http4k:http4k-bom:5.18.2.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-server-jetty")
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("com.ubertob.pesticide:pesticide-core:1.6.5")
    testImplementation ("org.http4k:http4k-client-jetty")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}