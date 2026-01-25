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

    private final GenderModPlugin plugin;

    private ModSyncPacket packetFormat;

    static {
        PACKET_FORMATS = Map.of(
                1, new ModSyncPacketV1(),
                2, new ModSyncPacketV2(),
                3, new ModSyncPacketV3(),
                4, new ModSyncPacketV4(),
                5, new ModSyncPacketV5()
        );
    }

    public NetworkManager(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean init() {
        int protocolVersion = plugin.getConfig().getInt("mod.protocol", -1);
        packetFormat = protocolVersion == -1 ? PACKET_FORMATS.get(5)
                : PACKET_FORMATS.get(protocolVersion);
        if (packetFormat == null) return false;

        plugin.getCustomLogger().info("Using protocol %d for mod version(s) %s",
                packetFormat.getVersion(), packetFormat.getModRange());

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
        try (CraftInputStream input = CraftInputStream.ofBytes(data)) {
            if (forge) input.readByte();

            return packetFormat.read(input);
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not deserialize user (forge=%s)", forge);
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
            plugin.getCustomLogger().warning(ex, "Could not serialize user (forge=%s)", forge);
        }

        return new byte[0];
    }

    private void sendData(Player target, String channel, byte[] data) {
        target.sendPluginMessage(plugin, channel, data);
    }
}
