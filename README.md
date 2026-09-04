# Modular Muskets

A Minecraft mod by **Queso**, built for both **NeoForge** and **Fabric** using the
[MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template).

Build your own black-powder musket from mix-and-match parts. Craft a barrel, stock, and
bracing, snap them together at a **Musket Station**, then sneak to aim down the sights and
fire. Different parts change damage, velocity, accuracy, reload, zoom, and grant special
on-hit abilities.

| Property | Value |
| --- | --- |
| Mod ID | `niche` |
| Package | `com.queso.niche` |
| Minecraft | `26.2` |
| Java | `25` |
| Loaders | NeoForge, Fabric |

## Getting started (in-game)

1. Craft a **Musket Station** (iron over planks) and a few **Musket Balls** (iron nugget + gunpowder).
2. Craft the three parts: a **Barrel**, a **Stock**, and a **Bracing**. Recipes unlock in your
   recipe book as soon as you pick up one of their materials.
3. Open the Musket Station, drop in the three parts (and an optional augment), and take out
   your assembled musket. You can drop a finished musket back in to break it into its parts.
4. Hold the musket, **sneak to aim**, **hold** to reload, and **click to fire**.

Barrels can also be found in structure chests (blaze in nether fortresses, prismarine in
shipwrecks/buried treasure, echo in ancient cities, shulker in end cities, and so on).

## Configuration

All tuning lives in a single JSON file at **`config/niche.json`**, relative to your game
(or server/instance) directory. The file is created automatically the first time the mod
loads, and any missing options are re-added with their defaults, so it is always safe to
delete or hand-edit.

Values are read once when the game starts, so **edit the file while the game is closed** and
your changes apply on the next launch.

```json
{
  "recoilScale": 1.0,
  "zoomFactor": 0.4,
  "cameraSway": true,
  "vignetteIntensity": 1.0,
  "abilitiesEnabled": true,
  "tutorialShown": false
}
```

| Option | Type | Default | Range | What it does |
| --- | --- | --- | --- | --- |
| `recoilScale` | number | `1.0` | `0.0`+ | Multiplies all recoil (camera view-kick, gun-model kick, and the little movement bump). `0` disables recoil entirely; `2.0` doubles it. |
| `zoomFactor` | number | `0.4` | `0.1`–`1.0` | Field-of-view multiplier when fully aimed. Lower = more zoom. `1.0` means no zoom at all. Clamped to a minimum of `0.1`. |
| `cameraSway` | boolean | `true` | — | When `true`, holding your aim too long makes the scope drift, so you have to time your shot. Set `false` for a rock-steady scope. |
| `vignetteIntensity` | number | `1.0` | `0.0`–`1.0` | Darkness of the black scope vignette while aiming. `0` turns it off completely, `1` is full strength. |
| `abilitiesEnabled` | boolean | `true` | — | Master switch for barrel abilities (fire, lightning, poison, homing, etc.). Set `false` to make every musket fire plain projectiles. |
| `tutorialShown` | boolean | `false` | — | Client bookkeeping for the one-time "how to use a musket" toast. It flips to `true` on its own after you first hold a musket; set it back to `false` if you want to see the tip again. |

> Server admins: `recoilScale`, `zoomFactor`, `cameraSway`, and `vignetteIntensity` are
> client-feel options and only affect the machine they are set on. `abilitiesEnabled` matters
> on the side that fires the projectile (the server for gameplay).

## Project layout

- `common/` — Loader-agnostic code compiled against vanilla Minecraft. The majority of the
  mod lives here. It cannot reference NeoForge- or Fabric-specific APIs directly; it uses the
  `Services`/`IPlatformHelper` abstraction (Java `ServiceLoader`) to bridge to loader-specific
  behaviour.
- `neoforge/` — NeoForge entry point (`com.queso.niche.Niche`) and platform implementation.
- `fabric/` — Fabric entry point (`com.queso.niche.Niche`) and platform implementation.
- `build-logic/` — Shared Gradle convention plugins (`multiloader-common`, `multiloader-loader`).

## Building

```bash
./gradlew build
```

Loader jars are produced under `neoforge/build/libs/` and `fabric/build/libs/`.

## Running in a dev environment

```bash
./gradlew :fabric:runClient      # launch the Fabric client
./gradlew :neoforge:runClient    # launch the NeoForge client
./gradlew :fabric:runServer      # or :neoforge:runServer
```

In IntelliJ IDEA, refresh the Gradle project and use the generated **Fabric Client /
NeoForge Client** run configurations. Ensure the Gradle JVM and Project SDK are set to a
Java 25 JDK.

## Mod metadata

Mod metadata (name, author, version, Minecraft/loader versions) is centralized in
[`gradle.properties`](gradle.properties). Any new property added there must also be added to
the `expandProps` map in
[`build-logic/src/main/groovy/multiloader-common.gradle`](build-logic/src/main/groovy/multiloader-common.gradle)
so it is available for token expansion in resource files.

## License

CC0-1.0 — see [LICENSE](LICENSE).
