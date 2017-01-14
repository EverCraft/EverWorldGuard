/*
 * This file is part of EverEssentials.
 *
 * EverEssentials is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverEssentials is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverEssentials.  If not, see <http://www.gnu.org/licenses/>.
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
import fr.evercraft.everapi.services.worldguard.SelectType;
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
		if (player.getSelectType().equals(SelectType.CUBOID)) {
			return this.commandSelectCuboid(player);
		} else if (player.getSelectType().equals(SelectType.POLY)) {
			return this.commandSelectPoly(player);
		} else if (player.getSelectType().equals(SelectType.CYLINDER)) {
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
	
	public static final Text getPositionHover(final Vector3i position) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<x>", EReplace.of(String.valueOf(position.getX())));
		replaces.put("<y>", EReplace.of(String.valueOf(position.getY())));
		replaces.put("<z>", EReplace.of(String.valueOf(position.getZ())));
		return EWMessages.SELECT_INFO_POS.getFormat().toText(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.SELECT_INFO_POS_HOVER.getFormat().toText(replaces)))
				.build();
	}
}