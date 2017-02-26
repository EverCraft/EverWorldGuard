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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.plugin.command.ECommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWCui extends ECommand<EverWorldGuard> {

	public EWCui(final EverWorldGuard plugin) {
		super(plugin, "we");
	}

	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.SELECT_CUI.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_CUI_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <cui>")
					.onClick(TextActions.suggestCommand("/" + this.getName()))
					.color(TextColors.RED)
					.build();
	}

	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			return Arrays.asList("cui");
		}
		return Arrays.asList();
	}

	@Override
	public boolean execute(CommandSource source, final List<String> args) throws CommandException {
		// Erreur : Context 
		if(source instanceof EPlayer) {
			source = ((EPlayer) source).get();
		}
		
		// Résultat de la commande :
		boolean resultat = false;
		
		// Si on ne connait pas le joueur
		if (args.size() == 1 && args.get(0).equalsIgnoreCase("cui")) {
			resultat = this.commandCui(source, "\"" + args.get(0) + "\"");
		// Nombre d'argument incorrect
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}
	
	public boolean commandCui(final CommandSource player, final String world) throws CommandException {
		this.plugin.getGame().getCommandManager().process(player, "select cui");
		return false;
	}
}
