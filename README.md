# Plugin Info

This is a spigot plugin that allows clients that use the Female Gender Mod to have synced configs when playing on a spigot server. This plugin was made by me as a member of the community and is not affiliated with the Wildfire's Female Gender Mod.

Wildfire's Female Gender Mod is still required on the client to use the features (download from https://modrinth.com/mod/female-gender ), all this plugin does is sync the player-specific settings (as it would on a fabric server with the mod installed).

Currently this plugin only works in syncing the fabric versions of the mod, but I will be trying to fix it in the future for forge versions (if its possible). This is the first plugin I have ever made so it is not particularly clean, but it works

Thank you to Flamgop for the help with learning how to make a plugin, and Stigstille for porting it to the latest version!

Download from modrinth: https://modrinth.com/plugin/female-gender-spigot

# Build instuctions

1. Download BuildTools from Spigot's Jenkins: [https://hub.spigotmc.org/jenkins/job/BuildTools/](https://hub.spigotmc.org/jenkins/job/BuildTools/)
2. From the command line, run the following command (or similar) to generate the remapped jar 
```powershell
java -Xms512M -jar E:\CSys\Downloads\BuildTools.jar --nogui --rev 1.20.4 --remapped --generate-source --compile NONE
```
3. Use your IDE to download required plugins and dependancies
4. Compile using ***MAVEN'S*** built in compiler