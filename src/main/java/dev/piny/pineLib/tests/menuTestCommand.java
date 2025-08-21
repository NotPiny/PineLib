package dev.piny.pineLib.tests;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AdventureChatComponentArgument;
import dev.piny.pineLib.menus.AnvilMenu;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class menuTestCommand {
    public menuTestCommand() {
        new CommandAPICommand("testmenu")
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
                })
                .register("pinylibtest");
    }
}
