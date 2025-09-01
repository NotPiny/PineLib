package dev.piny.pineLib.menus;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import dev.piny.pineLib.PineLib;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class AnvilMenu implements Listener {
    private static final boolean PROTOCOLLIB_AVAILABLE;

    static {
        boolean found;
        try {
            Class.forName("com.comphenix.protocol.ProtocolLibrary");
            found = Bukkit.getPluginManager().getPlugin("ProtocolLib") != null;
        } catch (ClassNotFoundException e) {
            found = false;
        }
        PROTOCOLLIB_AVAILABLE = found;
    }

    private Component menuTitle;
    private ItemStack startingItem;
    private ItemStack resultItem;  // New field for the result item
    private boolean autoClose = true;
    private Consumer<ItemStack> clickAction;
    private final Map<UUID, Listener> playerListeners = new HashMap<>();
    private final Map<UUID, PacketAdapter> protocolLibAdapters = new HashMap<>();
    private final Map<UUID, String> pendingRenameText = new HashMap<>();

    public AnvilMenu(Component title, ItemStack startingItem, boolean autoClose, Consumer<ItemStack> clickAction) {
        this.menuTitle = title;
        this.startingItem = startingItem;
        this.resultItem = startingItem.clone();  // By default, use a clone of the starting item as the result
        this.clickAction = clickAction;
        this.autoClose = autoClose;

        if (PROTOCOLLIB_AVAILABLE) {
            Bukkit.getLogger().info("[AnvilMenu] ProtocolLib detected and will be used for advanced anvil input handling.");
        } else {
            Bukkit.getLogger().warning("[AnvilMenu] ProtocolLib not found! Falling back to Bukkit events for anvil input. Some features may be less reliable.");
        }
    }

    public Component getMenuTitle() { return menuTitle; }
    public void setMenuTitle(Component menuTitle) { this.menuTitle = menuTitle; }
    public ItemStack getStartingItem() { return startingItem; }
    public void setStartingItem(ItemStack startingItem) { this.startingItem = startingItem; }
    public ItemStack getResultItem() { return resultItem; }
    public void setResultItem(ItemStack resultItem) { this.resultItem = resultItem; }
    public void setClickAction(Consumer<ItemStack> clickAction) { this.clickAction = clickAction; }

    public void show(Player player) {
        UUID playerUUID = player.getUniqueId();
        // Clean up previous listener for this player if it exists
        if (playerListeners.containsKey(playerUUID)) {
            HandlerList.unregisterAll(playerListeners.get(playerUUID));
            playerListeners.remove(playerUUID);
        }
        // Clean up ProtocolLib adapter if it exists
        if (protocolLibAdapters.containsKey(playerUUID)) {
            ProtocolLibrary.getProtocolManager().removePacketListener(protocolLibAdapters.get(playerUUID));
            protocolLibAdapters.remove(playerUUID);
        }
        pendingRenameText.remove(playerUUID);

        Inventory anvilInventory = Bukkit.createInventory(null, InventoryType.ANVIL, menuTitle);
        anvilInventory.setItem(0, startingItem); // Set the starting item in the first slot

        player.openInventory(anvilInventory);

        Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
            if (player.getOpenInventory().getType() == InventoryType.ANVIL) {
                player.getOpenInventory().getTopInventory().setItem(2, resultItem);
                player.updateInventory();
            }
        }, 2L);

        // ProtocolLib Listener for rename text
        if (PROTOCOLLIB_AVAILABLE) {
            PacketAdapter adapter = new PacketAdapter(PineLib.getInstance(), PacketType.Play.Client.ITEM_NAME) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    if (!event.getPlayer().getUniqueId().equals(playerUUID))
                        return;
                    String renameText = event.getPacket().getStrings().read(0);
                    pendingRenameText.put(playerUUID, renameText);
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
            protocolLibAdapters.put(playerUUID, adapter);
        }

        // Bukkit Listener for inventory events
        Listener listener = new Listener() {
            @EventHandler
            public void onInventoryClick(InventoryClickEvent event) {
                if (!(event.getView().getPlayer() instanceof Player p) || !p.getUniqueId().equals(playerUUID))
                    return;
                if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.ANVIL)
                    return;

                // Prevent taking item from slot 0 or 1
                if (event.getSlot() == 0 || event.getSlot() == 1) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
                        if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
                            p.getOpenInventory().getTopInventory().setItem(2, resultItem);
                            p.updateInventory();
                        }
                    }, 1L);
                    return;
                }

                // Handle output slot click (slot 2)
                if (event.getSlot() == 2) {
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem != null && !clickedItem.getType().isAir()) {
                        event.setCancelled(true);
                        // Use ProtocolLib-captured rename text if available
                        String renameText = PROTOCOLLIB_AVAILABLE ? pendingRenameText.get(playerUUID) : null;

                        ItemStack customItem = startingItem.clone();
                        if (renameText != null && !renameText.isEmpty()) {
                            customItem.editMeta(meta -> meta.displayName(net.kyori.adventure.text.Component.text(renameText)));
                        } else if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                            customItem.editMeta(meta -> meta.displayName(clickedItem.getItemMeta().displayName()));
                        }

                        setResultItem(customItem);

                        clickAction.accept(customItem);

                        if (autoClose) {
                            p.closeInventory();
                        }
                    } else {
                        event.setCancelled(true);
                        Bukkit.getScheduler().runTaskLater(PineLib.getInstance(), () -> {
                            if (p.getOpenInventory().getType() == InventoryType.ANVIL) {
                                p.getOpenInventory().getTopInventory().setItem(2, resultItem);
                                p.updateInventory();
                            }
                        }, 1L);
                    }
                }
            }

            @EventHandler(priority = EventPriority.HIGHEST)
            public void onPrepareAnvil(PrepareAnvilEvent event) {
                if (!(event.getView().getPlayer() instanceof Player p) || !p.getUniqueId().equals(playerUUID)) return;
                Inventory anvilInventory = event.getInventory();
                ItemStack result = event.getResult();
                if (result != null && !result.getType().isAir()) {
                    resultItem = result.clone();
                } else {
                    event.setResult(resultItem);
                }
            }

            @EventHandler
            public void onInventoryClose(InventoryCloseEvent event) {
                if (!(event.getPlayer() instanceof Player p) || !p.getUniqueId().equals(playerUUID))
                    return;
                if (event.getInventory().getType() == InventoryType.ANVIL) {
                    HandlerList.unregisterAll(this);
                    playerListeners.remove(playerUUID);
                    // Remove ProtocolLib listener for this player
                    if (protocolLibAdapters.containsKey(playerUUID)) {
                        ProtocolLibrary.getProtocolManager().removePacketListener(protocolLibAdapters.get(playerUUID));
                        protocolLibAdapters.remove(playerUUID);
                    }
                    pendingRenameText.remove(playerUUID);
                }
            }
        };

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
        protocolLibAdapters.values().forEach(adapter -> ProtocolLibrary.getProtocolManager().removePacketListener(adapter));
        protocolLibAdapters.clear();
        pendingRenameText.clear();
    }
}