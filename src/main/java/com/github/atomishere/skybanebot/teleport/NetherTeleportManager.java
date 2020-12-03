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
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;

public class NetherTeleportManager extends AbstractService {
    public NetherTeleportManager(SkybaneBot plugin) {
        super(plugin);
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onStop() {
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) && event.getFrom().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            event.setCancelled(true);
            Player target = event.getPlayer();
            target.teleport(target.getBedSpawnLocation() == null ? event.getTo().getWorld().getSpawnLocation() : target.getBedSpawnLocation());
        }
    }
}
