package dev.piny.pineLib.tests;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AdventureChatComponentArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.piny.pineLib.menus.*;
import dev.piny.pineLib.menus.MenuItem;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.awt.*;
import java.util.Objects;

public class menuTestCommand {
    private void generateCheckerPattern(ContainerMenu container, int size) {
        ItemStack black = new ItemStack(Material.COAL_BLOCK);
        ItemStack white = new ItemStack(Material.QUARTZ_BLOCK);

        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                container.set(i, MenuItem.of(black, inventoryClickEvent -> {
                    inventoryClickEvent.getWhoClicked().sendMessage(Component.text("All the black blocks have a event listener here!"));
                }));
            } else {
                container.set(i, MenuItem.of(white));
            }
        }
    }

    public menuTestCommand() {
        new CommandAPICommand("testmenu")
                .withSubcommands(
                        new CommandAPICommand("anvil")
                                .withArguments(
                                        new AdventureChatComponentArgument("title")
                                )
                                .executesPlayer((player, args) -> {
                                    Component title = (Component) args.get("title");

                                    AnvilMenu menu = new AnvilMenu(title, new ItemStack(Material.PAPER), true, itemStack -> {
                                        try {
                                            player.sendMessage("The anvil thingy worked");
                                            if (itemStack != null) {
                                                // More reliable way to get the display name
                                                Component displayName;

                                                if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
                                                    // Use the custom name if it exists
                                                    displayName = itemStack.getItemMeta().displayName();
                                                    player.sendMessage("You clicked on: " + PlainTextComponentSerializer.plainText().serialize(displayName));
                                                } else {
                                                    // Fallback to type name if no custom name
                                                    player.sendMessage("You clicked on: " + itemStack.getType().toString().toLowerCase().replace('_', ' '));
                                                }

                                                // Debug information to help diagnose the issue
                                                player.sendMessage("Item debug info: Type=" + itemStack.getType() + ", HasMeta=" +
                                                        itemStack.hasItemMeta() + ", HasDisplayName=" +
                                                        (itemStack.hasItemMeta() ? itemStack.getItemMeta().hasDisplayName() : "N/A"));
                                            } else {
                                                player.sendMessage("You clicked on an empty item.");
                                            }
                                        } catch(Exception e) {
                                            player.sendMessage("The anvil thingy did not work");
                                            e.printStackTrace();
                                        }
                                    });

                                    menu.show(player);
                                }),
                        new CommandAPICommand("chest")
                                .withArguments(
                                        new AdventureChatComponentArgument("title"),
                                        new IntegerArgument("rows", 1, 6)
                                )
                                .executesPlayer((player, args) -> {
                                    Component title = (Component) args.get("title");
                                    int rows = (Integer) args.get("rows");

                                    ContainerMenu menu = new ChestMenu(title, rows);
                                    generateCheckerPattern(menu, rows * 9);
                                    menu.show(player);
                                }),
                        new CommandAPICommand("crafter")
                                .withArguments(
                                        new AdventureChatComponentArgument("title")
                                )
                                .executesPlayer((player, args) -> {
                                    Component title = (Component) args.get("title");

                                    ContainerMenu menu = new CrafterMenu(title);
                                    generateCheckerPattern(menu, 3 * 3);
                                    menu.show(player);
                                }),
                        new CommandAPICommand("dropper")
                                .withArguments(
                                        new AdventureChatComponentArgument("title")
                                )
                                .executesPlayer((player, args) -> {
                                    Component title = (Component) args.get("title");

                                    ContainerMenu menu = new DropperMenu(title);
                                    generateCheckerPattern(menu, 3 * 3);
                                    menu.show(player);
                                })
                )
                .register("pinelibtest");
    }
}
