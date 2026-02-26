pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
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

rootProject.name = "AICC"

include(":app")
include(":core:model")
include(":core:database")
include(":core:network")
include(":core:ui")
include(":data:api")
include(":data:recording")
include(":feature:callplan")
include(":feature:calling")
include(":feature:contacts")
include(":feature:settings")
