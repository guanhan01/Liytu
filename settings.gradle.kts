pluginManagement {
    repositories {
        google()
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
rootProject.name = "Liytu"
include(":app")
include(":core-ui")
include(":core-media")
include(":data")
include(":feature-home")
include(":feature-music")
include(":feature-video")
include(":feature-books")
include(":feature-comics")
include(":feature-mine")
