package dbrighthd.wildfiregendermodplugin.networking;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.networking.wildfire.*;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author winnpixie
 */
public class NetworkManager {
    private static final Map<Integer, ModSyncPacket> PACKET_FORMATS;

    private final GenderModPlugin plugin;

    private ModSyncPacket packetFormat;

    static {
        PACKET_FORMATS = Map.of(
                1, new ModSyncPacketV1(),
                2, new ModSyncPacketV2(),
                3, new ModSyncPacketV3(),
                4, new ModSyncPacketV4(),
                5, new ModSyncPacketV5());
    }

    public NetworkManager(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        int protocolVersion = plugin.getConfig().getInt("mod.protocol", -1);
        if (protocolVersion == -1) {
            protocolVersion = detectDefaultProtocol();
        }

        packetFormat = PACKET_FORMATS.get(protocolVersion);
        if (packetFormat == null)
            return false;

        plugin.getCustomLogger().info("Using default protocol %d for mod version(s) %s",
                packetFormat.getVersion(), packetFormat.getModRange());

        return true;
    }

    private int detectDefaultProtocol() {
        return detectDefaultProtocol(plugin.getServer().getBukkitVersion());
    }

    static int detectDefaultProtocol(String version) {
        try {
            String versionStr = version.split("-")[0];
            String[] parts = versionStr.split("\\.");
            int major = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

            if (major > 21 || (major == 21 && patch >= 9))
                return 5;
            if (major == 21 && patch >= 2)
                return 4;
            if (major == 20 && patch >= 2)
                return 3;
            if (major >= 18)
                return 2;
        } catch (Exception ignored) {
        }
        return 2;
    }

    static int detectProtocolFromLength(int length) {
        if (length == 36)
            return 2;
        if (length == 49)
            return 3;
        if (length == 70)
            return 4;
        if (length > 70)
            return 5;
        return -1;
    }

    public void sync(Collection<? extends Player> audience) {
        for (ModUser userToSync : plugin.getUserManager().getUsers().values()) {
            Map<Integer, byte[]> fabricCache = new HashMap<>();
            Map<Integer, byte[]> forgeCache = new HashMap<>();

            for (Player recipient : audience) {
                ModSyncPacket format = getPacketFormatForPlayer(recipient.getUniqueId());
                int version = format.getVersion();

                byte[] fabricData = fabricCache.computeIfAbsent(version, v -> serializeUser(userToSync, false, format));
                byte[] forgeData = forgeCache.computeIfAbsent(version, v -> serializeUser(userToSync, true, format));

                if (fabricData != null && fabricData.length > 0)
                    sendData(recipient, ModConstants.SYNC, fabricData);
                if (forgeData != null && forgeData.length > 0)
                    sendData(recipient, ModConstants.FORGE, forgeData);
            }
        }
    }

    public ModSyncPacket getPacketFormatForPlayer(UUID playerId) {
        int version = plugin.getUserManager().getProtocolVersion(playerId);
        if (version == -1) {
            return packetFormat; // Default from config
        }

        ModSyncPacket format = PACKET_FORMATS.get(version);
        if (format == null) {
            plugin.getCustomLogger().warning("Unsupported protocol version %d for %s, falling back to version %d",
                    version, playerId, packetFormat.getVersion());
            return packetFormat;
        }

        return format;
    }

    public ModUser deserializeUser(byte[] data, boolean forge, Player sender) {
        if (plugin.getCustomLogger().isVerbose()) {
            plugin.getCustomLogger().debug("Incoming payload [%s] -> %s",
                    forge ? ModConstants.FORGE : ModConstants.SEND_GENDER_INFO,
                    plugin.getCustomLogger().hexDump(data));
        }

        ModSyncPacket format = getPacketFormatForPlayer(sender.getUniqueId());

        // Dynamic detection if version is unknown
        if (plugin.getUserManager().getProtocolVersion(sender.getUniqueId()) == -1) {
            int len = data.length;
            if (forge)
                len--; // Subtract forge prefix byte

            int detectedVersion = detectProtocolFromLength(len);

            if (detectedVersion != -1) {
                ModSyncPacket candidateFormat = PACKET_FORMATS.get(detectedVersion);

                // Validate detection by attempting to parse UUID
                try (CraftInputStream probe = CraftInputStream.ofBytes(data)) {
                    if (forge)
                        probe.readByte();
                    UUID parsedUuid = new UUID(probe.readLong(), probe.readLong());

                    if (!parsedUuid.equals(sender.getUniqueId())) {
                        plugin.getCustomLogger().warning(
                                "Protocol detection mismatch for %s: parsed UUID %s doesn't match sender, using default",
                                sender.getName(), parsedUuid);
                    } else {
                        plugin.getUserManager().setProtocolVersion(sender.getUniqueId(), detectedVersion);
                        format = candidateFormat;
                        plugin.getCustomLogger().info("Auto-detected protocol V%d for %s (packet length: %d)",
                                detectedVersion, sender.getName(), len);
                    }
                } catch (IOException ex) {
                    plugin.getCustomLogger().warning("Protocol detection failed for %s, using default",
                            sender.getName());
                }
            }
        }
        try (CraftInputStream input = CraftInputStream.ofBytes(data)) {
            if (forge)
                input.readByte();

            ModUser user = format.read(input);
            if (user != null) {
                plugin.getCustomLogger().debug("Successfully deserialized user %s (forge=%s, protocol=%d)",
                        user.userId(), forge, format.getVersion());
            }
            return user;
        } catch (IOException ex) {
            plugin.getCustomLogger().debug("Data malformed during deserialization (forge=%s, protocol=%d)",
                    forge, format.getVersion());
            plugin.getCustomLogger().warning(ex, "Could not deserialize user (forge=%s, protocol=%d)",
                    forge, format.getVersion());
        }

        return null;
    }

    private byte[] serializeUser(ModUser user, boolean forge, ModSyncPacket format) {
        try (ByteArrayOutputStream payload = new ByteArrayOutputStream();
                CraftOutputStream output = new CraftOutputStream(payload)) {
            if (forge)
                output.writeByte(1);

            format.write(user, output);
            return payload.toByteArray();
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not serialize user (forge=%s, protocol=%d)",
                    forge, format.getVersion());
        }

        return new byte[0];
    }

    private void sendData(Player target, String channel, byte[] data) {
        if (plugin.getCustomLogger().isVerbose()) {
            plugin.getCustomLogger().debug("Outgoing payload [%s] -> %s",
                    channel, plugin.getCustomLogger().hexDump(data));
        }
        target.sendPluginMessage(plugin, channel, data);
    }

    /**
     * Sends a dummy sync packet for a fake UUID to a player.
     * Useful for verifying that the client mod is receiving and processing data.
     */
    public void sendTestData(Player target, UUID dummyId) {
        var configOpt = plugin.getUserManager().getUsers().values().stream()
                .findFirst().map(ModUser::configuration);

        if (configOpt.isEmpty()) {
            plugin.getCustomLogger().warning("No mod users stored to use as template for test sync.");
            return;
        }

        ModSyncPacket format = getPacketFormatForPlayer(target.getUniqueId());
        ModUser dummyUser = new ModUser(dummyId, configOpt.get());

        byte[] data = serializeUser(dummyUser, false, format);
        if (data.length > 0) {
            sendData(target, ModConstants.SYNC, data);
            plugin.getCustomLogger().info("Sent dummy sync for %s to %s using protocol %d",
                    dummyId, target.getName(), format.getVersion());
        }
    }
}
