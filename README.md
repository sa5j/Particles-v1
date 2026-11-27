# Particle Customizer (Fabric 26.2)

A monochrome (black, gray, white) Fabric client mod for Minecraft **26.2** that lets you customize particle types and spawn multipliers via a configurable keybind and in-game GUI.

## Features
- Swap particle types (for example, `minecraft:crit` -> `minecraft:flame`) and adjust spawn count multipliers.
- Bind the GUI opener key in **Options -> Controls -> Key Binds** (default: `P`).
- Uses Minecraft's default font with a black, gray, and white theme.

## Building on GitHub Actions
Push this repository to GitHub and open the **Actions** tab. The included `.github/workflows/build.yml` workflow compiles the mod with JDK 25 and Gradle 9.5.1 and attaches the `.jar` under **Artifacts** (`particlecustomizer-mod-jar`).
