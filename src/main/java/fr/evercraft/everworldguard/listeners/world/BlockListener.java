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
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everworldguard.EverWorldGuard;

public class BlockListener {
	
	private EverWorldGuard plugin;

	public BlockListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	/*
	 * Debug
	 */
	
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
		this.plugin.getManagerFlags().ENDERMAN_GRIEF.onChangeBlockPlace(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Place");
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Break event) {
		this.plugin.getManagerFlags().BUILD.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().BLOCK_BREAK.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().ENDERMAN_GRIEF.onChangeBlockBreak(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Break");
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockModify(event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Modify");
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Decay event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Decay");	
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Post event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Post");
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Grow event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		//UtilsCause.debug(event.getCause(), "ChangeBlockEvent.Grow");
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractBlock(InteractBlockEvent.Secondary event) {
		event.getTargetBlock().getLocation().ifPresent(location -> {
			WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(location.getExtent());
		
			this.plugin.getManagerFlags().INTERACT_BLOCK.onInteractBlockSecondary(world, event, location);
		});
		
		// Debug
		//UtilsCause.debug(event.getCause(), "InteractBlockEvent.Secondary");
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideBlock(CollideBlockEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetLocation().getExtent());
		
		this.plugin.getManagerFlags().INTERACT_BLOCK.onCollideBlock(world, event);
		
		// Debug
		//UtilsCause.debug(event.getCause(), "CollideBlockEvent");
	}
}
