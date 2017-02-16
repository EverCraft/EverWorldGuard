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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.SelectionType;
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
	
	@Override
	protected boolean commandDefault(final CommandSource source, final List<String> args) {
		// Résultat de la commande :
		boolean resultat = false;
				
		// Si la source est un joueur
		if (source instanceof EPlayer) {
			resultat = this.commandSelect((EPlayer) source);
		// La source n'est pas un joueur
		} else {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
		}
		
		return resultat;
	}
	
	private boolean commandSelect(final EPlayer player) {
		if (player.getSelectorType().equals(SelectionType.CUBOID)) {
			return this.commandSelectCuboid(player);
		} else if (player.getSelectorType().equals(SelectionType.POLYGONAL)) {
			return this.commandSelectPoly(player);
		} else if (player.getSelectorType().equals(SelectionType.CYLINDER)) {
			return this.commandSelectCylinder(player);
		} else {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
	}
	
	private boolean commandSelectCuboid(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectPos1();
		Optional<Vector3i> pos2 = player.getSelectPos2();
		
		if (pos1.isPresent() && pos2.isPresent()) {
			EWMessages.SELECT_INFO_CUBOID_POS1_AND_POS2.sender()
				.replace("<pos1>", EWSelect.getPositionHover(pos1.get()))
				.replace("<pos2>", EWSelect.getPositionHover(pos2.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else if (pos1.isPresent()) {
			EWMessages.SELECT_INFO_CUBOID_POS1.sender()
				.replace("<pos>", EWSelect.getPositionHover(pos1.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else if (pos2.isPresent()) {
			EWMessages.SELECT_INFO_CUBOID_POS2.sender()
				.replace("<pos>", EWSelect.getPositionHover(pos2.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else {
			EWMessages.SELECT_INFO_CUBOID_EMPTY.sendTo(player);
			return false;
		}
		return true;
	}
	
	private boolean commandSelectPoly(final EPlayer player) {
		List<Vector3i> points = player.getSelectPoints();
		
		if (!points.isEmpty()) {
			List<Text> lists = new ArrayList<Text>();
			
			for (Vector3i pos : points) {
				lists.add(EWMessages.SELECT_INFO_POLY_LINE.getFormat()
					.toText("<pos>", EWSelect.getPositionHover(pos)));
			}
			
			this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(EWMessages.SELECT_INFO_POLY_TITLE.getFormat()
					.toText("<area>", player.getSelectArea().orElse(0).toString()).toBuilder()
					.onClick(TextActions.runCommand("/s"))
					.build(), 
					lists, player);
		} else {
			EWMessages.SELECT_INFO_POLY_EMPTY.sendTo(player);
			return false;
		}
		return true;
	}
	
	private boolean commandSelectCylinder(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectPos1();
		Optional<Vector3i> pos2 = player.getSelectPos2();
		
		if (pos1.isPresent() && pos2.isPresent()) {
			EWMessages.SELECT_INFO_CYLINDER_CENTER_AND_RADIUS.sender()
				.replace("<pos1>", EWSelect.getPositionHover(pos1.get()))
				.replace("<pos2>", EWSelect.getPositionHover(pos2.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else if (pos1.isPresent()) {
			EWMessages.SELECT_INFO_CYLINDER_CENTER.sender()
				.replace("<pos>", EWSelect.getPositionHover(pos1.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else if (pos2.isPresent()) {
			EWMessages.SELECT_INFO_CYLINDER_RADIUS.sender()
				.replace("<pos>", EWSelect.getPositionHover(pos2.get()))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		} else {
			EWMessages.SELECT_INFO_CYLINDER_EMPTY.sendTo(player);
			return false;
		}
		return true;
	}
	
	public static Text getPositionHover(final Vector3i position) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<x>", EReplace.of(String.valueOf(position.getX())));
		replaces.put("<y>", EReplace.of(String.valueOf(position.getY())));
		replaces.put("<z>", EReplace.of(String.valueOf(position.getZ())));
		return EWMessages.SELECT_INFO_POS.getFormat().toText(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.SELECT_INFO_POS_HOVER.getFormat().toText(replaces)))
				.build();
	}
	
	public static boolean eventPos1(final EPlayer player, final Vector3i position) {
		if (!player.hasPermission(EWPermissions.SELECT_WAND.get())) {
			return false;
		}
		
		if (!player.setSelectPos1(position)) {
			return true;
		}
		
		if (player.getSelectType().equals(SelectionType.CUBOID)) {
			if (!player.getSelectPos2().isPresent()) {
				EWMessages.SELECT_POS1_CUBOID_ONE.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS1_CUBOID_TWO.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.replace("<area>", player.getSelectArea().orElse(0).toString())
					.sendTo(player);
			}
		} else if (player.getSelectType().equals(SelectionType.POLYGONAL)) {
			player.setSelectPos2(null);
			player.clearSelectPoints();
			EWMessages.SELECT_POS1_POLY.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
		} else if (player.getSelectType().equals(SelectionType.CYLINDER)) {
			player.setSelectPos2(null);
			EWMessages.SELECT_POS1_CYLINDER_CENTER.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.sendTo(player);
		}
		return true;
	}
	
	public static boolean eventPos2(final EPlayer player, final Vector3i position) {
		if (!player.hasPermission(EWPermissions.SELECT_WAND.get())) {
			return false;
		}
		
		if (player.getSelectType().equals(SelectionType.CUBOID)) {
			if (!player.setSelectPos2(position)) {
				return true;
			}
			
			if (!player.getSelectPos2().isPresent()) {
				EWMessages.SELECT_POS2_CUBOID_ONE.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS2_CUBOID_TWO.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.replace("<area>", player.getSelectArea().orElse(0).toString())
					.sendTo(player);
			}
		} else if (player.getSelectType().equals(SelectionType.POLYGONAL)) {
			if (!player.addSelectPoint(position)) {
				return true;
			}
			
			if (player.getSelectPoints().size() == 1) {
				EWMessages.SELECT_POS2_POLY_ONE.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.replace("<num>", String.valueOf(player.getSelectPoints().size()))
					.sendTo(player);
			} else {
				EWMessages.SELECT_POS2_POLY_ALL.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.replace("<num>", String.valueOf(player.getSelectPoints().size()))
					.replace("<area>", player.getSelectArea().orElse(0).toString())
					.sendTo(player);
			}
		} else if (player.getSelectType().equals(SelectionType.CYLINDER)) {
			Optional<Vector3i> pos1 = player.getSelectPos1();
			
			if (!pos1.isPresent()) {
				EWMessages.SELECT_POS2_NO_CENTER.sender()
					.replace("<pos>", EWSelect.getPositionHover(position))
					.sendTo(player);
				return false;
			}
			
			if (!player.setSelectPos2(position)) {
				return true;
			}
			
			EWMessages.SELECT_POS2_RADIUS.sender()
				.replace("<pos>", EWSelect.getPositionHover(position))
				.replace("<radius>", String.valueOf(position.distance(pos1.get())))
				.replace("<area>", player.getSelectArea().orElse(0).toString())
				.sendTo(player);
		}
		return true;
	}
}