package dbrighthd.wildfiregendermodplugin;

import dbrighthd.wildfiregendermodplugin.gender.GenderData;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.protocol.wildfire.GenderSyncPacket;
import dbrighthd.wildfiregendermodplugin.protocol.wildfire.GenderSyncPacketV2;
import dbrighthd.wildfiregendermodplugin.protocol.wildfire.GenderSyncPacketV3;
import dbrighthd.wildfiregendermodplugin.protocol.wildfire.GenderSyncPacketV4;
import dbrighthd.wildfiregendermodplugin.utilities.Constants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GenderPlugin extends JavaPlugin implements PluginMessageListener, Listener {
    private final Map<UUID, GenderData> dataStorage = new HashMap<>();

    private GenderSyncPacket syncPacket;

    @Override
    public void onEnable() {
        getLogger().info("By @dbrighthd, with contributions from @stigstille and @winnpixie");

        saveDefaultConfig();

        int protocolVersion = getConfig().getInt("mod.protocol", 4); // "4" is latest as of 20/Jul/2025
        syncPacket = switch (protocolVersion) {
            case 2 -> new GenderSyncPacketV2();
            case 3 -> new GenderSyncPacketV3();
            case 4 -> new GenderSyncPacketV4();
            default -> null;
        };

        if (syncPacket == null) {
            getLogger().severe("Unknown Protocol Version! Disabling self.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Using Protocol V%d for mod version(s) %s".formatted(syncPacket.getVersion(), syncPacket.getModRange()));

        // Handle syncing for new joins, and removing unused data from offline players.
        this.getServer().getPluginManager().registerEvents(this, this);

        // Fabric
        this.getServer().getMessenger().registerIncomingPluginChannel(this, Constants.SEND_GENDER_INFO, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, Constants.SYNC);

        // Forge
        this.getServer().getMessenger().registerIncomingPluginChannel(this, Constants.FORGE, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, Constants.FORGE);
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        String playerName = player.getName();
        getLogger().info("Received message from %s".formatted(playerName));

        // Save player's gender data to memory.
        if (channel.equals(Constants.SEND_GENDER_INFO) || channel.equals(Constants.FORGE)) {
            getLogger().info("Channel verified for %s".formatted(playerName));

            try (CraftInputStream craftInput = new CraftInputStream(message)) {
                if (channel.equals(Constants.FORGE)) {
                    getLogger().info("%s is using FORGE".formatted(playerName));

                    try {
                        // Forge version contains an extra byte
                        craftInput.readByte();
                    } catch (IOException ex) {
                        getLogger().severe("Could not read FORGE header from %s".formatted(playerName));
                        ex.printStackTrace();

                        // Early return to prevent potentially malformed data storage.
                        return;
                    }
                }

                try {
                    GenderData data = syncPacket.read(craftInput);
                    dataStorage.put(data.uuid, data);

                    getLogger().info("Stored GenderData for %s(%s) (%s)".formatted(data.uuid, playerName, data.gender.name()));
                } catch (IOException ex) {
                    getLogger().severe("Error storing GenderData for %s".formatted(playerName));
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Sync NEW/MODIFIED gender data to ALL online players.
        dataStorage.forEach((uuid, data) -> {
            getLogger().info("Sending GenderData from %s(%s) to ALL".formatted(data.uuid, data.gender.name()));

            ByteArrayOutputStream fabricPayload = new ByteArrayOutputStream();
            CraftOutputStream fabricStream = new CraftOutputStream(fabricPayload);
            try {
                syncPacket.write(data, fabricStream);
            } catch (IOException ex) {
                getLogger().severe("Error encoding data");
                ex.printStackTrace();

                // Nullify encoder so it won't be used.
                fabricStream = null;
            }

            ByteArrayOutputStream forgePayload = new ByteArrayOutputStream();
            CraftOutputStream forgeStream = new CraftOutputStream(forgePayload);
            try {
                forgeStream.writeByte(1);
                syncPacket.write(data, forgeStream);
            } catch (IOException ex) {
                getLogger().severe("Error encoding data[FORGE]");
                ex.printStackTrace();

                // Nullify encoder so it won't be used.
                forgeStream = null;
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (fabricStream != null) sendDataToPlayer(onlinePlayer, Constants.SYNC, fabricPayload.toByteArray());
                if (forgeStream != null) sendDataToPlayer(onlinePlayer, Constants.FORGE, forgePayload.toByteArray());
            }

            // FIXME: Investigate why this causes de-sync for new players.
            // genderData.needsSync = false;
        });
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        // Send ALL existing gender data to the newly joined player. !! IGNORES SYNC STATUS !!
        dataStorage.forEach((uuid, data) -> {
            getLogger().info("Sending gender data from %s(%s) to %s".formatted(data.uuid, data.gender.name(), playerName));

            try (ByteArrayOutputStream fabricPayload = new ByteArrayOutputStream();
                 CraftOutputStream fabricStream = new CraftOutputStream(fabricPayload)) {
                syncPacket.write(data, fabricStream);
                sendDataToPlayer(player, Constants.SYNC, fabricPayload.toByteArray());
            } catch (IOException ex) {
                getLogger().severe("Error sending data to %s".formatted(playerName));
                ex.printStackTrace();
            }

            try (ByteArrayOutputStream forgePayload = new ByteArrayOutputStream();
                 CraftOutputStream forgeStream = new CraftOutputStream(forgePayload)) {
                forgeStream.writeByte(1);
                syncPacket.write(data, forgeStream);
                sendDataToPlayer(player, Constants.FORGE, forgePayload.toByteArray());
            } catch (IOException ex) {
                getLogger().severe("Error sending data[FORGE] to %s".formatted(playerName));
                ex.printStackTrace();
            }
        });
    }

    private void sendDataToPlayer(Player player, String channel, byte[] data) {
        player.sendPluginMessage(this, channel, data);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        getLogger().info("Removing gender data for %s(%s)".formatted(uuid, player.getName()));
        dataStorage.remove(uuid);  // Remove gender data for a player who is no longer online.
    }
}
