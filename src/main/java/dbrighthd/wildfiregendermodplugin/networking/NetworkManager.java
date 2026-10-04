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
import java.util.Map;

/**
 * @author winnpixie
 */
public class NetworkManager {
    private static final Map<Integer, ModSyncPacket> PACKET_FORMATS;

    /**
     * The lowest protocol version with a working implementation.
     * Protocol 1 (ModSyncPacketV1) is a stub that throws
     * {@link UnsupportedOperationException} from read/write.
     */
    private static final int MIN_IMPLEMENTED_PROTOCOL = 2;

    private final GenderModPlugin plugin;

    private ModSyncPacket packetFormat;

    static {
        PACKET_FORMATS = Map.of(
                1, new ModSyncPacketV1(),
                2, new ModSyncPacketV2(),
                3, new ModSyncPacketV3(),
                4, new ModSyncPacketV4(),
                5, new ModSyncPacketV5(),
                6, new ModSyncPacketV6());
    }

    public NetworkManager(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        int protocolVersion = plugin.getConfig().getInt("mod.protocol", -1);
        if (protocolVersion == -1) {
            protocolVersion = detectDefaultProtocol();
        }

        if (!isImplementedProtocol(protocolVersion))
            return false;

        packetFormat = PACKET_FORMATS.get(protocolVersion);

        plugin.getCustomLogger().info("Using protocol %d for mod version(s) %s",
                packetFormat.getVersion(), packetFormat.getModRange());

        return true;
    }

    public int getProtocolVersion() {
        return packetFormat.getVersion();
    }

    /**
     * Whether {@code version} has a usable (non-stub) packet format registered.
     */
    static boolean isImplementedProtocol(int version) {
        return version >= MIN_IMPLEMENTED_PROTOCOL && PACKET_FORMATS.containsKey(version);
    }

    private int detectDefaultProtocol() {
        return detectDefaultProtocol(plugin.getServer().getBukkitVersion());
    }

    static int detectDefaultProtocol(String version) {
        try {
            String versionStr = version.split("-")[0];
            String[] parts = versionStr.split("\\.");
            int epoch = Integer.parseInt(parts[0]);

            if (epoch == 1) {
                // Legacy "1.MINOR.PATCH" scheme (up through 1.21.x).
                int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

                if (minor > 21 || (minor == 21 && patch >= 9))
                    return 5;
                if (minor == 21 && patch >= 2)
                    return 4;
                if (minor == 20 && patch >= 2)
                    return 3;
                return 2;
            }

            // New "YY.RELEASE[.PATCH]" scheme introduced with Minecraft 26.1 (2026+).
            // Mod 5.0.0 (stable) is the first release where these versions have a
            // non-beta build, and it speaks protocol 6.
            if (epoch >= 26)
                return 6;
        } catch (Exception ignored) {
        }
        return 2;
    }

    /**
     * The channels sync payloads are received on for the active protocol.
     */
    public String[] getIncomingSyncChannels() {
        if (packetFormat.getVersion() >= 6)
            return new String[] { ModConstants.V6_SYNC_SERVERBOUND };
        return new String[] { ModConstants.SEND_GENDER_INFO, ModConstants.FORGE };
    }

    /**
     * The channels sync payloads are sent on for the active protocol.
     */
    public String[] getOutgoingSyncChannels() {
        if (packetFormat.getVersion() >= 6)
            return new String[] { ModConstants.V6_SYNC_CLIENTBOUND };
        return new String[] { ModConstants.SYNC, ModConstants.FORGE };
    }

    public void sync(Collection<? extends Player> audience) {
        boolean v6 = packetFormat.getVersion() >= 6;

        for (ModUser userToSync : plugin.getUserManager().getUsers().values()) {
            byte[] fabricData = null;
            byte[] forgeData = null;

            for (Player recipient : audience) {
                // Skip recipients that haven't confirmed they speak our protocol yet.
                if (!plugin.getUserManager().isProtocolReady(recipient.getUniqueId()))
                    continue;

                // The 5.0.0 client logs a warning for every packet about itself.
                if (v6 && recipient.getUniqueId().equals(userToSync.userId()))
                    continue;

                if (v6) {
                    if (fabricData == null)
                        fabricData = serializeUser(userToSync, false);
                    if (fabricData.length > 0)
                        sendData(recipient, ModConstants.V6_SYNC_CLIENTBOUND, fabricData);
                    continue;
                }

                if (fabricData == null) {
                    fabricData = serializeUser(userToSync, false);
                    forgeData = serializeUser(userToSync, true);
                }

                if (fabricData.length > 0)
                    sendData(recipient, ModConstants.SYNC, fabricData);
                if (forgeData.length > 0)
                    sendData(recipient, ModConstants.FORGE, forgeData);
            }
        }
    }

    public ModUser deserializeUser(byte[] data, boolean forge, Player sender) {
        if (plugin.getCustomLogger().isVerbose()) {
            plugin.getCustomLogger().debug("Incoming payload from %s (forge=%s) -> %s",
                    sender.getName(), forge, plugin.getCustomLogger().hexDump(data));
        }

        try (CraftInputStream input = CraftInputStream.ofBytes(data)) {
            if (forge)
                input.readByte();

            ModUser user = packetFormat.readFromClient(sender.getUniqueId(), input);
            if (input.available() != 0)
                throw new IOException("Trailing data after protocol " + packetFormat.getVersion() + " payload");

            plugin.getCustomLogger().debug("Successfully deserialized user %s (forge=%s, protocol=%d)",
                    user.userId(), forge, packetFormat.getVersion());
            return user;
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex,
                    "Could not deserialize data from %s (forge=%s, protocol=%d). "
                            + "Make sure 'protocol' in the config matches the mod version your players use (%s).",
                    sender.getName(), forge, packetFormat.getVersion(), packetFormat.getModRange());
        }

        return null;
    }

    private byte[] serializeUser(ModUser user, boolean forge) {
        try (ByteArrayOutputStream payload = new ByteArrayOutputStream();
                CraftOutputStream output = new CraftOutputStream(payload)) {
            if (forge)
                output.writeByte(1);

            packetFormat.write(user, output);
            return payload.toByteArray();
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not serialize user (forge=%s, protocol=%d)",
                    forge, packetFormat.getVersion());
        }

        return new byte[0];
    }

    private void sendData(Player target, String channel, byte[] data) {
        if (plugin.getCustomLogger().isVerbose()) {
            plugin.getCustomLogger().debug("Outgoing payload [%s] -> %s",
                    channel, plugin.getCustomLogger().hexDump(data));
        }
        try {
            target.sendPluginMessage(plugin, channel, data);
        } catch (Exception ex) {
            // Guard against IllegalArgumentException (unregistered channel) or
            // other runtime errors so one bad recipient cannot abort the whole
            // sync loop and leave other players without their update.
            plugin.getCustomLogger().warning(ex, "Could not send plugin message to %s on channel %s",
                    target.getName(), channel);
        }
    }
}
