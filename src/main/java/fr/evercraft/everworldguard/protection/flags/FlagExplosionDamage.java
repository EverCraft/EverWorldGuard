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

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.EntityTemplateFlag;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public class FlagExplosionDamage extends EntityTemplateFlag {
	
	private final EverWorldGuard plugin;
	
	public FlagExplosionDamage(EverWorldGuard plugin) {
		super("EXPLOSION_DAMAGE");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<EntityTemplate>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().getEntities(this.getName());
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_EXPLOSION_DAMAGE_DESCRIPTION.getString();
	}
	
	public void onDetonateExplosive(DetonateExplosiveEvent event, WorldGuardWorld world) {
		if (event.isCancelled()) return;
		
		// TODO Bug : OWNER
		Optional<Player> optPlayer = event.getCause().first(Player.class);
		if (optPlayer.isPresent()) {
			this.onDetonateExplosivePlayer(event, world, optPlayer.get());
		} else {
			this.onDetonateExplosiveNatural(event, world);
		}
	}
	
	public void onDetonateExplosivePlayer(DetonateExplosiveEvent event, WorldGuardWorld world, Player player) {
		if (this.getDefault().contains(event.getTargetEntity()) && 
				!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlag(player, this).contains(event.getTargetEntity(), player)) {
			
			// TODO shouldDamageEntities : https://github.com/SpongePowered/SpongeCommon/issues/1367
			event.getExplosionBuilder().shouldDamageEntities(false);
		}
	}
	
	public void onDetonateExplosiveNatural(DetonateExplosiveEvent event, WorldGuardWorld world) {
		if (this.getDefault().contains(event.getTargetEntity()) && 
				!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlagDefault(this).contains(event.getTargetEntity())) {
			
			// TODO shouldDamageEntities : https://github.com/SpongePowered/SpongeCommon/issues/1367
			event.getExplosionBuilder().shouldDamageEntities(false);
		}
	}
}
