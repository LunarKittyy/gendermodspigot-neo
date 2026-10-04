# Wildfire's Female Gender Mod - Spigot Plugin

A Spigot/Paper plugin that syncs player settings from [Wildfire's Female Gender Mod](https://modrinth.com/mod/female-gender) on Spigot/Paper servers. This project is community-made and is not affiliated with the mod or its developers.

**The client mod is still required.** This plugin replicates the sync behaviour that the mod provides natively on Fabric servers, allowing it to work on Spigot/Paper as well.

> **Note:** Currently only the Fabric version of the mod is supported for syncing. Forge/NeoForge support is not guaranteed.

> **Mod 5.0.0 and newer (protocol 6) needs a [Paper](https://papermc.io) server** (or a fork of Paper, like Purpur). The mod now does its handshake during the configuration phase, which Spigot doesn't let plugins take part in. Older protocols still work on Spigot.

Original repo: https://github.com/dbrighthd/gendermodspigot 

---

## Installation

1. Download the latest JAR from the [Releases page](../../releases). Choose latest stable. If that has issues, try the latest dev release.
2. Place the JAR in your server's `plugins/` directory.
3. Start or restart your server.
4. Configure the plugin (see below), then reload or restart again.

---

## Building from Source

1. Clone the repository.
2. Run `mvn package` in the project root (or use your IDE's Maven `Package` task).
3. The compiled JAR will be in the `target/` folder.

---

## Configuration

### `protocol`

Controls which packet format the plugin uses to communicate with the client mod. Set this to match the version of Wildfire's Female Gender Mod your players are using.

| Protocol | Mod Version                   | Minecraft Version (auto-detect) | Server        |
|:--------:|:-----------------------------:|:-------------------------------:|:-------------:|
| 2        | 2.8.1 – 3.0.1                 | 1.18 – 1.20.1                   | Spigot/Paper  |
| 3        | 3.1.0 – 4.0.0                 | 1.20.2 – 1.21.1                 | Spigot/Paper  |
| 4        | 4.0.0 – 4.3.4                 | 1.21.2 – 1.21.8                 | Spigot/Paper  |
| 5        | 5.0.0-Beta.1 – 5.0.0-Beta.4   | 1.21.9 – 1.21.11                | Spigot/Paper  |
| 6        | 5.0.0-Beta.5+ (incl. 5.0.0)   | 26.1+                           | Paper only    |

Minecraft moved to a `YEAR.RELEASE` version scheme starting with `26.1` in 2026, replacing the old `1.21.x` numbering.

Set to `-1` to pick the protocol based on your server's Minecraft version. The plugin speaks one protocol at a time, so everyone needs a mod version from the same row. If your players are still on an older mod beta on a 26.x server (e.g. 5.0.0-Beta.4), set `protocol: 5` explicitly, or have them update.
`1.21.9` uses an incomplete proto 5 implementation and will not be officially supported.

### `debug`

Set to `true` to enable debug-level logging in the server console. **This is required when submitting a bug report.**

---

## Reporting Issues

Before opening an issue:

- Make sure you're on the **latest release**.
- Check that your `protocol` value matches your client mod version (or use `-1`).
- **Enable `debug: true` in the plugin config** and reproduce the issue, then include the output in your report.

Use the issue templates provided in this repository:
- [**Bug Report**](https://github.com/LunarKittyy/gendermodspigot-neo/issues/new?template=bug_report.md) - for unexpected behaviour or errors
- [**Feature Request**](https://github.com/LunarKittyy/gendermodspigot-neo/issues/new?template=feature_request.md) - for suggestions or improvements

---

## Known incompatibilities

- ViaVersion support is limited. If it works, great. If it breaks it breaks and might not be fixed. 

## Credits

Originally created by **[dbrighthd](https://github.com/dbrighthd)**.

- **Flamgop** — help with the original plugin development
- **Stigstille** & **winnpixie** - porting to latest Spigot version
- **LunarKittyy** - porting to protocols v5 and v6
