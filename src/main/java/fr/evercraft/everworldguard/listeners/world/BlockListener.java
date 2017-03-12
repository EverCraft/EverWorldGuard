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

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

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
		/*List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of()))
					.onClick(TextActions.suggestCommand(""))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.of("Pre : ").concat(Text.joinWith(Text.of(", "), list)));
		*/
	}
	
	@Listener(order=Order.FIRST)
	public void onChangeBlock(ChangeBlockEvent event) {
		if (event.isCancelled()) return;
		/*
		List<Text> list = new ArrayList<Text>();
		event.getCause().getNamedCauses().forEach((key, value) -> {
			list.add(Text.builder(key)
					.onHover(TextActions.showText(Text.of()))
					.onClick(TextActions.suggestCommand(""))
					.build());
		});
		this.plugin.getEServer().getBroadcastChannel().send(Text.builder("Autre : ")
				.onHover(TextActions.showText(Text.of(event.getClass().getName())))
				.onClick(TextActions.suggestCommand(event.getClass().getName()))
				.build().concat(Text.joinWith(Text.of(", "), list)));
				*/
	}
}
