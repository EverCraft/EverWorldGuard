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

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.flag.type.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagPvp extends StateFlag {

	public FlagPvp() {
		super("PVP");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_PVP.getString();
	}

	@Override
	public State getDefault() {
		return State.ALLOW;
	}
	
	public void onPlayerDamage(WorldWorldGuard world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (event.getTargetEntity() instanceof Player) {
			Player player = (Player) event.getTargetEntity();
			
			Optional<EntityDamageSource> optDamageSource = event.getCause().first(EntityDamageSource.class);
			if (optDamageSource.isPresent() && optDamageSource.get().getSource() instanceof Player) {
				if (world.getRegions(player.getLocation().getPosition()).getFlag(player, Flags.PVP).equals(State.DENY)) {
					event.setCancelled(true);
				}
			}
		}
	}
}
