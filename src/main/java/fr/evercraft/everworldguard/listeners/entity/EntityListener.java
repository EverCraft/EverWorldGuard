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
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EntityListener {
	
	private EverWorldGuard plugin;

	public EntityListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractEntity(InteractEntityEvent.Secondary event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().INTERACT_ENTITY.onInteractEntity(world, event);
		this.plugin.getManagerFlags().BUILD.onInteractEntity(world, event);
	}
	
	@Listener(order=Order.FIRST)
	public void onPlayerDamage(DamageEntityEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getWorld());
		
		this.plugin.getManagerFlags().PVP.onDamageEntity(world, event);
		this.plugin.getManagerFlags().DAMAGE_ENTITY.onDamageEntity(world, event);
		this.plugin.getManagerFlags().BUILD.onDamageEntity(world, event);
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideEntity(CollideEntityEvent event) {
		if (event.getEntities().isEmpty()) return;
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getEntities().get(0).getWorld());

		this.plugin.getManagerFlags().PVP.onCollideEntity(world, event);
		this.plugin.getManagerFlags().DAMAGE_ENTITY.onCollideEntity(world, event);
		this.plugin.getManagerFlags().BUILD.onCollideEntity(world, event);
	}
}
