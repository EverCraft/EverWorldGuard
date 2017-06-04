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
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.exception.SelectorSecondaryException;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectPos2 extends ESubCommand<EverWorldGuard> {
	
	public EWSelectPos2(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "pos2");
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
				resultat = this.commandSelectPos2((EPlayer) source);
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

	private boolean commandSelectPos2(final EPlayer player) {		
		Vector3i position = player.getLocation().getPosition().toInt();
		
		if (player.getSelectorType().equals(SelectionRegion.Types.CUBOID) || 
				player.getSelectorType().equals(SelectionRegion.Types.EXTEND)) {
			return this.commandSelectPos2Cuboid(player, position);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.POLYGONAL)) {
			return this.commandSelectPos2Poly(player, position);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.CYLINDER) ||
					player.getSelectorType().equals(SelectionRegion.Types.ELLIPSOID) ||
					player.getSelectorType().equals(SelectionRegion.Types.SPHERE)) {
			return this.commandSelectPos2Cylinder(player, position);
		}
		
		EAMessages.COMMAND_ERROR.sender()
			.prefix(EWMessages.PREFIX)
			.sendTo(player);
		return false;
	}
	
	private boolean commandSelectPos2Cuboid(final EPlayer player, final Vector3i position) {
		Optional<Vector3i> pos2 = player.getSelectorSecondary();
		
		if (pos2.isPresent() && pos2.get().equals(position)) {
			EWMessages.SELECT_POS2_EQUALS.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		try {
			if (!player.setSelectorSecondary(position)) {
				EWMessages.SELECT_POS2_CANCEL.sender()
					.replace("<position>", EWSelect.getPositionHover(position))
					.sendTo(player);
				return false;
			}
		} catch (SelectorSecondaryException e) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		if (!player.getSelectorPrimary().isPresent()) {
			EWMessages.SELECT_POS2_ONE.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else {
			EWMessages.SELECT_POS2_TWO.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.replace("<area>", String.valueOf(player.getSelectorVolume()))
				.sendTo(player);
		}
		return true;
	}
	
	private boolean commandSelectPos2Poly(final EPlayer player, final Vector3i position) {
		List<Vector3i> points = player.getSelectorPositions();
		
		if (!points.isEmpty() && points.get(points.size() - 1).equals(position)) {
			EWMessages.SELECT_POS2_EQUALS.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		try {
			if (!player.setSelectorSecondary(position)) {
				EWMessages.SELECT_POS2_CANCEL.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
				return false;
			}
		} catch (SelectorSecondaryException e) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		if (points.isEmpty()) {
			EWMessages.SELECT_POS2_POLY_ONE.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.replace("<num>", String.valueOf(points.size()))
				.sendTo(player);
		} else {
			EWMessages.SELECT_POS2_POLY_ALL.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.replace("<num>", String.valueOf(points.size()))
				.replace("<area>", String.valueOf(player.getSelectorVolume()))
				.sendTo(player);
		}
		return true;
	}
	
	private boolean commandSelectPos2Cylinder(final EPlayer player, final Vector3i position) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Vector3i> pos2 = player.getSelectorSecondary();

		if (pos2.isPresent() && pos2.get().equals(position)) {
			EWMessages.SELECT_POS2_EQUALS.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		try {
			if (!player.setSelectorSecondary(position)) {
				EWMessages.SELECT_POS2_CANCEL.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
				return false;
			}
		} catch (SelectorSecondaryException e) {
			EWMessages.SELECT_POS2_NO_CENTER.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		EWMessages.SELECT_POS2_RADIUS.sender()
			.replace("<pos>", EWSelect.getPositionHover(position))
			.replace("<radius>", String.valueOf(position.distance(pos1.get())))
			.replace("<area>", String.valueOf(player.getSelectorVolume()))
			.sendTo(player);
		return true;
	}
}
