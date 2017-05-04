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
		
		// Debug
		/*
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.of("ChangeBlockEvent.Pre : ").concat(Text.joinWith(Text.of(", "), list)));
		
		event.getLocations().forEach(location -> this.plugin.getEServer().getBroadcastChannel().send(Text.of("   - " + location.getPosition())));*/
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Place event) {		
		this.plugin.getManagerFlags().BUILD.onChangeBlockPlace(event);
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Place : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Break event) {
		this.plugin.getManagerFlags().BUILD.onChangeBlockBreak(event);
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockBreak(event);
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Break : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
		
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		
		this.plugin.getManagerFlags().INTERACT_BLOCK.onChangeBlockModify(event);
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Modify : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Decay event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Decay : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Post event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Post : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Grow event) {
		//WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld());
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Grow : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
	}
	
	@Listener(order=Order.FIRST)
	public void onInteractBlock(InteractBlockEvent.Secondary event) {
		event.getTargetBlock().getLocation().ifPresent(location -> {
			WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(location.getExtent());
		
			this.plugin.getManagerFlags().INTERACT_BLOCK.onInteractBlockSecondary(world, event, location);
		});
	}
	
	@Listener(order=Order.FIRST)
	public void onCollideBlock(CollideBlockEvent event) {
		WorldWorldGuard world = this.plugin.getProtectionService().getOrCreateWorld(event.getTargetLocation().getExtent());
		
		this.plugin.getManagerFlags().INTERACT_BLOCK.onCollideBlock(world, event);
		
		// Debug
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("CollideBlockEvent : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));*/
	}
}
