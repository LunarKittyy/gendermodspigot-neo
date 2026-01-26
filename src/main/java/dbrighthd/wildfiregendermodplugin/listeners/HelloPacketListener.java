package dbrighthd.wildfiregendermodplugin.listeners;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Handles the "Hello" handshake from mod users (5.0.0+).
 */
public class HelloPacketListener implements PluginMessageListener {
    private final GenderModPlugin plugin;

    public HelloPacketListener(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(ModConstants.HELLO_SERVERBOUND))
            return;

        try (CraftInputStream input = CraftInputStream.ofBytes(message)) {
            int handshakeVersion = input.readVarInt();

            // Handshake version 1 currently maps to Protocol V5 (which includes UV support)
            int protocolVersion = handshakeVersion == 1 ? 5 : -1;

            plugin.getCustomLogger().debug(
                    "Received hello from player %s using handshake version %d (mapped to protocol %d)",
                    player.getName(), handshakeVersion, protocolVersion);

            if (protocolVersion != -1) {
                plugin.getUserManager().setProtocolVersion(player.getUniqueId(), protocolVersion);
            } else {
                plugin.getCustomLogger().warning(
                        "Sync version mismatch for %s; network errors may occur! (client handshake=%d, server protocol version=1)",
                        player.getName(), handshakeVersion);
            }

            sendHelloResponse(player);

        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not parse Hello packet from %s", player.getName());
        }
    }

    private void sendHelloResponse(Player player) {
        try (ByteArrayOutputStream payload = new ByteArrayOutputStream();
                CraftOutputStream output = new CraftOutputStream(payload)) {

            output.writeVarInt(ModConstants.HELLO_PROTOCOL_VERSION);

            player.sendPluginMessage(plugin, ModConstants.HELLO_CLIENTBOUND, payload.toByteArray());
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not send Hello response to %s", player.getName());
        }
    }
}
