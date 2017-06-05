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
package fr.evercraft.everworldguard.listeners.entity;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.event.entity.ChangeEntityPotionEffectEvent;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.HealEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.entity.projectile.TargetProjectileEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EntityListener {
	
	private EverWorldGuard plugin;

	public EntityListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractEntity(InteractEntityEvent.Secondary event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().INTERACT_ENTITY.onInteractEntity(world, event);
		this.plugin.getManagerFlags().BUILD.onInteractEntity(world, event);
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractEntityPrimary(InteractEntityEvent.Primary event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().DAMAGE_ENTITY.onInteractEntityPrimary(world, event);
	}
	
	@Listener(order=Order.FIRST)
	public void onDamageEntity(DamageEntityEvent event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().PVP.onDamageEntity(world, event);
		this.plugin.getManagerFlags().INVINCIBILITY.onDamageEntity(world, event);
		this.plugin.getManagerFlags().DAMAGE_ENTITY.onDamageEntity(world, event);
		this.plugin.getManagerFlags().ENTITY_DAMAGE.onDamageEntity(world, event);
		this.plugin.getManagerFlags().BUILD.onDamageEntity(world, event);
		
		//UtilsCause.debug(event.getCause(), "DamageEntityEvent");
	}
	
	@Listener(order=Order.FIRST)
	public void onDamageEntity(DestructEntityEvent event) {
		//UtilsCause.debug(event.getCause(), "DestructEntityEvent");
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractEntity(InteractEntityEvent.Primary event) {
		//UtilsCause.debug(event.getCause(), "InteractEntityEvent.Primary ");
	}
	
	@Listener(order=Order.FIRST)
	public void onSpawnEntity(SpawnEntityEvent event) {
		this.plugin.getManagerFlags().ENTITY_SPAWNING.onSpawnEntity(event);
		this.plugin.getManagerFlags().ITEM_DROP.onSpawnEntity(event);
		this.plugin.getManagerFlags().POTION_SPLASH.onSpawnEntity(event);
		//this.plugin.getManagerFlags().EXP_DROP.onSpawnEntity(event);
		
		// Debug 
		//UtilsCause.debug(event.getCause(), "SpawnEntityEvent : " + String.join(", ", event.getEntities().stream().map(entity -> entity.getType().getName()).collect(Collectors.toList())));
	}
	
	@Listener(order=Order.FIRST)
	public void onDropItemPre(DropItemEvent.Pre event) {
		this.plugin.getManagerFlags().INVENTORY_DROP.onDropItemPre(event);
		this.plugin.getManagerFlags().ITEM_DROP.onDropItemPre(event);
		
		// Debug 
		//UtilsCause.debug(event.getCause(), "DropItemEvent : " + String.join(", ", event.getDroppedItems().stream().map(entity -> entity.getType().getName()).collect(Collectors.toList())));
	}
	
	@Listener(order=Order.FIRST)
	public void onDropItemPree(LaunchProjectileEvent event) {
		// Debug 
		//UtilsCause.debug(event.getCause(), event.getClass().getSimpleName());
	}
	
	@Listener(order=Order.FIRST)
	public void onConstructEntityPre(ConstructEntityEvent.Pre event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getTransform().getExtent());
		
		this.plugin.getManagerFlags().LIGHTNING.onConstructEntityPre(world, event);
		
		// Debug 
		//UtilsCause.debug(event.getCause(), "ConstructEntityEvent.Pre : " + event.getTargetType().getName());
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerHeal(HealEntityEvent event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().INVINCIBILITY.onHealEntity(world, event);
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideEntityImpact(CollideEntityEvent.Impact event) {
		if (event.getEntities().isEmpty()) return;
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getEntities().get(0).getWorld());

		this.plugin.getManagerFlags().PVP.onCollideEntityImpact(world, event);
		this.plugin.getManagerFlags().DAMAGE_ENTITY.onCollideEntityImpact(world, event);
		this.plugin.getManagerFlags().BUILD.onCollideEntityImpact(world, event);
		
		//UtilsCause.debug(event.getCause(), "CollideEntityEvent.Impact");
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideEntityImpact(CollideEvent.Impact event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateWorld(event.getImpactPoint().getExtent());

		this.plugin.getManagerFlags().POTION_SPLASH.onCollideImpact(world, event);
		
		//UtilsCause.debug(event.getCause(), "CollideEvent.Impact");
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeEntityPotionEffect(ChangeEntityPotionEffectEvent event) {
		//UtilsCause.debug(event.getCause(), "ChangeEntityPotionEffectEvent");
	}
	
	@Listener(order=Order.FIRST)
	public void onTargetProjectile(TargetProjectileEvent event) {
		//UtilsCause.debug(event.getCause(), "TargetProjectileEvent");
	}
}
