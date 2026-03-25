# Wildfire's Female Gender Mod - Spigot Plugin

A Spigot plugin that syncs player settings from [Wildfire's Female Gender Mod](https://modrinth.com/mod/female-gender) on Spigot/Paper servers. This project is community-made and is not affiliated with the mod or its developers.

**The client mod is still required.** This plugin replicates the sync behaviour that the mod provides natively on Fabric servers, allowing it to work on Spigot/Paper as well.

> **Note:** Currently only the Fabric version of the mod is supported for syncing. Forge support is not guaranteed.

Original repo: https://github.com/dbrighthd/gendermodspigot 

---

## Installation

1. Download the latest JAR from the [Releases page](../../releases).
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

| Protocol | Mod Version   |
|:--------:|:-------------:|
| 2        | 2.8.1 – 3.0.1 |
| 3        | 3.1.0 – 4.0.0 |
| 4        | 4.0.0 – 4.3.4 |
| 5        | 5.0.0+        |

Set to `-1` to automatically use the newest known protocol - recommended if you want to avoid updating this manually on every mod update.

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

## Credits

Originally created by **[dbrighthd](https://github.com/dbrighthd)**.

- **Flamgop** — help with the original plugin development
- **Stigstille** & **winnpixie** - porting to latest Spigot version
- **LunarKittyy** - porting to protocol v5
