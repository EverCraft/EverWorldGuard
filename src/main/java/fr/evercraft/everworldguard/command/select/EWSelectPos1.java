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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SelectType;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectPos1 extends ESubCommand<EverWorldGuard> {
	
	public EWSelectPos1(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "pos1");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_POS1_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName())
				.onClick(TextActions.suggestCommand("/" + this.getName()))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return new ArrayList<String>();
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 0) {
			if (source instanceof EPlayer) {
				resultat = this.commandSelectPos1((EPlayer) source);
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

	private boolean commandSelectPos1(final EPlayer player) {
		Vector3i position = player.getLocation().getPosition().toInt();
		
		Optional<Vector3i> pos1 = player.getSelectPos1();
		Optional<Vector3i> pos2 = player.getSelectPos2();
		
		if (pos1.isPresent() && pos1.get().equals(position)) {
			EWMessages.SELECT_POS1_EQUALS.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		if (!player.setSelectPos1(position)) {
			EWMessages.SELECT_POS1_CANCEL.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		if (player.getSelectType().equals(SelectType.CUBOID)) {
			if (!pos2.isPresent()) {
				EWMessages.SELECT_POS1_CUBOID_ONE.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS1_CUBOID_TWO.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.replace("<area>", player.getSelectArea().orElse(0).toString())
					.sendTo(player);
			}
		} else if (player.getSelectType().equals(SelectType.POLY)) {
			player.setSelectPos2(null);
			player.clearSelectPoints();
			EWMessages.SELECT_POS1_POLY.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else if (player.getSelectType().equals(SelectType.CYLINDER)) {
			player.setSelectPos2(null);
			EWMessages.SELECT_POS1_CYLINDER_CENTER.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		return true;
	}
}
