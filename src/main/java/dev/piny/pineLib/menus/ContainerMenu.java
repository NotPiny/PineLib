package dev.piny.pineLib.menus;

import dev.piny.pineLib.PineLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ContainerMenu {
    private Inventory inventory;
    private HashMap<Integer, MenuItem> menuItems;
    private final Map<UUID, Listener> playerListeners = new HashMap<>();
    private Consumer<InventoryCloseEvent> closeAction;

    /**
     * Generic container menu used for all inventory types that don't modify the items within (e.g. chest, crafter, dropper, etc.)
     * @param inventory The bukkit Inventory to use for this menu.
     */
    public ContainerMenu(@NotNull Inventory inventory) {
        this.inventory = inventory;
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

    /**
     * Sets a MenuItem at the specified slot in the inventory, replacing any existing item.
     * @param slot the (0 based) slot index to set the MenuItem at
     * @param menuItem the MenuItem to set
     */
    public void set(int slot, MenuItem menuItem) {
        if (slot < 0 || slot >= inventory.getSize()) {
            throw new IndexOutOfBoundsException("Slot index out of bounds: " + slot);
        }
        inventory.setItem(slot, menuItem.getItem());
        menuItems.put(slot, menuItem);
    }

    /**
     * Gets the MenuItem at the specified slot, or null if none exists.
     * @param slot the (0 based) slot index to get the MenuItem from
     * @return the MenuItem at the specified slot, or null if none exists
     */
    public @Nullable MenuItem get(int slot) {
        return menuItems.get(slot);
    }

    /**
     * Fills the inventory with the specified MenuItem.
     * @param menuItem the MenuItem to fill the inventory with
     */
    public void fill(MenuItem menuItem) {fill(menuItem, false, 0, inventory.getSize() - 1);}

    /**
     * Fills the inventory with the specified MenuItem.
     * @param menuItem the MenuItem to fill the inventory with
     * @param override if true, existing items will be replaced; if false, only empty slots will be filled
     */
    public void fill(MenuItem menuItem, boolean override) {fill(menuItem, override, 0, inventory.getSize() - 1);}

    /**
     * Fills a range of slots in the inventory with the specified MenuItem.
     * @param menuItem the MenuItem to fill the inventory with
     * @param override if true, existing items will be replaced; if false, only empty slots will be filled
     * @param start the (0 based) starting slot index (inclusive)
     */
    public void fill(MenuItem menuItem, boolean override, int start) {fill(menuItem, override, start, inventory.getSize() - 1);}

    /**
     * Fills a range of slots in the inventory with the specified MenuItem.
     * @param menuItem the MenuItem to fill the inventory with
     * @param override if true, existing items will be replaced; if false, only empty slots will be filled
     * @param start the (0 based) starting slot index (inclusive)
     * @param end the (0 based) ending slot index (inclusive)
     * @throws IndexOutOfBoundsException if the start or end indices are out of bounds
     */
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

    /**
     * Shows the menu to the specified player.
     * @param player the player to show the menu to
     * @throws IllegalArgumentException if the player is null or not online
    */
    public void show(@NotNull Player player) {
        if (!player.isOnline()) {
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
     * Sets an action to be performed when the inventory is closed by any player.
     * @param closeAction the action to perform on inventory close
     */
    public void onClose(Consumer<InventoryCloseEvent> closeAction) {
        this.closeAction = closeAction;
        // Register a global listener if not already registered
        if (!playerListeners.containsKey(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            Listener listener = new Listener() {
                @EventHandler
                public void onInventoryClose(InventoryCloseEvent event) {
                    if (Objects.equals(event.getInventory(), inventory)) {
                        if (ContainerMenu.this.closeAction != null) {
                            ContainerMenu.this.closeAction.accept(event);
                        }
                    }
                }
            };
            playerListeners.put(UUID.fromString("00000000-0000-0000-0000-000000000000"), listener);
            Bukkit.getPluginManager().registerEvents(listener, PineLib.getInstance());
        }
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
