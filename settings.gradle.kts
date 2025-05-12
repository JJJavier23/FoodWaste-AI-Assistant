pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        // Optional: predeclare plugin versions
        id("com.android.application") version "8.2.2"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "UI"
include(":app")
