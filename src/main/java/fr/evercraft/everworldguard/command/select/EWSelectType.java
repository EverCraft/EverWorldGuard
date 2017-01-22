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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.LiteralText.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SelectType;
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
		for (SelectType type : SelectType.values()){
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
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		List<String> suggests = new ArrayList<String>();
		if (args.size() == 1) {
			for (SelectType type : SelectType.values()){
				suggests.add(type.getName());
			}
		}
		return suggests;
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 1) {
			if (source instanceof EPlayer) {
				resultat = this.commandSelectType((EPlayer) source, args.get(0));
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}

	private boolean commandSelectType(final EPlayer player, final String type_string) {
		Optional<SelectType> type = SelectType.getSelectType(type_string);
		if (!type.isPresent()) {
			player.sendMessage(this.help(player));
			return false;
		}
		
		if (player.getSelectType().equals(type.get())) {
			EWMessages.SELECT_TYPE_EQUALS.sender()
				.replace("<type>", type.get().getName())
				.sendTo(player);
			return false;
		}
		
		if (!player.setSelectType(type.get())) {
			EWMessages.SELECT_TYPE_CANCEL.sender()
				.replace("<type>", type.get().getName())
				.sendTo(player);
			return false;
		}
		
		player.setSelectPos1(null);
		player.setSelectPos2(null);
		player.clearSelectPoints();
		
		if (player.getSelectType().equals(SelectType.CUBOID)) {
			EWMessages.SELECT_TYPE_CUBOID.sendTo(player);
		} else if (player.getSelectType().equals(SelectType.POLY)) {
			EWMessages.SELECT_TYPE_POLYGONAL.sendTo(player);
		} else if (player.getSelectType().equals(SelectType.CYLINDER)) {
			EWMessages.SELECT_TYPE_CYLINDER.sendTo(player);
		} else {
			player.sendMessage(this.help(player));
			return false;
		}
		return true;
	}
}
