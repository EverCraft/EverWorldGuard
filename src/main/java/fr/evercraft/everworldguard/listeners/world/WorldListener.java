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
package fr.evercraft.everworldguard.listeners.world;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everworldguard.EverWorldGuard;

public class WorldListener {
	
	private EverWorldGuard plugin;

	public WorldListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.PRE)
	public void onLoadWorld(LoadWorldEvent event) {
		this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
	}
	
	@Listener(order=Order.PRE)
	public void onUnloadWorld(UnloadWorldEvent event) {
		this.plugin.getProtectionService().unLoadWorld(event.getTargetWorld());
	}
	
	@Listener(order=Order.FIRST)
	public void onDetonateExplosive(DetonateExplosiveEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetEntity().getLocation().getExtent());
		
		this.plugin.getManagerFlags().EXPLOSION.onDetonateExplosive(event, world);
		this.plugin.getManagerFlags().EXPLOSION_BLOCK.onDetonateExplosive(event, world);
		this.plugin.getManagerFlags().EXPLOSION_DAMAGE.onDetonateExplosive(event, world);
		
		
		// Debug
		//UtilsCause.debug(event.getCause(), "DetonateExplosiveEvent : " + event.getTargetEntity().getClass().getSimpleName() + " : shouldDamageEntities " + event.getExplosionBuilder().build().shouldDamageEntities());
	}
}
