/*
 * This file is part of EverWorldGuard.
 *
 * EverWorldGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverWorldGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverWorldGuard.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.protection.flags;

import java.util.Optional;

import fr.evercraft.everapi.server.location.VirtualTransform;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.WorldGuardService.Priorities;
import fr.evercraft.everapi.services.worldguard.flag.LocationFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagSpawn extends LocationFlag {

	public FlagSpawn(EverWorldGuard plugin) {
		super(plugin, "SPAWN");
	}
	
	public void register() {
		this.plugin.getEverAPI().getManagerService().getSpawn().register(Priorities.FLAG, user -> {
			if (user instanceof EPlayer) {
				EPlayer player = (EPlayer) user;

				return ((EverWorldGuard) this.plugin).getProtectionService().getOrCreateEWorld(player.getWorld())
					.getRegions(player.getLocation().getPosition()).getFlag(user, player.getLocation(), this).getTransform();
			}
			return Optional.empty();
		});
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_SPAWN_DESCRIPTION.getString();
	}

	@Override
	public VirtualTransform getDefault() {
		return VirtualTransform.empty();
	}
}
