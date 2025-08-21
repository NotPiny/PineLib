package dev.piny.pineLib.menus;

import dev.piny.pineLib.PineLib;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AnvilMenu implements Listener {
    private Component menuTitle;
    private ItemStack startingItem;
    private ItemStack resultItem;  // New field for the result item
    private boolean autoClose = true;
    private Consumer<ItemStack> clickAction;
    private final Map<UUID, Listener> playerListeners = new HashMap<>();

    public AnvilMenu(Component title, ItemStack startingItem, boolean autoClose, Consumer<ItemStack> clickAction) {
        this.menuTitle = title;
        this.startingItem = startingItem;
        this.resultItem = startingItem.clone();  // By default, use a clone of the starting item as the result
        this.clickAction = clickAction;
        this.autoClose = autoClose;
    }

    public Component getMenuTitle() {
        return menuTitle;
    }
    public void setMenuTitle(Component menuTitle) {
        this.menuTitle = menuTitle;
    }
    public ItemStack getStartingItem() {
        return startingItem;
    }
    public void setStartingItem(ItemStack startingItem) {
        this.startingItem = startingItem;
    }
    public ItemStack getResultItem() {
        return resultItem;
    }
    public void setResultItem(ItemStack resultItem) {
        this.resultItem = resultItem;
    }
    public void setClickAction(Consumer<ItemStack> clickAction) {
        this.clickAction = clickAction;
    }

    public void open(Player player) {
        // Clean up previous listener for this specific player if it exists
        UUID playerUUID = player.getUniqueId();
        if (playerListeners.containsKey(playerUUID)) {
            HandlerList.unregisterAll(playerListeners.get(playerUUID));
            playerListeners.remove(playerUUID);
        }

        // Create an anvil inventory
        Inventory anvilInventory = Bukkit.createInventory(null, InventoryType.ANVIL, menuTitle);
        anvilInventory.setItem(0, startingItem); // Set the starting item in the first slot

        player.openInventory(anvilInventory);

        // Force set the result item after a short delay (to give time for the inventory to open)
        Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
            if (player.getOpenInventory().getType() == InventoryType.ANVIL) {
                // Ensure player still has the anvil open
                player.getOpenInventory().getTopInventory().setItem(2, resultItem);
                player.updateInventory();
            }
        }, 2L);

        // Register the click event listener for this specific player
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getView().getPlayer() instanceof Player p && p.getUniqueId().equals(playerUUID)) {
                    // Check if the clicked inventory is the anvil inventory
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ANVIL) {
                        // If they try to take the item from slot 0 or 1, prevent it
                        if (event.getSlot() == 0 || event.getSlot() == 1) {
                            event.setCancelled(true);

                            // Reset the result item if it was cleared
                            Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
                                if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
                                    p.getOpenInventory().getTopInventory().setItem(2, resultItem);
                                    p.updateInventory();
                                }
                            }, 1L);
                            return;
                        }

                        // Check if the clicked slot is the output slot (slot 2)
                        if (event.getSlot() == 2) {
                            ItemStack clickedItem = event.getCurrentItem();

                            if (clickedItem != null && !clickedItem.getType().isAir()) {
                                // Cancel the event to prevent the item from being taken
                                event.setCancelled(true);

                                // Call the click action with the result item
                                clickAction.accept(resultItem);

                                // Optionally close the inventory if autoClose is true
                                if (autoClose) {
                                    p.closeInventory();
                                }
                            } else {
                                event.setCancelled(true);

                                // Reset the result item since it was somehow cleared
                                Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
                                    if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
                                        p.getOpenInventory().getTopInventory().setItem(2, resultItem);
                                        p.updateInventory();
                                    }
                                }, 1L);
                            }
                        }
                    }
                }
            }

            @EventHandler
            public void onPrepareAnvil(PrepareAnvilEvent event) {
                if (event.getView().getPlayer() instanceof Player p && p.getUniqueId().equals(playerUUID)) {
                    // Force our result item into the result slot
                    event.getInventory().setItem(2, resultItem);
                    p.updateInventory();
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof Player p && p.getUniqueId().equals(playerUUID)) {
                    if (event.getInventory().getType() == InventoryType.ANVIL) {
                        // Unregister the listener when this specific player closes their inventory
                        HandlerList.unregisterAll(this);
                        playerListeners.remove(playerUUID);
                    }
                }
            }
        };

        // Store the listener reference for this player
        playerListeners.put(playerUUID, listener);

        Bukkit.getPluginManager().registerEvents(listener, PineLib.getInstance());
    }

    /**
     * Cleanup method to unregister all listeners.
     * Should be called when the plugin is disabled or when this menu is no longer needed.
     */
    public void cleanup() {
        playerListeners.values().forEach(HandlerList::unregisterAll);
        playerListeners.clear();
    }
}
