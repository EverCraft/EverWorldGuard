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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.flag.type.EntityTemplateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class FlagEntitySpawning extends EntityTemplateFlag {
	
	private final EverWorldGuard plugin;
	
	public FlagEntitySpawning(EverWorldGuard plugin) {
		super("ENTITY_SPAWNING");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<EntityTemplate>> getConfig() {
		return this.plugin.getProtectionService().getConfigFlags().getEntities(this.getName());
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_SPAWN_ENTITY_DESCRIPTION.getString();
	}
	
	public boolean sendMessage(Player player, Entity entity) {
		Vector3i position = entity.getLocation().getPosition().toInt();
		return this.plugin.getProtectionService().sendMessage(player, this,
				EWMessages.FLAG_SPAWN_ENTITY_MESSAGE.sender()
					.replace("<x>", position.getX())
					.replace("<y>", position.getY())
					.replace("<z>", position.getZ())
					.replace("<entity>", entity.getType().getTranslation()));
	}
	
	/*
	 * CollideEntity
	 */
	
	public void onSpawnEntity(SpawnEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.OWNER, Player.class);
		if (optPlayer.isPresent()) {
			this.onSpawnEntityPlayer(this.plugin.getProtectionService(), event, optPlayer.get());
		} else {
			this.onSpawnEntityNatural(this.plugin.getProtectionService(), event);
		}
	}
	
	public void onSpawnEntityPlayer(EProtectionService service, SpawnEntityEvent event, Player player) {
		List<? extends Entity> filter = event.filterEntities(entity -> {
			if (this.getDefault().contains(entity) && 
					!service.getOrCreateEWorld(entity.getWorld()).getRegions(entity.getLocation().getPosition()).getFlag(player, this).contains(entity, player)) {
				return false;
			}
			return true;
		});
		
		if (!filter.isEmpty()) {
			this.sendMessage(player, filter.get(0));
		}
	}
	
	public void onSpawnEntityNatural(EProtectionService service, SpawnEntityEvent event) {		
		event.filterEntities(entity -> {
			if (this.getDefault().contains(entity) && 
					!service.getOrCreateEWorld(entity.getWorld()).getRegions(entity.getLocation().getPosition()).getFlagDefault(this).contains(entity)) {
				return false;
			}
			return true;
		});
	}
	
	public void onInteractItem(InteractItemEvent event) {
		if (event.isCancelled()) return;
		
		if (!event.getItemStack().getType().equals(ItemTypes.SPAWN_EGG)) return;
		if (!event.getInteractionPoint().isPresent()) return;
		
		Optional<Player> optPlayer = event.getCause().get(NamedCause.SOURCE, Player.class);
		if (!optPlayer.isPresent()) return;
		
		Optional<EntityType> type = event.getItemStack().get(Keys.SPAWNABLE_ENTITY_TYPE);
		if (!type.isPresent()) return;
		
		Player player = optPlayer.get();
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(player.getWorld());
		Entity entity = player.getWorld().createEntity(type.get(), event.getInteractionPoint().get());
		
		if (this.getDefault().contains(entity) && 
				!world.getRegions(event.getInteractionPoint().get()).getFlag(player, this).contains(entity, player)) {
			event.setCancelled(true);
			this.sendMessage(player, entity);
		}
	}
}
