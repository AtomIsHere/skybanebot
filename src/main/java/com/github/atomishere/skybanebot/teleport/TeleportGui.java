/*
 * Copyright (C) 2020 Archie O'Connor
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.atomishere.skybanebot.teleport;

import com.github.atomishere.skybanebot.SkybaneBot;
import com.github.atomishere.skybanebot.service.AbstractService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Objects;

public class TeleportGui extends AbstractService implements Listener {
    private final Inventory inventory = Bukkit.createInventory(null, 9, "Teleport Menu");
    private NamespacedKey locKey;

    public TeleportGui(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
        this.locKey = new NamespacedKey(plugin, "telLoc");
        Arrays.stream(TeleportLocation.values()).map(this::createGuiItem).forEach(inventory::addItem);
    }

    @Override
    public void onStop() {
        inventory.clear();
        locKey = null;
    }

    private ItemStack createGuiItem(TeleportLocation teleportLocation) {
        ItemStack item = new ItemStack(teleportLocation.getDisplayMat(), 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(teleportLocation.getDisplayColor() + teleportLocation.getDisplayName());
        meta.getPersistentDataContainer().set(locKey, TeleportLocation.DATA_TYPE, teleportLocation);

        item.setItemMeta(meta);

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!Objects.equals(event.getInventory(), this.inventory)) {
            return;
        }

        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        HumanEntity player = event.getWhoClicked();
        TeleportLocation location = item.getItemMeta().getPersistentDataContainer().get(this.locKey, TeleportLocation.DATA_TYPE);
        if (location != null) {
            player.teleport(new Location(player.getWorld(), location.getX(), location.getY(), location.getZ()));
        }
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (Objects.equals(event.getInventory(), this.inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            event.getPlayer().openInventory(inventory);
        }
    }
}
