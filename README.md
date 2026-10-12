# Tether, the Gradle plugin for Serilum's mods

Tether is a small Gradle plugin I made for my own mods. It adds the library mods they need, like [Collective](https://modrinth.com/mod/collective), and makes sure a jar built from the source works next to the mods I release. It's built around how my mods are set up, so it isn't meant as a general plugin.

## How it works

A library is added when the code imports it and `Common/src/main/resources/META-INF/mods.toml` requires it, at the lowest version in its `versionRange`. If the code imports a library that `mods.toml` doesn't require, the build stops and says so. The libraries come from [maven.serilum.com](https://maven.serilum.com/).

The jars I upload merge all loaders into one, so Collective's common code is moved into a package per loader there. Tether links a jar built from the source to those packages. Without that, it would crash with a `NoClassDefFoundError`.

## Toggles

Everything is on by default. Each part can be turned off with a line in a `gradle.properties` file:

| Property | What it turns off |
|---|---|
| `tether_enabled=false` | Tether as a whole. |
| `tether_dependencies=false` | Adding the libraries as dependencies. |
| `tether_link=false` | Linking the built jar to the released libraries. |

## Building one of my mods

1. Clone the mod's repository, and check out the branch of your Minecraft version.
2. Run `gradlew build` in the folder you cloned.
3. Take the jar for your loader from `Fabric/build/libs`, `Forge/build/libs` or `NeoForge/build/libs`.

When it worked, the log shows a `[Tether] Linked ... to the released collective` line for each loader.

If you've built the mod before, run it once as `gradlew build --refresh-dependencies` to get the newest Tether.

If you've fixed or improved something, feel free to open a pull request!

## Adding it to an older branch

Branches made before Tether existed build fine, but the jar crashes next to the released Collective. Add my maven as the first repository in `pluginManagement` in `settings.gradle`:

```gradle
maven { url = 'https://maven.serilum.com/' }
```

And add Tether as the last line of the `plugins` block in the Fabric, Forge and NeoForge `build.gradle` files. Common's `build.gradle` can stay as it is:

```gradle
id 'com.serilum.tether' version '1.+'
```

## Building Tether

`gradlew publishToMavenLocal` builds it locally. Bumping the version in `build.gradle` and pushing releases it to the maven.
