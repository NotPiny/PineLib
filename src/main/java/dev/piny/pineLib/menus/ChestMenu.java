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
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ChestMenu {
    private Inventory inventory;
    private HashMap<Integer, MenuItem> menuItems;
    private final Map<UUID, Listener> playerListeners = new HashMap<>();

    public ChestMenu(Component title, int rows) {
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.menuItems = new HashMap<>();
    }

    /**
     * Adds a MenuItem to the first available slot in the inventory.
     * @param menuItem the MenuItem to add
     * @param strict if true, the method will throw an exception if no empty slot is found; if false, it will simply not add the item if no empty slot is available.
     */
    public void add(MenuItem menuItem, boolean strict) {
        if (menuItem == null) {
            throw new IllegalArgumentException("MenuItem cannot be null.");
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, menuItem.getItem());
                menuItems.put(i, menuItem);
                return;
            }
        }

        if (strict) {
            throw new IllegalStateException("No empty slot available in the inventory to add the MenuItem.");
        }
    }

    public void set(int slot, MenuItem menuItem) {
        if (slot < 0 || slot >= inventory.getSize()) {
            throw new IndexOutOfBoundsException("Slot index out of bounds: " + slot);
        }
        inventory.setItem(slot, menuItem.getItem());
        menuItems.put(slot, menuItem);
    }

    public MenuItem get(int slot) {
        return menuItems.get(slot);
    }

    public void fill(MenuItem menuItem) {fill(menuItem, false, 0, inventory.getSize() - 1);}
    public void fill(MenuItem menuItem, boolean override, int start, int end) {
        if (start < 0 || end >= inventory.getSize() || start > end) {
            throw new IndexOutOfBoundsException("Invalid range: " + start + " to " + end);
        }
        for (int i = start; i <= end; i++) {
            if (override) {
                inventory.setItem(i, menuItem.getItem());
                menuItems.put(i, menuItem);
            } else if (inventory.getItem(i) == null) {
                inventory.setItem(i, menuItem.getItem());
                menuItems.put(i, menuItem);
            }
        }
    }

    public void show(Player player) {
        if (player == null || !player.isOnline()) {
            throw new IllegalArgumentException("Player must be online to show the menu.");
        }

        // Clean up previous listener for this specific player if it exists
        UUID playerUUID = player.getUniqueId();
        if (playerListeners.containsKey(playerUUID)) {
            HandlerList.unregisterAll(playerListeners.get(playerUUID));
            playerListeners.remove(playerUUID);
        }

        // Register a new player-specific listener
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (event.getView().getPlayer() instanceof Player p && p.getUniqueId().equals(playerUUID)) {
                    if (!Objects.equals(event.getClickedInventory(), inventory)) return;

                    event.setCancelled(true);

                    int slot = event.getSlot();
                    MenuItem menuItem = get(slot);

                    Consumer<InventoryClickEvent> clickAction = null;
                    if (menuItem != null) {
                        clickAction = menuItem.getClickAction();
                    }

                    if (clickAction == null) return;
                    clickAction.accept(event);
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (event.getPlayer() instanceof Player p && p.getUniqueId().equals(playerUUID)) {
                    if (Objects.equals(event.getInventory(), inventory)) {
                        // Unregister the listener when this specific player closes the inventory
                        HandlerList.unregisterAll(this);
                        playerListeners.remove(playerUUID);
                    }
                }
            }
        };

        // Store the listener reference for this player
        playerListeners.put(playerUUID, listener);

        // Register the listener
        Bukkit.getPluginManager().registerEvents(listener, PineLib.getInstance());

        // Open the inventory for the player
        player.openInventory(inventory);
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
