package dbrighthd.wildfiregendermodplugin.logging;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Level;

public class CustomPluginLogger {
    private final GenderModPlugin plugin;

    private final boolean enabled;
    private final boolean debug;

    public CustomPluginLogger(GenderModPlugin plugin) {
        this.plugin = plugin;

        FileConfiguration pluginConfig = plugin.getConfig();
        this.enabled = pluginConfig.getBoolean("logging.enabled", true);
        this.debug = pluginConfig.getBoolean("logging.debug", false);
    }

    public void debug(String message, Object... params) {
        if (debug) log(Level.INFO, message, params);
    }

    public void info(String message, Object... params) {
        log(Level.INFO, message, params);
    }

    public void warning(String message, Object... params) {
        log(Level.WARNING, message, params);
    }

    public void severe(String message, Object... params) {
        log(Level.SEVERE, message, params);
    }

    public void log(Level level, String message, Object... params) {
        if (!enabled) return;

        if (params.length == 0) {
            plugin.getLogger().log(level, message);
            return;
        }

        plugin.getLogger().log(level, () -> message.formatted(params));
    }

    public void debug(Throwable throwable, String message, Object... params) {
        if (debug) thrown(Level.INFO, throwable, message, params);
    }

    public void info(Throwable throwable, String message, Object... params) {
        thrown(Level.INFO, throwable, message, params);
    }

    public void warning(Throwable throwable, String message, Object... params) {
        thrown(Level.WARNING, throwable, message, params);
    }

    public void severe(Throwable throwable, String message, Object... params) {
        thrown(Level.SEVERE, throwable, message, params);
    }

    public void thrown(Level level, Throwable throwable, String message, Object... params) {
        if (!enabled) return;

        if (params.length == 0) {
            plugin.getLogger().log(level, message, throwable);
            return;
        }

        plugin.getLogger().log(level, throwable, () -> message.formatted(params));
    }
}
