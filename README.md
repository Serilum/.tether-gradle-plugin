# Tether, the Gradle plugin for Serilum's mods

Tether ties each of my mods to the library mods it needs, like [Collective](https://modrinth.com/mod/collective). It works out which loader a project is for (Common, Fabric, Forge or NeoForge) and which Minecraft version it targets, and adds the right dependencies for that combination. That way every build file stays the same, no matter the loader or version.

The mod itself decides what gets added. A library is added when the code imports it and `Common/src/main/resources/META-INF/mods.toml` lists it as a required dependency. The library's version is the lowest one in that dependency's `versionRange`, so `[8.40,)` means 8.40. When the code imports a library that `mods.toml` doesn't require, the build stops and says so.

The libraries come from [maven.serilum.com](https://maven.serilum.com/), in the `com.serilum.<library>` group. The one exception is Collective for Minecraft 26.3.0 and older, which stays in `com.natamus.collective-ml`.

It is used by the public source of the mods, so you only need it if you want to build one of them yourself.

## Using it

Add the repository to your **settings.gradle**:

```gradle
pluginManagement {
  repositories {
    maven {
      url = "https://maven.serilum.com/"
    }
    gradlePluginPortal()
  }
}
```

Then apply it in a loader's **build.gradle**, after the loader's own plugin:

```gradle
plugins {
  id 'com.serilum.tether' version '1.+'
}
```

`1.+` always picks the newest 1.x release. A change that would break existing builds gets a new major version instead.

## Building it

Tether is compiled for Java 17 with Gradle 8.8, so it also loads in the older 1.20.1 and 1.21.1 workspaces.

```
$ gradlew publishToMavenLocal
```

Bumping the version in **build.gradle** and pushing is all it takes to release one. A GitHub Action builds it and adds it to [maven.serilum.com](https://maven.serilum.com/).
