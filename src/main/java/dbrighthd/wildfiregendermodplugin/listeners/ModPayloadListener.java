package dbrighthd.wildfiregendermodplugin.listeners;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * Handles payload packets from mod users.
 *
 * @author winnpixie
 */
public class ModPayloadListener implements PluginMessageListener {
    private final GenderModPlugin plugin;

    public ModPayloadListener(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(ModConstants.SEND_GENDER_INFO) && !channel.equals(ModConstants.FORGE))
            return;

        ModUser user = plugin.getNetworkManager().deserializeUser(message, channel.equals(ModConstants.FORGE), player);
        if (user == null)
            return;

        if (!player.getUniqueId().equals(user.userId())) {
            plugin.getCustomLogger().warning("Unauthorized access attempt by %s for %s",
                    player.getName(), user.userId());

            // Early return, unauthorized attempt to set another player's data.
            return;
        }

        plugin.getUserManager().getUsers().put(user.userId(), user);
        plugin.getCustomLogger().debug("Stored %s as %s",
                player.getName(), user.configuration().generalOptions().genderIdentity().name());

        // Mark this player ready to receive sync packets now that their protocol
        // is confirmed. For V4/older clients this is the first confirmation;
        // for V5 clients this is a no-op (already marked ready by HelloPacketListener).
        plugin.getUserManager().setProtocolReady(player.getUniqueId());

        // Send this player a full dump of all currently stored users so they see
        // everyone who was already online. This is the deferred initial sync that
        // replaces the join-time sync for this player — they weren't ready at join.
        plugin.getNetworkManager().sync(Collections.singletonList(player));

        // Sync all stored users (including this player's just-updated data) to
        // every ready online player.
        plugin.getNetworkManager().sync(plugin.getServer().getOnlinePlayers());
    }
}
