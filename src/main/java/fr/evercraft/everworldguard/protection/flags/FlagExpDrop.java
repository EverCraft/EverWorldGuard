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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class FlagExpDrop extends StateFlag {
	
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
		
		// TODO Bug : SpawnTypes.EXPERIENCE
		
		Optional<EntitySpawnCause> optCause = event.getCause().get(NamedCause.SOURCE, EntitySpawnCause.class);
		if (!optCause.isPresent()) return;
		EntitySpawnCause cause = optCause.get();
		
		if (!cause.getType().equals(SpawnTypes.EXPERIENCE)) return;
		
		if (!(cause.getEntity() instanceof Player)) return;
		Player player = (Player) cause.getEntity();
		EProtectionService service = this.plugin.getProtectionService();
		
		event.filterEntities(entity -> {
			if (entity instanceof ExperienceOrb) {
				return service.getOrCreateEWorld(entity.getWorld()).getRegions(entity.getLocation().getPosition()).getFlag(player, entity.getLocation(), this).equals(State.ALLOW);
			}
			return true;
		});
	}
}
