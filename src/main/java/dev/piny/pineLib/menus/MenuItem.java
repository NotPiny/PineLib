package dev.piny.pineLib.menus;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class MenuItem {
    private ItemStack item;
    private Consumer<InventoryClickEvent> clickAction;

    public MenuItem(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        this.item = item;
        this.clickAction = clickAction;
    }

    public static MenuItem of(Material item, Consumer<InventoryClickEvent> clickAction) {
        return new MenuItem(new ItemStack(item), clickAction);
    }
    public static MenuItem of(ItemStack item, Consumer<InventoryClickEvent> clickAction) {
        return new MenuItem(item, clickAction);
    }

    public static MenuItem of(Material material) {
        return new MenuItem(new ItemStack(material), event -> {});
    }
    public static MenuItem of(ItemStack item) {
        return new MenuItem(item, event -> {});
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Consumer<InventoryClickEvent> getClickAction() {
        return clickAction;
    }

    public void setClickAction(Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
    }
}
