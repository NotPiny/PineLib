package dev.piny.pineLib.tasks.countdown;

import dev.piny.pineLib.PineLib;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CountdownAssigner implements Listener {
    public CountdownAssigner(PineLib plugin) {
        // Register the listener with the plugin's event manager
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        AutoAssignedCountdowns.BUKKIT_BOSS_BARS.forEach(bossBar -> bossBar.addPlayer(event.getPlayer()));
        AutoAssignedCountdowns.ADVENTURE_BOSS_BARS.forEach(bossBar -> bossBar.addViewer(event.getPlayer()));
    }
}
