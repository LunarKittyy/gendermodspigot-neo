package dbrighthd.wildfiregendermodplugin.wildfire;

import dbrighthd.wildfiregendermodplugin.wildfire.setup.ModConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private final Map<UUID, ModUser> users = new HashMap<>();

    /**
     * A map to link players (by {@link UUID}) to their {@link ModConfiguration}.
     *
     * @return The underlying map.
     */
    public Map<UUID, ModUser> getUsers() {
        return users;
    }
}
