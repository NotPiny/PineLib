package dev.piny.pineLib.tasks.countdown;

import dev.piny.pineLib.PineLib;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;

public class BukkitBossBarCountdown {
    public String originalTitle;
    private final PineLib plugin;

    /**
     * Creates a countdown boss bar that updates every interval until it reaches 0.
     * @param bar the BossBar to update
     * @param max the maximum value for the countdown (in ticks)
     * @param interval the interval in ticks at which the bar updates
     */
    public BukkitBossBarCountdown(BossBar bar, int max, int interval, boolean assignOnJoin) {
        this.originalTitle = bar.getTitle(); // Store the original title with placeholders
        bar.setVisible(true);
        bar.setProgress(1.0);
        plugin = PineLib.getPlugin(PineLib.class);

        if (assignOnJoin) AutoAssignedCountdowns.BUKKIT_BOSS_BARS.add(bar);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateBossBar(bar, max, interval, plugin);
        }, interval);
    }

    private void updateBossBar(BossBar bar, int max, int interval, PineLib plugin) {
        if (!(bar.getProgress() >= 0.01)) {
            bar.removeAll();
            bar.setVisible(false);
            AutoAssignedCountdowns.BUKKIT_BOSS_BARS.remove(bar);
            return;
        }

        double progress = bar.getProgress() - (1.0 / max);
        if (progress < 0.0) {
            progress = 0.0;
            bar.setProgress(progress);
            updateBossBar(bar, max, interval, plugin);
            return;
        }
        bar.setProgress(progress);

        // Start with the original template each time
        String barName = this.originalTitle;

        // Process all placeholders
        if (barName.contains("%%{seconds}%%")) {
            int seconds = (int) (progress * max / 20);
            barName = barName.replace("%%{seconds}%%", String.valueOf(seconds));
        }

        if (barName.contains("%%{minutes}%%")) {
            int minutes = (int) (progress * max / (20 * 60)); // Convert ticks to minutes
            barName = barName.replace("%%{minutes}%%", String.valueOf(minutes));
        }

        if (barName.contains("%%{hours}%%")) {
            int hours = (int) (progress * max / (20 * 3600)); // Convert ticks to hours
            barName = barName.replace("%%{hours}%%", String.valueOf(hours));
        }

        if (barName.contains("%%{percentage}%%")) {
            int percentage = (int) (progress * 100);
            barName = barName.replace("%%{percentage}%%", String.valueOf(percentage));
        }

        if (barName.contains("%%{progress}%%")) {
            int progressValue = (int) (progress * max);
            barName = barName.replace("%%{progress}%%", String.valueOf(progressValue));
        }

        if (barName.contains("%%{raw_progress}%%")) {
            // Raw progress as stored in the variable
            barName = barName.replace("%%{raw_progress}%%", String.valueOf(progress));
        }

        if (barName.contains("%%{remaining}%%")) {
            // Calculate remaining time based on ticks
            int totalSeconds = (int) (progress * max / 20); // Convert ticks to seconds
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            int seconds = totalSeconds % 60;

            StringBuilder remainingTime = new StringBuilder();
            if (hours > 0) {
                remainingTime.append(hours).append("h ");
            }
            if (minutes > 0) {
                remainingTime.append(minutes).append("m ");
            }
            if (seconds > 0 || (hours == 0 && minutes == 0)) {
                remainingTime.append(seconds).append("s");
            }
            barName = barName.replace("%%{remaining}%%", remainingTime.toString().trim());
        }

        if (barName.contains("%%{ticks}%%")) {
            int ticks = (int) (progress * max); // Already in ticks
            barName = barName.replace("%%{ticks}%%", String.valueOf(ticks));
        }

        bar.setTitle(barName);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            updateBossBar(bar, max, interval, plugin);
        }, interval);
    }

    public void stopCountdown(BossBar bar) {
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
            AutoAssignedCountdowns.BUKKIT_BOSS_BARS.remove(bar);
        }
    }
}