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
package fr.evercraft.everworldguard.command.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectType extends ESubCommand<EverWorldGuard> {
	
	public EWSelectType(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "type");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_TYPE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		Builder build = Text.builder("/" + this.getName() + " <");
		
		List<Text> populator = new ArrayList<Text>();
		for (SelectionRegion.Type type : this.plugin.getGame().getRegistry().getAllOf(SelectionRegion.Type.class)){
			populator.add(Text.builder(type.getName())
								.onClick(TextActions.suggestCommand("/" + this.getName() + " " + type.getName().toUpperCase()))
								.build());
		}
		build.append(Text.joinWith(Text.of("|"), populator));
		return build.append(Text.of(">"))
					.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
					.color(TextColors.RED)
					.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			List<String> suggests = new ArrayList<String>();
			for (SelectionRegion.Type type : this.plugin.getGame().getRegistry().getAllOf(SelectionRegion.Type.class)){
				suggests.add(type.getName());
			}
			return suggests;
		}
		return Arrays.asList();
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			if (source instanceof EPlayer) {
				return this.commandSelectType((EPlayer) source, args.get(0));
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return CompletableFuture.completedFuture(false);
	}

	private CompletableFuture<Boolean> commandSelectType(final EPlayer player, final String type_string) {
		Optional<SelectionRegion.Type> type = this.plugin.getGame().getRegistry().getType(SelectionRegion.Type.class, type_string);
		if (!type.isPresent()) {
			player.sendMessage(this.help(player));
			return CompletableFuture.completedFuture(false);
		}
		
		if (player.getSelectorType().equals(type.get())) {
			EWMessages.SELECT_TYPE_EQUALS.sender()
				.replace("{type}", type.get().getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!player.setSelectorType(type.get())) {
			EWMessages.SELECT_TYPE_CANCEL.sender()
				.replace("{type}", type.get().getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		if (player.getSelectorType().equals(SelectionRegion.Types.CUBOID)) {
			EWMessages.SELECT_TYPE_CUBOID.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.EXTEND)) {
			EWMessages.SELECT_TYPE_EXTEND.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.POLYGONAL)) {
			EWMessages.SELECT_TYPE_POLYGONAL.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.CYLINDER)) {
			EWMessages.SELECT_TYPE_CYLINDER.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.ELLIPSOID)) {
			EWMessages.SELECT_TYPE_ELLIPSOID.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.SPHERE)) {
			EWMessages.SELECT_TYPE_SPHERE.sendTo(player);
		} else {
			player.sendMessage(this.help(player));
			return CompletableFuture.completedFuture(false);
		}
		
		return CompletableFuture.completedFuture(true);
	}
}
