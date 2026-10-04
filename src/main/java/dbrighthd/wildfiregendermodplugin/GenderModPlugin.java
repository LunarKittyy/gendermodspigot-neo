package dbrighthd.wildfiregendermodplugin;

import dbrighthd.wildfiregendermodplugin.listeners.ConfigHandshakeListener;
import dbrighthd.wildfiregendermodplugin.listeners.ConnectionListener;
import dbrighthd.wildfiregendermodplugin.listeners.HelloPacketListener;
import dbrighthd.wildfiregendermodplugin.listeners.ModPayloadListener;
import dbrighthd.wildfiregendermodplugin.logging.CustomPluginLogger;
import dbrighthd.wildfiregendermodplugin.networking.NetworkManager;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import dbrighthd.wildfiregendermodplugin.wildfire.UserManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The entry-point for this plugin.
 *
 * @author dbrighthd
 */
public final class GenderModPlugin extends JavaPlugin {
    private CustomPluginLogger customLogger;
    private final UserManager userManager = new UserManager();
    private final NetworkManager networkManager = new NetworkManager(this);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        customLogger = new CustomPluginLogger(this);

        customLogger.info("By @dbrighthd, with contributions from @stigstille and @winnpixie");

        if (!networkManager.init()) {
            customLogger.severe("INVALID PROTOCOL, DISABLING SELF.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (networkManager.getProtocolVersion() >= 6 && !ConfigHandshakeListener.isSupported()) {
            customLogger.severe("Protocol %d (mod 5.0.0-Beta.5 and newer) needs a Paper server (or a fork of Paper), "
                    + "Spigot can't do the handshake the mod requires. DISABLING SELF.",
                    networkManager.getProtocolVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerEventListeners();
        registerModListeners();
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public CustomPluginLogger getCustomLogger() {
        return customLogger;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    private void registerEventListeners() {
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
    }

    private void registerModListeners() {
        ModPayloadListener payloadListener = new ModPayloadListener(this);
        for (String channel : networkManager.getIncomingSyncChannels())
            getServer().getMessenger().registerIncomingPluginChannel(this, channel, payloadListener);
        for (String channel : networkManager.getOutgoingSyncChannels())
            getServer().getMessenger().registerOutgoingPluginChannel(this, channel);

        int protocol = networkManager.getProtocolVersion();
        if (protocol == 5) {
            // Play-phase hello handshake (5.0.0-Beta.1 to Beta.4)
            HelloPacketListener helloListener = new HelloPacketListener(this);
            getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.HELLO_SERVERBOUND, helloListener);
            getServer().getMessenger().registerOutgoingPluginChannel(this, ModConstants.HELLO_CLIENTBOUND);
        } else if (protocol >= 6) {
            // Configuration-phase hello handshake (5.0.0-Beta.5 and newer)
            ConfigHandshakeListener.register(this);
        }
    }
}
