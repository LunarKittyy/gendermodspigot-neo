package dbrighthd.wildfiregendermodplugin.wildfire;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private final Map<UUID, ModUser> users = new HashMap<>();
    private final Map<UUID, Integer> protocolVersions = new HashMap<>();

    /**
     * A map to link players (by {@link UUID}) to their {@link ModUser}.
     *
     * @return The underlying map.
     */
    public Map<UUID, ModUser> getUsers() {
        return users;
    }

    /**
     * Store the identified protocol version for a player.
     *
     * @param uuid    The player's UUID.
     * @param version The protocol version.
     */
    public void setProtocolVersion(UUID uuid, int version) {
        protocolVersions.put(uuid, version);
    }

    /**
     * Get the identified protocol version for a player.
     *
     * @param uuid The player's UUID.
     * @return The protocol version, or -1 if not identified.
     */
    public int getProtocolVersion(UUID uuid) {
        return protocolVersions.getOrDefault(uuid, -1);
    }

    /**
     * Remove all stored data for a player.
     *
     * @param uuid The player's UUID.
     */
    public void removePlayer(UUID uuid) {
        users.remove(uuid);
        protocolVersions.remove(uuid);
    }
}
