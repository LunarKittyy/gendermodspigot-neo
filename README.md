# Plugin Info

This is a Spigot plugin that allows clients using [Wildfire's Female Gender Mod](https://modrinth.com/mod/female-gender) to have synced configs when playing on a Spigot server. This plugin was made by me as a member of the community and is not affiliated with the Wildfire's Female Gender Mod.

Wildfire's Female Gender Mod is still required on the client to use the features, all this plugin does is sync the player-specific settings as it would on a Fabric server with the mod installed.

Currently, this plugin only works in syncing the Fabric version of the mod, but I will be trying to fix it in the future for Forge (if possible). This is the first plugin I have ever made so it is not particularly clean, but it works!

Thank you to Flamgop for the help with learning how to make a plugin, and Stigstille + winnpixie for porting it to the latest Spigot version!

Download from Modrinth: https://modrinth.com/plugin/female-gender-spigot

## Build Instructions

1. (Optional) Open the project in your IDE of choice (ie. Eclipse, IntelliJ IDEA, NetBeans, etc)
2. Compile using Maven's `Package` task (or run the command `mvn package` in your terminal).
3. Copy the JAR file from the `target` folder to your server's `plugins` directory.
4. Enjoy synced gender settings!