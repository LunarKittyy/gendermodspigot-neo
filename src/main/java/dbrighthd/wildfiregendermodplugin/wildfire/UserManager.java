package dbrighthd.wildfiregendermodplugin.wildfire;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<UUID, ModUser> users = new ConcurrentHashMap<>();

    /**
     * Players that confirmed they speak the server's protocol, either through a
     * hello handshake (protocols 5 and 6) or a successfully-parsed payload
     * (protocols 2-4). Only ready players are sent sync packets.
     */
    private final Set<UUID> readyPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * A map to link players (by {@link UUID}) to their {@link ModUser}.
     *
     * @return The underlying map.
     */
    public Map<UUID, ModUser> getUsers() {
        return users;
    }

    /**
     * Mark a player as ready to receive sync packets.
     *
     * @param uuid The player's UUID.
     */
    public void setProtocolReady(UUID uuid) {
        readyPlayers.add(uuid);
    }

    /**
     * Returns {@code true} if the player is safe to receive sync packets.
     *
     * @param uuid The player's UUID.
     */
    public boolean isProtocolReady(UUID uuid) {
        return readyPlayers.contains(uuid);
    }

    /**
     * Remove all stored data for a player.
     *
     * @param uuid The player's UUID.
     */
    public void removePlayer(UUID uuid) {
        users.remove(uuid);
        readyPlayers.remove(uuid);
    }
}
