package dev.piny.pineLib.tests;

import dev.jorel.commandapi.*;
import dev.jorel.commandapi.arguments.*;
import dev.piny.pineLib.tasks.countdown.BossBarCountdown;
import dev.piny.pineLib.tasks.countdown.BukkitBossBarCountdown;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class BossBarTestCommand {
    public BossBarTestCommand() {
        new CommandAPICommand("bossbar")
                .withPermission(CommandPermission.OP)
                .withSubcommands(
                        new CommandAPICommand("adventure")
                                .withArguments(
                                        new AdventureChatComponentArgument("title"),
                                        new BooleanArgument("assignOnJoin"),
                                        new IntegerArgument("max", 1, Integer.MAX_VALUE),
                                        new IntegerArgument("interval", 1, Integer.MAX_VALUE)
                                )
                                .executesPlayer((player, args) -> {
                                    boolean assignOnJoin = (boolean) args.get("assignOnJoin");
                                    Component title = (Component) args.get("title");
                                    int max = (int) args.get("max");
                                    int interval = (int) args.get("interval");

                                    if (title == null) {
                                        title = Component.text("Adventure BossBar Test"); // THIS SHOULD NEVER HAPPEN
                                    }

                                    net.kyori.adventure.bossbar.BossBar bar = net.kyori.adventure.bossbar.BossBar.bossBar(
                                            title,
                                            1.0f,
                                            net.kyori.adventure.bossbar.BossBar.Color.BLUE,
                                            net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_20
                                    );

                                    bar.addViewer(player);
                                    new BossBarCountdown(bar, max, interval, assignOnJoin);
                                }),
                        new CommandAPICommand("bukkit")
                                .withArguments(
                                        new TextArgument("title"),
                                        new BooleanArgument("assignOnJoin"),
                                        new IntegerArgument("max", 1, Integer.MAX_VALUE),
                                        new IntegerArgument("interval", 1, Integer.MAX_VALUE)
                                )
                                .executesPlayer((player, args) -> {
                                    boolean assignOnJoin = (boolean) args.get("assignOnJoin");
                                    String title = (String) args.get("title");
                                    int max = (int) args.get("max");
                                    int interval = (int) args.get("interval");

                                    if (title == null || title.isEmpty()) {
                                        title = "Bukkit BossBar Test"; // THIS SHOULD NEVER HAPPEN
                                    }

                                    org.bukkit.boss.BossBar bar = Bukkit.createBossBar(
                                            title,
                                            BarColor.BLUE,
                                            BarStyle.SEGMENTED_20
                                    );

                                    bar.addPlayer(player);
                                    new BukkitBossBarCountdown(bar, max, interval, assignOnJoin);
                                })
                )
                .register("pinelibtest");
    }
}
