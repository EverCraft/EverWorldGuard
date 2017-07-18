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

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.DamageEntityEvent;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.StateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagInvincibility extends StateFlag {

	public FlagInvincibility() {
		super("INVINCIBILITY");
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INVINCIBILITY_DESCRIPTION.getString();
	}

	@Override
	public State getDefault() {
		return State.DENY;
	}
	
	public void onDamageEntity(WorldGuardWorld world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (!(event.getTargetEntity() instanceof Player)) return;
		Player player = (Player) event.getTargetEntity();
		
		if (world.getRegions(player.getLocation().getPosition()).getFlag(player, player.getLocation(), this).equals(State.ALLOW)) {
			event.setCancelled(true);
			player.offer(Keys.FIRE_TICKS, 0);
		}
	}
}
