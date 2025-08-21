package dev.piny.pineLib.tasks.countdown;

import dev.piny.pineLib.PineLib;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.intellij.lang.annotations.RegExp;

public class BossBarCountdown {
    public Component originalTitle;
    private final PineLib plugin;

    public BossBarCountdown(BossBar bar, int max, int interval, boolean assignOnJoin) {
        this.originalTitle = bar.name(); // Store the original title with placeholders
        bar.progress(1.0f);
        plugin = PineLib.getPlugin(PineLib.class);

        if (assignOnJoin) AutoAssignedCountdowns.ADVENTURE_BOSS_BARS.add(bar);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateBossBar(bar, max, interval, plugin);
        }, interval);
    }

    private boolean containsPlaceholder(Component component, String placeholder) {
        if (component instanceof TextComponent textComponent) {
            if (textComponent.content().contains(placeholder)) {
                return true;
            }
        }
        for (Component child : component.children()) {
            if (containsPlaceholder(child, placeholder)) {
                return true;
            }
        }
        return false;
    }

    private Component replacePlaceholder(Component component, String placeholder, String replacement) {
        // Escape special regex characters in the placeholder
        @RegExp String escapedPlaceholder = java.util.regex.Pattern.quote(placeholder);
        return component.replaceText(builder -> {
            builder.match(escapedPlaceholder)
                    .replacement(replacement);
        });
    }

    private void updateBossBar(BossBar bar, int max, int interval, PineLib plugin) {
        if (!(bar.progress() >= 0.01f)) {
            // Surely there's a better way to do this? I mean, the bukkit one has a removeAll method so why doesn't this one?
            plugin.getServer().getOnlinePlayers().forEach(bar::removeViewer);
            bar.progress(0.0f);
            AutoAssignedCountdowns.ADVENTURE_BOSS_BARS.remove(bar);
            return;
        }

        float progress = bar.progress() - (1.0f / max);
        if (progress < 0.0f) {
            progress = 0.0f;
            bar.progress(progress);
            updateBossBar(bar, max, interval, plugin);
            return;
        }

        bar.progress(progress);

        // Start with the original template each time
        Component barName = this.originalTitle;

        if (containsPlaceholder(barName, "%%{seconds}%%")) {
            int seconds = (int) (progress * max / 20);
            barName = replacePlaceholder(barName, "%%{seconds}%%", String.valueOf(seconds));
        }

        if (containsPlaceholder(barName, "%%{minutes}%%")) {
            int minutes = (int) (progress * max / (20 * 60)); // Convert ticks to minutes
            barName = replacePlaceholder(barName, "%%{minutes}%%", String.valueOf(minutes));
        }

        if (containsPlaceholder(barName, "%%{hours}%%")) {
            int hours = (int) (progress * max / (20 * 3600)); // Convert ticks to hours
            barName = replacePlaceholder(barName, "%%{hours}%%", String.valueOf(hours));
        }

        if (containsPlaceholder(barName, "%%{percentage}%%")) {
            int percentage = (int) (progress * 100);
            barName = replacePlaceholder(barName, "%%{percentage}%%", String.valueOf(percentage));
        }

        if (containsPlaceholder(barName, "%%{progress}%%")) {
            int progressValue = (int) (progress * max);
            barName = replacePlaceholder(barName, "%%{progress}%%", String.valueOf(progressValue));
        }

        if (containsPlaceholder(barName, "%%{raw_progress}%%")) {
            // Raw progress as stored in the variable
            barName = replacePlaceholder(barName, "%%{raw_progress}%%", String.valueOf(progress));
        }

        if (containsPlaceholder(barName, "%%{remaining}%%")) {
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
            barName = replacePlaceholder(barName, "%%{remaining}%%", remainingTime.toString().trim());
        }

        if (containsPlaceholder(barName, "%%{ticks}%%")) {
            int ticks = (int) (progress * max); // Already in ticks
            barName = replacePlaceholder(barName, "%%{ticks}%%", String.valueOf(ticks));
        }

        bar.name(barName);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updateBossBar(bar, max, interval, plugin);
        }, interval);
    }
}
