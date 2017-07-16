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
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import fr.evercraft.everapi.services.entity.EntityTemplate;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.flag.EntityTemplateFlag;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;

public class FlagEntityDamage extends EntityTemplateFlag {
		
	private final EverWorldGuard plugin;
	
	public FlagEntityDamage(EverWorldGuard plugin) {
		super("ENTITY_DAMAGE");
		
		this.plugin = plugin;
		this.reload();
	}
	
	@Override
	protected Map<String, Set<EntityTemplate>> getConfig() {
		return this.plugin.getConfigFlags().getEntities(this.getName());
	}
	
	@Override
	public String getDescription() {
		return EWMessages.FLAG_ENTITY_DAMAGE_DESCRIPTION.getString();
	}
	
	/*
	 * CollideEntityEvent : Pour les arcs Flame
	 */
	
	public void onCollideEntity(WorldGuardWorld world, CollideEntityEvent event) {
		if (event.isCancelled()) return;
		
		// TODO  Cause "ProjectileSource"
		Optional<Entity> optEntity = event.getCause().get("ProjectileSource", Entity.class);
		if (!optEntity.isPresent()) return;
		
		if (!this.getDefault().contains(optEntity.get())) return;
		
		event.filterEntities(player -> {
			if (!(player instanceof Player)) return true;
			
			if (!world.getRegions(player.getLocation().getPosition()).getFlag((Player) player, player.getLocation(), this).contains(optEntity.get(), (Player) player)) {
				return false;
			}
			return true;
		});
	}
	
	/*
	 * SpawnEntityEvent : Permet désactiver les Witch
	 */
	
	public void onSpawnEntity(SpawnEntityEvent event) {
		if (event.isCancelled()) return;
		
		Optional<EntitySpawnCause> optCause = event.getCause().get(NamedCause.SOURCE, EntitySpawnCause.class);
		if (!optCause.isPresent()) return;
		
		EntitySpawnCause cause = optCause.get();
		if (!cause.getType().equals(SpawnTypes.PROJECTILE)) return;
		if (!(cause.getEntity() instanceof Agent)) return;
		if (!this.getDefault().contains(cause.getEntity())) return;
		
		
		Agent agent = (Agent) cause.getEntity();
		
		if (!agent.getTarget().isPresent()) return;
		if (!(agent.getTarget().get() instanceof Player)) return;
		
		Player player = (Player) agent.getTarget().get();
		EWWorld world = this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld());
		
		if (!world.getRegions(cause.getEntity().getLocation().getPosition()).getFlag((Player) player, cause.getEntity().getLocation(), this)
				.contains(cause.getEntity(), (Player) player)) {
			event.setCancelled(true);
		} else if (!world.getRegions(player.getLocation().getPosition()).getFlag((Player) player, player.getLocation(), this)
				.contains(cause.getEntity(), (Player) player)) {
			event.setCancelled(true);
		}
	}
	
	/*
	 * DamageEntity
	 */
	
	public void onDamageEntity(WorldGuardWorld world, DamageEntityEvent event) {
		if (event.isCancelled()) return;
		
		if (!(event.getTargetEntity() instanceof Player)) return;
		Player player = (Player) event.getTargetEntity();
		Object source = event.getCause().root();
		if (source instanceof IndirectEntityDamageSource) {				
			IndirectEntityDamageSource damageSource = (IndirectEntityDamageSource) source;
			
			// TODO Witch dégât
			if (!(damageSource.getIndirectSource() instanceof Player)) {
				this.onDamageEntity(world, event, damageSource.getIndirectSource(), player);
			}
		} else if (source instanceof EntityDamageSource) {				
			EntityDamageSource damageSource = (EntityDamageSource) source;
			
			if (!(damageSource.getSource() instanceof Player)) {
				this.onDamageEntity(world, event, damageSource.getSource(), player);
			}
		}
	}
	
	public boolean onDamageEntity(WorldGuardWorld world, DamageEntityEvent event, Entity entity, Player player) {
		if (!this.getDefault().contains(entity)) return false;
		
		if (!world.getRegions(player.getLocation().getPosition()).getFlag((Player) player, player.getLocation(), this).contains(entity, player)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}
}
