import java.util.*

plugins {
    id("java")
    id("jacoco")
}

group = "cloud.marton.hostup_dns_client"
version = providers.gradleProperty("version").get()

// Produce a release version without SNAPSHOT via -PreleaseVersion
val releaseVersion = providers.gradleProperty("releaseVersion")!!
if (releaseVersion.isPresent) {
    version = version.toString().removeSuffix("-SNAPSHOT")
    println("Using release version $version")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.dslplatform:dsl-json:2.0.2")
    annotationProcessor("com.dslplatform:dsl-json:2.0.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.wiremock:wiremock:3.13.2")

    testImplementation("org.slf4j:slf4j-api:2.0.17")
    testImplementation("ch.qos.logback:logback-classic:1.5.23")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val jacocoReportDir = layout.buildDirectory.dir("reports/jacoco")!!
val jacocoExcludedClasses = listOf("**/*DslJson*.class")

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(jacocoExcludedClasses)
        }
    )
    reports {
        xml.required.set(true)
        xml.outputLocation.set(jacocoReportDir.map { it.file("jacocoTestReport.xml") })
        html.required.set(true)
        html.outputLocation.set(jacocoReportDir.map { it.dir("html") })
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    classDirectories.setFrom(
        sourceSets.main.get().output.asFileTree.matching {
            exclude(jacocoExcludedClasses)
        }
    )
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "cloud.marton.hostup_dns_client.Main",
                "Implementation-Version" to version
            )
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith(".jar") }
            .map { zipTree(it) }
    })
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Simple patch bump when run explicitly: ./gradlew bumpPatchVersion
val bumpPatchVersion by tasks.registering {
    group = "versioning"
    description = "Bumps patch in gradle.properties version (only when run explicitly)."
    doLast {
        val propsFile = rootProject.file("gradle.properties")
        val props = Properties().apply { propsFile.inputStream().use { load(it) } }
        val current = props.getProperty("version") ?: error("version not set in gradle.properties")
        val parts = current.removeSuffix("-SNAPSHOT").split('.')
        require(parts.size == 3) { "Version must be major.minor.patch" }
        val (maj, min, pat) = parts.map { it.toInt() }
        val next = "$maj.$min.${pat + 1}-SNAPSHOT"
        props.setProperty("version", next)
        propsFile.outputStream().use { props.store(it, "Updated by bumpPatchVersion") }
        println("Version bumped to $next")
    }
}
