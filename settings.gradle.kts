pluginManagement {
    repositories {
        google()
        mavenCentral() // Ensure this is present
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Kkarhua"
include(":app")

 