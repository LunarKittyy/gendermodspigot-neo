package dbrighthd.wildfiregendermodplugin.listeners;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles player join and quit events.
 *
 * @author winnpixie
 */
public class ConnectionListener implements Listener {
    private final GenderModPlugin plugin;

    public ConnectionListener(GenderModPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // The initial sync to this player is deferred until the client can accept
        // it: after the play-phase hello (protocol 5), after it registers the sync
        // channel (protocol 6), or after its first payload (protocols 2-4).
        plugin.getCustomLogger().debug("Player %s joined, awaiting handshake/payload", player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        plugin.getCustomLogger().debug("Removing %s", player.getName());

        // Remove configuration for a player who is no longer online.
        plugin.getUserManager().removePlayer(uuid);
    }
}
