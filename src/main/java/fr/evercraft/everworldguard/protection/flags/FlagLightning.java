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

import java.util.Set;

import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagLightning extends StateFlag {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;

	public FlagLightning(EverWorldGuard plugin) {
		super("LIGHTNING");
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_LIGHTNING_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	@Override
	public Set<Group> getGroups() {
		return ImmutableSet.of(Groups.DEFAULT);
	}
	
	public void onConstructEntityPre(WorldGuardWorld world, ConstructEntityEvent.Pre event) {
		if (event.isCancelled()) return;
		if (!event.getTargetType().equals(EntityTypes.LIGHTNING)) return;
		
		if (world.getRegions(event.getTransform().getPosition()).getFlagDefault(this).equals(State.DENY)) {
			event.setCancelled(true);
		}
	}
}
