pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven("https://jitpack.io")   // ⭐ JitPack (KTS syntax)

        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")   // ⭐ JitPack (KTS syntax)

    }
}

rootProject.name = "New Human Body"
include(":app")
include(":sticker")
//include(":Roozi")
//project(":Roozi").projectDir = file("Roozi")