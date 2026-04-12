# Wildfire's Female Gender Mod - Spigot Plugin

A Spigot plugin that syncs player settings from [Wildfire's Female Gender Mod](https://modrinth.com/mod/female-gender) on Spigot/Paper servers. This project is community-made and is not affiliated with the mod or its developers.

**The client mod is still required.** This plugin replicates the sync behaviour that the mod provides natively on Fabric servers, allowing it to work on Spigot/Paper as well.

> **Note:** Currently only the Fabric version of the mod is supported for syncing. Forge support is not guaranteed.

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

| Protocol | Mod Version   | Minecraft Version (auto-detect) |
|:--------:|:-------------:|:-------------------------------:|
| 2        | 2.8.1 – 3.0.1 | 1.18 – 1.20.1                   |
| 3        | 3.1.0 – 4.0.0 | 1.20.2 – 1.21.1                 |
| 4        | 4.0.0 – 4.3.4 | 1.21.2 – 1.21.8                 |
| 5-legacy | 5.0.0-Beta.1  | 1.21.9 (UNSUPPORTED)            |
| 5        | 5.0.0+        | 1.21.10+                        |

Set to `-1` to automatically select a protocol based on your server's Minecraft version. This does **not** mean all client mod versions are supported simultaneously — the plugin will still use a single protocol determined by the server version (e.g. `1.21.10+` → protocol 5). Make sure your players are all using a client mod version that matches that protocol. If you have players on mixed mod versions, set the protocol explicitly to match whichever version they are all using.
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
- **LunarKittyy** - porting to protocol v5
