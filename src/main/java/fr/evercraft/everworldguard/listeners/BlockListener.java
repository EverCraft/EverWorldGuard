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
package fr.evercraft.everworldguard.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.explosive.DetonateExplosiveEvent;
import org.spongepowered.api.event.entity.explosive.PrimeExplosiveEvent;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EverWorldGuard;

public class BlockListener {
	
	private EverWorldGuard plugin;

	public BlockListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Pre event) {
		this.plugin.getManagerFlags().BUILD.onChangeBlockPre(event);
		this.plugin.getManagerFlags().BLOCK_BREAK.onChangeBlockPre(event);
		this.plugin.getManagerFlags().BLOCK_PLACE.onChangeBlockPre(event);
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockPre(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Pre");
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Place event) {		
		this.plugin.getManagerFlags().BUILD.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().BLOCK_PLACE.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().BLOCK_BREAK.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().ENDERMAN_GRIEF.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().FIRE.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().ICE.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().SNOW.onChangeBlockPlace(event);
		this.plugin.getManagerFlags().PROPAGATION.onChangeBlockPlace(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Place : " + String.join(", ", event.getTransactions().stream()
		//		.map(t -> "(" + t.getOriginal().getState().getType().getId() + " : " + t.getFinal().getState().getType().getId() + ")").collect(Collectors.toList())));
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Break event) {
		this.plugin.getManagerFlags().BUILD.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().BLOCK_BREAK.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().ENDERMAN_GRIEF.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().ENDERDRAGON_GRIEF.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().SNOW.onChangeBlockBreak(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Break : " + String.join(", ", event.getTransactions().stream()
		//		.map(t -> "(" + t.getOriginal().getState().getType().getId() + " : " + t.getFinal().getState().getType().getId() + ")").collect(Collectors.toList())));
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		
		this.plugin.getManagerFlags().BUILD.onChangeBlockModify(event);
		this.plugin.getManagerFlags().BLOCK_PLACE.onChangeBlockModify(event);
		this.plugin.getManagerFlags().BLOCK_BREAK.onChangeBlockModify(event);
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockModify(event);
		this.plugin.getManagerFlags().ICE.onChangeBlockModify(event);
		this.plugin.getManagerFlags().SNOW.onChangeBlockModify(event);
		this.plugin.getManagerFlags().PROPAGATION.onChangeBlockModify(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Modify : " + String.join(", ", event.getTransactions().stream()
		//		.map(t -> "(" + t.getOriginal().getState().getType().getId() + " : " + t.getFinal().getState().getType().getId() + ")").collect(Collectors.toList())));
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractBlock(InteractBlockEvent.Secondary event) {
		event.getTargetBlock().getLocation().ifPresent(location -> {
			WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateEWorld(location.getExtent());
		
			this.plugin.getManagerFlags().BUILD.onInteractBlockSecondary(world, event, location);
			this.plugin.getManagerFlags().BLOCK_BREAK.onInteractBlockSecondary(world, event, location);
			this.plugin.getManagerFlags().INTERACT_BLOCK.onInteractBlockSecondary(world, event, location);
			this.plugin.getManagerFlags().FIRE.onInteractBlockSecondary(world, event, location);
		});
		
		// Debug
		//UtilsCause.debug(event.getCause(), "InteractBlockEvent.Secondary");
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideBlock(CollideBlockEvent event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateEWorld(event.getTargetLocation().getExtent());
		
		this.plugin.getManagerFlags().INTERACT_BLOCK.onCollideBlock(world, event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "CollideBlockEvent");
	}
	
	@Listener(order=Order.FIRST)
	public void onDetonateExplosive(DetonateExplosiveEvent event) {
		WorldGuardWorld world = this.plugin.getProtectionService().getOrCreateEWorld(event.getTargetEntity().getLocation().getExtent());
		
		this.plugin.getManagerFlags().EXPLOSION.onDetonateExplosive(event, world);
		this.plugin.getManagerFlags().EXPLOSION_BLOCK.onDetonateExplosive(event, world);
		this.plugin.getManagerFlags().EXPLOSION_DAMAGE.onDetonateExplosive(event, world);
		
		
		// Debug
		//UtilsCause.debug(event.getCause(), "DetonateExplosiveEvent : " + event.getTargetEntity().getClass().getSimpleName() + " : shouldDamageEntities " + event.getExplosionBuilder().build().shouldDamageEntities());
	}
	
	@Listener(order=Order.FIRST)
	public void onDetonateExplosive(PrimeExplosiveEvent event) {
		// Debug
		UtilsCause.debug(event.getCause(), "PrimeExplosiveEvent : " + event.getTargetEntity().getClass().getSimpleName());
	}
}
