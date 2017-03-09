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
import fr.evercraft.everapi.services.selection.SelectionType;
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
		return Arrays.asList();
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
		
		if (player.getSelectorType().equals(SelectionType.CUBOID) ||
				player.getSelectorType().equals(SelectionType.EXTEND)) {
			return this.commandSelectPos1Cuboid(player, position);
		} else if (player.getSelectorType().equals(SelectionType.POLYGONAL)) {
			return this.commandSelectPos1Polygonal(player, position);
		} else if (player.getSelectorType().equals(SelectionType.CYLINDER) || 
					player.getSelectorType().equals(SelectionType.ELLIPSOID) || 
					player.getSelectorType().equals(SelectionType.SPHERE)) {
			return this.commandSelectPos1Cylinder(player, position);
		} else {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
	}
	
	private boolean commandSelectPos1Cuboid(final EPlayer player, Vector3i position) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Vector3i> pos2 = player.getSelectorSecondary();
		
		if (pos1.isPresent() && pos1.get().equals(position)) {
			EWMessages.SELECT_POS1_EQUALS.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		if (!player.setSelectorPrimary(position)) {
			EWMessages.SELECT_POS1_CANCEL.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		if (!pos2.isPresent()) {
			EWMessages.SELECT_POS1_ONE.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else {
			EWMessages.SELECT_POS1_TWO.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.replace("<area>", String.valueOf(player.getSelectorVolume()))
				.sendTo(player);
		}
		return true;
	}
	
	private boolean commandSelectPos1Polygonal(final EPlayer player, Vector3i position) {
		if (!player.setSelectorPrimary(position)) {
			EWMessages.SELECT_POS1_CANCEL.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		EWMessages.SELECT_POS1_POLY.sender()
			.replace("<pos>", EWSelect.getPositionHover(position))
			.sendTo(player);
		return true;
	}
	
	private boolean commandSelectPos1Cylinder(final EPlayer player, Vector3i position) {		
		if (!player.setSelectorPrimary(position)) {
			EWMessages.SELECT_POS1_CANCEL.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		EWMessages.SELECT_POS1_CENTER.sender()
			.replace("<pos>", EWSelect.getPositionHover(position))
			.sendTo(player);
		return true;
	}
}
