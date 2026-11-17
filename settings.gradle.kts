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

rootProject.name = "UniversalPiano"

// ETAPA 2: apenas os módulos que já têm código.
// Os demais (audio, keyboard, instruments, midi, recorder, compatibility, database)
// existem como pastas e serão incluídos aqui nas etapas correspondentes.
include(":app")
include(":ui")
include(":settings")
