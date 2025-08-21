package dev.piny.pineLib.tests;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AdventureChatComponentArgument;
import dev.piny.pineLib.menus.CrafterMenu;
import dev.piny.pineLib.menus.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class menuTestCommand {
    public menuTestCommand() {
        new CommandAPICommand("testmenu")
                .withArguments(
                        new AdventureChatComponentArgument("title")
                )
                .executesPlayer((player, args) -> {
                    Component title = (Component) args.get("title");
                    ItemStack itemA = new ItemStack(Material.DIAMOND_SWORD);
                    ItemStack itemB = new ItemStack(Material.GOLDEN_APPLE);
                    ItemStack itemC = new ItemStack(Material.IRON_CHESTPLATE);

                    CrafterMenu menu = new CrafterMenu(title);
                    menu.add(MenuItem.of(itemA), true);
                    menu.add(MenuItem.of(itemB), true);
                    menu.add(MenuItem.of(itemC), true);
                    menu.fill(MenuItem.of(Material.DIRT));

                    menu.show(player);
                })
                .register("pinylibtest");
    }
}
