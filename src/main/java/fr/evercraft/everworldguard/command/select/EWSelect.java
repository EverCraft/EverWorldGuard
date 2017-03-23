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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.SelectionType;
import fr.evercraft.everapi.services.selection.exception.SelectorSecondaryException;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelect extends EParentCommand<EverWorldGuard> {
	
	public EWSelect(final EverWorldGuard plugin) {
        super(plugin, "select", "sel", "s");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.SELECT.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return true;
	}
	
	public static Text getPositionHover(final Vector3i position) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<x>", EReplace.of(String.valueOf(position.getX())));
		replaces.put("<y>", EReplace.of(String.valueOf(position.getY())));
		replaces.put("<z>", EReplace.of(String.valueOf(position.getZ())));
		return EWMessages.SELECT_INFO_POS.getFormat().toText2(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.SELECT_INFO_POS_HOVER.getFormat().toText2(replaces)))
				.build();
	}
	
	public static boolean eventPos1(final EPlayer player, final Vector3i position) {
		if (!player.hasPermission(EWPermissions.SELECT_WAND.get())) {
			return false;
		}
		
		if (!player.setSelectorPrimary(position)) {
			return true;
		}
		
		if (player.getSelectorType().equals(SelectionType.CUBOID) || 
				player.getSelectorType().equals(SelectionType.EXTEND)) {
			Optional<Vector3i> pos2 = player.getSelectorSecondary();
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
		} else if (player.getSelectorType().equals(SelectionType.POLYGONAL)) {
			EWMessages.SELECT_POS1_POLY.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else if (player.getSelectorType().equals(SelectionType.CYLINDER) || 
					player.getSelectorType().equals(SelectionType.ELLIPSOID) || 
					player.getSelectorType().equals(SelectionType.SPHERE)) {
			EWMessages.SELECT_POS1_CENTER.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
		}
		return true;
	}
	
	public static boolean eventPos2(final EPlayer player, final Vector3i position) {
		if (!player.hasPermission(EWPermissions.SELECT_WAND.get())) {
			return false;
		}
		
		try {
			if (!player.setSelectorSecondary(position)) {
				return true;
			}
		} catch (SelectorSecondaryException e) {
			EWMessages.SELECT_POS2_NO_CENTER.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.sendTo(player);
			return false;
		}
		
		if (player.getSelectorType().equals(SelectionType.CUBOID) || 
				player.getSelectorType().equals(SelectionType.EXTEND)) {
			Optional<Vector3i> pos1 = player.getSelectorPrimary();
			if (!pos1.isPresent()) {
				EWMessages.SELECT_POS2_ONE.sender()
					.replace("<position>", EWSelect.getPositionHover(position))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS2_TWO.sender()
					.replace("<position>", EWSelect.getPositionHover(position))
					.replace("<area>", String.valueOf(player.getSelectorVolume()))
					.sendTo(player);
			}
		} else if (player.getSelectorType().equals(SelectionType.POLYGONAL)) {
			if (player.getSelectorPositions().size() == 1) {
				EWMessages.SELECT_POS2_POLY_ONE.sender()
					.replace("<position>", EWSelect.getPositionHover(position))
					.replace("<num>", String.valueOf(player.getSelectorPositions().size()))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS2_POLY_ALL.sender()
					.replace("<position>", EWSelect.getPositionHover(position))
					.replace("<num>", String.valueOf(player.getSelectorPositions().size()))
					.replace("<area>", String.valueOf(player.getSelectorVolume()))
					.sendTo(player);
			}
		} else if (player.getSelectorType().equals(SelectionType.CYLINDER) || 
					player.getSelectorType().equals(SelectionType.ELLIPSOID) || 
					player.getSelectorType().equals(SelectionType.SPHERE)) {
			Vector3i pos1 = player.getSelectorPrimary().orElse(Vector3i.ZERO);
			
			EWMessages.SELECT_POS2_RADIUS.sender()
				.replace("<position>", EWSelect.getPositionHover(position))
				.replace("<radius>", Math.round(position.distance(pos1)))
				.replace("<area>", player.getSelectorVolume())
				.sendTo(player);
		}
		return true;
	}
}