package dev.piny.pineLib.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

public class ChestMenu extends ContainerMenu {
    /**
     * Creates a chest with the specified title and number of rows.
     * @param title the title of the chest
     * @param rows the number of rows (1-6)
     * @throws IllegalArgumentException if rows is not between 1 and 6
     */
    public ChestMenu(Component title, int rows) {
        super(Bukkit.createInventory(null, rows * 9, title));

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6.");
        }
    }
}
