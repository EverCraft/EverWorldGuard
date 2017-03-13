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

import fr.evercraft.everworldguard.EverWorldGuard;

public class BlockListener {
	
	@SuppressWarnings("unused")
	private EverWorldGuard plugin;

	public BlockListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	/*
	 * Debug
	 */
	/*
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Pre event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.of("ChangeBlockEvent.Pre : ").concat(Text.joinWith(Text.of(", "), list)));
		
		event.getLocations().forEach(location -> this.plugin.getEServer().getBroadcastChannel().send(Text.of("   - " + location.getPosition())));
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Modify event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Modify : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Break event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Break : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Decay event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Decay : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Post event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Post : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Grow event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Grow : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent.Place event) {
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of(EChat.fixLength(value.toString(), 254))))
					.onClick(TextActions.suggestCommand(EChat.fixLength(value.toString(), 254)))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("ChangeBlockEvent.Place : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));				
	}*/
}
