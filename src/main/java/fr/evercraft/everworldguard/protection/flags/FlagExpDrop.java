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

import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagExpDrop extends StateFlag {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;

	public FlagExpDrop(EverWorldGuard plugin) {
		super("EXP_DROP");
		this.plugin = plugin;
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_EXP_DROP_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}

	public void onSpawnEntity(SpawnEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<SpawnCause> optCause = event.getCause().get(NamedCause.SOURCE, SpawnCause.class);
		if (!optCause.isPresent()) return;
		SpawnCause cause = optCause.get();
		
		if (!cause.getType().equals(SpawnTypes.CUSTOM)) return;
		
		event.filterEntities(entity -> {
			if (entity instanceof ExperienceOrb) {
				return false;
			}
			return true;
		});
	}
}
