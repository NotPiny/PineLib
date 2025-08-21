package dev.piny.pineLib;

import dev.piny.pineLib.tasks.countdown.CountdownAssigner;
import dev.piny.pineLib.tests.BossBarTestCommand;
import dev.piny.pineLib.tests.menuTestCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PineLib extends JavaPlugin {
    @Override
    public void onEnable() {
        FileConfiguration config = this.getConfig();
        config.addDefault("debug", false);
        config.setDefaults(config);

        if (config.getBoolean("debug", false)) {
            if (!getServer().getPluginManager().isPluginEnabled("CommandAPI")) {
                getLogger().warning("CommandAPI is not enabled! Some features may not work.");
            } else {
                getLogger().info("CommandAPI is enabled, proceeding with command registration.");
                new BossBarTestCommand();
                new menuTestCommand();
            }
        }

        new CountdownAssigner(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static PineLib getInstance() {
        return JavaPlugin.getPlugin(PineLib.class);
    }
}
