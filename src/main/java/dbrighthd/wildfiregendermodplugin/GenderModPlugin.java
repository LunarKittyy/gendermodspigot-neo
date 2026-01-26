package dbrighthd.wildfiregendermodplugin;

import dbrighthd.wildfiregendermodplugin.commands.DiagnosticCommand;
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
    private final CustomPluginLogger customLogger = new CustomPluginLogger(this);
    private final UserManager userManager = new UserManager();
    private final NetworkManager networkManager = new NetworkManager(this);

    @Override
    public void onEnable() {
        customLogger.info("By @dbrighthd, with contributions from @stigstille and @winnpixie");

        saveDefaultConfig();

        if (!networkManager.init()) {
            customLogger.severe("INVALID PROTOCOL, DISABLING SELF.");
            getServer().getPluginManager().disablePlugin(this);
        }

        registerEventListeners();
        registerModListeners();

        DiagnosticCommand diagCmd = new DiagnosticCommand(this);
        getCommand("femalegender").setExecutor(diagCmd);
        getCommand("femalegender").setTabCompleter(diagCmd);
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

        // Fabric
        getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.SEND_GENDER_INFO, payloadListener);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ModConstants.SYNC);

        // Forge
        getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.FORGE, payloadListener);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ModConstants.FORGE);

        // Hello handshake (5.0.0+)
        HelloPacketListener helloListener = new HelloPacketListener(this);
        getServer().getMessenger().registerIncomingPluginChannel(this, ModConstants.HELLO_SERVERBOUND, helloListener);
        getServer().getMessenger().registerOutgoingPluginChannel(this, ModConstants.HELLO_CLIENTBOUND);
    }
}
