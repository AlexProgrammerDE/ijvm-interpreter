plugins {
    base
    java
    idea
    id("com.diffplug.spotless") version "8.2.1"
    id("net.ltgt.errorprone") version "5.0.0"
    id("com.github.spotbugs") version "6.4.8"
    id("org.openrewrite.rewrite") version "7.25.0"
}

tasks.named<UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = JavaLanguageVersion.of(25)
}

spotless {
    java {
        trimTrailingWhitespace()
        leadingTabsToSpaces(2)
        endWithNewline()
    }
}

rewrite {
    activeRecipe("org.openrewrite.staticanalysis.CommonStaticAnalysis")
    activeRecipe("org.openrewrite.staticanalysis.CodeCleanup")
    activeRecipe("org.openrewrite.staticanalysis.JavaApiBestPractices")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava25")
    isExportDatatables = true
}

spotbugs {
    ignoreFailures = true
}

repositories {
    mavenCentral()
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.47.0")
    spotbugs("com.github.spotbugs:spotbugs:4.9.8")

    rewrite("org.openrewrite.recipe:rewrite-static-analysis:2.26.0")
    rewrite("org.openrewrite.recipe:rewrite-migrate-java:3.26.0")
    rewrite("org.openrewrite.recipe:rewrite-rewrite:0.19.0")
}

testing {
    suites {
        @Suppress("UnstableApiUsage")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter("5.10.3")
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
