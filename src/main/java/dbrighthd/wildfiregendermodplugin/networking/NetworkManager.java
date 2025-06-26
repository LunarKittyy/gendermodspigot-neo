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
import java.util.logging.Level;

/**
 * @author winnpixie
 */
public class NetworkManager {
    private static final Map<Integer, ModSyncPacket> PACKET_FORMATS = HashMap.newHashMap(4);

    private final GenderModPlugin plugin;

    private ModSyncPacket packetFormat;

    static {
        PACKET_FORMATS.put(1, new ModSyncPacketV1());
        PACKET_FORMATS.put(2, new ModSyncPacketV2());
        PACKET_FORMATS.put(3, new ModSyncPacketV3());
        PACKET_FORMATS.put(4, new ModSyncPacketV4());
    }

    public NetworkManager(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        int protocolVersion = plugin.getConfig().getInt("mod.protocol", -1);
        packetFormat = protocolVersion == -1 ? PACKET_FORMATS.get(4)
                : PACKET_FORMATS.get(protocolVersion);
        if (packetFormat == null) return false;

        plugin.getLogger().log(Level.INFO, () -> "Using Protocol %d for mod version(s) %s".formatted(
                packetFormat.getVersion(), packetFormat.getModRange()));

        return true;
    }

    public void sync(Collection<? extends Player> audience) {
        for (ModUser user : plugin.getUserManager().getUsers().values()) {
            byte[] fabricData = serializeUser(user, false);
            byte[] forgeData = serializeUser(user, true);

            for (Player recipient : audience) {
                if (fabricData.length > 0) sendData(recipient, ModConstants.SYNC, fabricData);
                if (forgeData.length > 0) sendData(recipient, ModConstants.FORGE, forgeData);
            }
        }
    }

    public ModUser deserializeUser(byte[] data, boolean forge) {
        try (CraftInputStream input = new CraftInputStream(data)) {
            if (forge) input.readByte();

            return packetFormat.read(input);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, ex, () -> "Could not deserialize user (forge=%s)".formatted(forge));
        }

        return null;
    }

    private byte[] serializeUser(ModUser user, boolean forge) {
        try (ByteArrayOutputStream payload = new ByteArrayOutputStream();
             CraftOutputStream output = new CraftOutputStream(payload)) {
            if (forge) output.writeByte(1);

            packetFormat.write(user, output);
            return payload.toByteArray();
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, ex, () -> "Could not serialize user (forge=%s)".formatted(forge));
        }

        return new byte[0];
    }

    private void sendData(Player target, String channel, byte[] data) {
        target.sendPluginMessage(plugin, channel, data);
    }
}
