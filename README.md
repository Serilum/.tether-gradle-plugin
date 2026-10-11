# Tether, the Gradle plugin for Serilum's mods

Tether is a small Gradle plugin I made for my own mods. It ties each of them to the library mods it needs, like [Collective](https://modrinth.com/mod/collective). It works out which loader a project is for and which Minecraft version it targets, adds the right dependencies, and makes sure a jar built from the source works next to the mods I release. That way every build file stays the same, no matter the loader or version.

It's built around how my mods are set up, so it isn't meant as a general plugin for other projects. If you want to build one of my mods yourself, there's a short guide further down.

## How it works

The mod itself decides what gets added. A library is added when the code imports it and `Common/src/main/resources/META-INF/mods.toml` lists it as a required dependency. Its version is the lowest one in that dependency's `versionRange`, so `[8.40,)` means 8.40. When the code imports a library that `mods.toml` doesn't require, the build stops and says so.

The libraries come from [maven.serilum.com](https://maven.serilum.com/), in the `com.serilum.<library>` group. The one exception is Collective for Minecraft 26.3.0 and older, which stays in `com.natamus.collective-ml`.

The jars I upload aren't a plain Gradle build. After building, the Fabric, Forge and NeoForge jars are merged into one, and to make that possible Collective's common code is moved into its own package for each loader. So the released Collective has `com.natamus.collective_common_fabric` where the source has `com.natamus.collective`. A jar built straight from the source would still look for the old names, and crash with a `NoClassDefFoundError`.

That's why Tether links the jar once it's built: it points the references to Collective's common code at the package of that loader. Which classes are moved is read from the Collective jars on the maven, so it always matches what's released. Loader-specific classes are left alone, and so are my own release builds.

The build files use version `1.+`, which always picks the newest 1.x release. A change that would break existing builds gets a new major version instead.

## Toggles

Everything is on by default. Each part can be turned off with a line in a `gradle.properties` file:

| Property | What it turns off |
|---|---|
| `tether_enabled=false` | Tether as a whole. |
| `tether_dependencies=false` | Adding the libraries as dependencies. |
| `tether_link=false` | Linking the built jar to the released libraries. |

I use `tether_link=false` myself, in the `gradle.properties` in my Gradle user folder (`~/.gradle`). My test setups run the jar against the plain library jars from the maven, so they need it unlinked. A line there counts for every build on that machine, without ending up in the mods' repositories.

## Building one of my mods

1. Clone the mod's repository, and check out the branch of your Minecraft version.
2. Run `gradlew build` in the folder you cloned.
3. Take the jar for your loader from `Fabric/build/libs`, `Forge/build/libs` or `NeoForge/build/libs`.

When it worked, the build log shows a line like this for every loader:

```
[Tether] Linked treeharvester-Fabric-26.3.0-1.0.jar to the released collective (15 files updated).
```

That jar works with the Collective from CurseForge or Modrinth, and with the rest of my mods. You end up with a separate jar for each loader instead of the single jar I upload, which works just as well.

If you've built the mod before, run it once as `gradlew build --refresh-dependencies`. Gradle remembers which Tether version it used for a day, so otherwise it might still use an older one.

If you've fixed or improved something, feel free to open a pull request! I'd much rather have the change in the mod itself than in a separate jar.

## Adding it to an older branch

Branches for Minecraft versions I no longer update were made before Tether existed. They build fine, but the jar crashes with a `NoClassDefFoundError` next to the released Collective. You can add Tether to them yourself, it only takes two small changes.

**1. settings.gradle**

At the top there's a `pluginManagement` block with a list of `repositories`. Add my maven as the first one:

```gradle
pluginManagement {
    repositories {
        maven { url = 'https://maven.serilum.com/' }
        gradlePluginPortal()
        ...
```

**2. Fabric/build.gradle, Forge/build.gradle and NeoForge/build.gradle**

Each of these starts with a `plugins` block. Add Tether as the last line in it:

```gradle
plugins {
    id 'fabric-loom' version '1.9-SNAPSHOT'
    id 'java'
    id 'idea'
    id 'com.serilum.tether' version '1.+'
}
```

The other lines differ per loader and version, just leave them as they are. You don't have to change Common's build.gradle, and the Collective lines that are already in the build files can stay too.

Then build it the same way as above, with `gradlew build`.

## Building Tether

Tether is compiled for Java 17 with Gradle 8.8, so it also loads in the older 1.20.1 and 1.21.1 workspaces.

```
$ gradlew publishToMavenLocal
```

Bumping the version in **build.gradle** and pushing is all it takes to release one. A GitHub Action builds it and adds it to [maven.serilum.com](https://maven.serilum.com/).
