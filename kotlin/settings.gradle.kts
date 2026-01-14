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
        maven {
            url = uri("https://repo.danubetech.com/repository/maven-public/")
        }
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://jitpack.io")
                }
            }
            filter {
                includeGroup("com.github.multiformats")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "INJI VCI Client"
//include(":example")
include(":vci-client")
