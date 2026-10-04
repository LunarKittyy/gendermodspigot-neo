package dbrighthd.wildfiregendermodplugin.listeners;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.connection.PlayerConnection;
import io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.UUID;

/**
 * The protocol 6 handshake (mod 5.0.0-Beta.5+). The client only exchanges sync packets
 * after the server sent it a hello during the configuration phase, which only Paper
 * exposes to plugins. Never load this class on a server without Paper's connection API.
 */
public final class ConfigHandshakeListener implements Listener, PluginMessageListener {
    private final GenderModPlugin plugin;
    private boolean warnedAboutChannel;

    private ConfigHandshakeListener(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    public static boolean isSupported() {
        try {
            Class.forName("io.papermc.paper.event.connection.configuration.AsyncPlayerConnectionConfigureEvent");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static void register(GenderModPlugin plugin) {
        ConfigHandshakeListener listener = new ConfigHandshakeListener(plugin);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, ModConstants.V6_HELLO_CLIENTBOUND);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, ModConstants.V6_HELLO_SERVERBOUND, listener);
    }

    @EventHandler
    private void onConfigure(AsyncPlayerConnectionConfigureEvent event) {
        PlayerConfigurationConnection connection = event.getConnection();
        if (!makeChannelSendable(connection, ModConstants.V6_HELLO_CLIENTBOUND))
            return;

        try (ByteArrayOutputStream payload = new ByteArrayOutputStream();
                CraftOutputStream output = new CraftOutputStream(payload)) {
            // List of supported handshake versions
            output.writeVarInt(1);
            output.writeVarInt(ModConstants.V6_HELLO_VERSION);

            connection.sendPluginMessage(plugin, ModConstants.V6_HELLO_CLIENTBOUND, payload.toByteArray());
            plugin.getCustomLogger().debug("Sent configuration hello to %s", connection.getProfile().getName());
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not send hello to %s", connection.getProfile().getName());
        }
    }

    /**
     * Paper only sends plugin messages on channels the client registered, but it never
     * starts the channel registration exchange during the configuration phase, so the
     * client never registers any. Plugins can't send that registration themselves
     * (reserved channel), so add the channel through Paper's internal bridge instead.
     */
    private boolean makeChannelSendable(PlayerConfigurationConnection connection, String channel) {
        if (connection.getListeningPluginChannels().contains(channel))
            return true;

        try {
            Method addChannel = connection.getClass().getMethod("addChannel", String.class);
            addChannel.invoke(connection, channel);
            return true;
        } catch (ReflectiveOperationException | RuntimeException ex) {
            if (!warnedAboutChannel) {
                warnedAboutChannel = true;
                plugin.getCustomLogger().warning(ex,
                        "Could not open the hello channel during the configuration phase; "
                                + "syncing won't work on this server version. Please report this.");
            }
            return false;
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull PlayerConnection connection, byte @NotNull [] message) {
        if (!channel.equals(ModConstants.V6_HELLO_SERVERBOUND)
                || !(connection instanceof PlayerConfigurationConnection configuration))
            return;

        String name = configuration.getProfile().getName();
        UUID playerId = configuration.getProfile().getId();
        if (playerId == null)
            return;

        try (CraftInputStream input = CraftInputStream.ofBytes(message)) {
            int version = input.readVarInt();
            if (version != ModConstants.V6_HELLO_VERSION) {
                plugin.getCustomLogger().warning(
                        "%s's mod uses sync handshake version %d but this server expects %d, so they won't be synced. "
                                + "They might need to update the mod, or the plugin is outdated.",
                        name, version, ModConstants.V6_HELLO_VERSION);
                return;
            }

            plugin.getUserManager().setProtocolReady(playerId);
            plugin.getCustomLogger().debug("Received hello from %s (handshake version %d)", name, version);
        } catch (IOException ex) {
            plugin.getCustomLogger().warning(ex, "Could not parse hello from %s", name);
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        // The hello only happens during the configuration phase.
    }

    /**
     * The first point in the play phase where the client accepts sync packets.
     */
    @EventHandler
    private void onRegisterChannel(PlayerRegisterChannelEvent event) {
        Player player = event.getPlayer();
        if (!event.getChannel().equals(ModConstants.V6_SYNC_CLIENTBOUND)
                || !plugin.getUserManager().isProtocolReady(player.getUniqueId()))
            return;

        plugin.getNetworkManager().sync(Collections.singletonList(player));
    }
}
