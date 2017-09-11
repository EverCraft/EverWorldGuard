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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.EntityTemplateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class FlagInteractEntity extends EntityTemplateFlag {
	
	private final EverWorldGuard plugin;
	
	public FlagInteractEntity(EverWorldGuard plugin) {
		super("INTERACT_ENTITY");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<EntityTemplate>> getConfig() {
		return this.plugin.getConfigFlags().getEntities(this.getName());
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_INTERACT_ENTITY_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Entity entity) {
		Vector3i position = entity.getLocation().getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_INTERACT_ENTITY_MESSAGE.sender()
					.replace("{x}", position.getX())
					.replace("{y}", position.getY())
					.replace("{z}", position.getZ())
					.replace("{entity}", entity.getType().getTranslation()));
	}
	
	/*
	 * InteractEntity
	 */

	public void onInteractEntity(WorldGuardWorld world, InteractEntityEvent event) {
		if (event.isCancelled()) return;
		if (!this.getDefault().contains(event.getTargetEntity())) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (optPlayer.isPresent()) {
			this.onInteractEntityPlayer(world, event, optPlayer.get());
		} else {
			this.onInteractEntityNatural(world, event);
		}
	}
	
	public void onInteractEntityPlayer(WorldGuardWorld world, InteractEntityEvent event, Player player) {
		// Bypass
		if (this.plugin.getProtectionService().hasBypass(player)) return;
		
		Location<World> location = event.getTargetEntity().getLocation();
		if (!world.getRegions(location.getPosition()).getFlag(player, location, this).contains(event.getTargetEntity(), player)) {
			event.setCancelled(true);
			this.sendMessage(player, event.getTargetEntity());
		}
	}
	
	public void onInteractEntityNatural(WorldGuardWorld world, InteractEntityEvent event) {
		if (!world.getRegions(event.getTargetEntity().getLocation().getPosition()).getFlagDefault(this).contains(event.getTargetEntity())) {
			event.setCancelled(true);
		}
	}
}
