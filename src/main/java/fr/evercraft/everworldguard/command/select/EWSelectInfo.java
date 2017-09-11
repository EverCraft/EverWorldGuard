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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.SelectionRegion.Cylinder;
import fr.evercraft.everapi.services.selection.SelectionRegion.Ellipsoid;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectInfo extends ESubCommand<EverWorldGuard> {
	
	public EWSelectInfo(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "info");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_INFO_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName())
				.onClick(TextActions.suggestCommand("/" + this.getName()))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return Arrays.asList();
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 0) {
			if (source instanceof EPlayer) {
				return this.commandSelect((EPlayer) source);
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

	private CompletableFuture<Boolean> commandSelect(final EPlayer player) {
		if (player.getSelectorType().equals(SelectionRegion.Types.CUBOID)) {
			return this.commandSelectCuboid(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.EXTEND)) {
			return this.commandSelectExtend(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.POLYGONAL)) {
			return this.commandSelectPoly(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.CYLINDER)) {
			return this.commandSelectCylinder(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.ELLIPSOID)) {
			return this.commandSelectEllipsoid(player);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.SPHERE)) {
			return this.commandSelectSphere(player);
		} else {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
	}
	
	private CompletableFuture<Boolean> commandSelectCuboid(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Vector3i> pos2 = player.getSelectorSecondary();
		
		Text text = null;
		if (pos1.isPresent() && pos2.isPresent()) {
			text = EWMessages.SELECT_INFO_CUBOID_POS1_AND_POS2.sender()
				.replace("{pos1}", EWSelect.getPositionHover(pos1.get()))
				.replace("{pos2}", EWSelect.getPositionHover(pos2.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else if (pos1.isPresent()) {
			text = EWMessages.SELECT_INFO_CUBOID_POS1.sender()
				.replace("{pos}", EWSelect.getPositionHover(pos1.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else if (pos2.isPresent()) {
			text = EWMessages.SELECT_INFO_CUBOID_POS2.sender()
				.replace("{pos}", EWSelect.getPositionHover(pos2.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else {
			EWMessages.SELECT_INFO_CUBOID_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.SELECT_INFO_CUBOID_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				Arrays.asList(text), player);
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectExtend(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Vector3i> pos2 = player.getSelectorSecondary();
		
		Text text = null;
		if (pos1.isPresent() && pos2.isPresent()) {
			text = EWMessages.SELECT_INFO_EXTEND_POS1_AND_POS2.sender()
				.replace("{pos1}", EWSelect.getPositionHover(pos1.get()))
				.replace("{pos2}", EWSelect.getPositionHover(pos2.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else if (pos1.isPresent()) {
			text = EWMessages.SELECT_INFO_EXTEND_POS1.sender()
				.replace("{pos}", EWSelect.getPositionHover(pos1.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else if (pos2.isPresent()) {
			text = EWMessages.SELECT_INFO_EXTEND_POS2.sender()
				.replace("{pos}", EWSelect.getPositionHover(pos2.get()))
				.replace("{area}", String.valueOf(player.getSelectorVolume()))
				.toText(false);
		} else {
			EWMessages.SELECT_INFO_EXTEND_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.SELECT_INFO_EXTEND_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				Arrays.asList(text), player);
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectPoly(final EPlayer player) {
		List<Vector3i> points = player.getSelectorPositions();
		
		if (!points.isEmpty()) {
			List<Text> lists = new ArrayList<Text>();
			int num = 1;
			for (Vector3i pos : points) {
				lists.add(EWMessages.SELECT_INFO_POLY_LINE.getFormat()
					.toText("{pos}", EWSelect.getPositionHover(pos),
							"{num}", String.valueOf(num)));
				num++;
			}
			
			this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(EWMessages.SELECT_INFO_POLY_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
					.onClick(TextActions.runCommand("/s"))
					.build(), 
					lists, player);
		} else {
			EWMessages.SELECT_INFO_POLY_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectCylinder(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Cylinder> optRegion = player.getSelectorRegion(SelectionRegion.Cylinder.class);
		
		Text text = null;
		if (optRegion.isPresent()) { 
			Cylinder region = optRegion.get();
			text = EWMessages.SELECT_INFO_CYLINDER_CENTER_AND_RADIUS.sender()
				.replace("{center}", EWSelect.getPositionHover(region.getCenter()))
				.replace("{width}", region.getWidth())
				.replace("{height}", region.getHeight())
				.replace("{length}", region.getLength())
				.replace("{area}", player.getSelectorVolume())
				.toText(false);
		} else if (pos1.isPresent()) {
			text = EWMessages.SELECT_INFO_CYLINDER_CENTER.sender()
				.replace("{center}", EWSelect.getPositionHover(pos1.get()))
				.toText(false);
		} else {
			EWMessages.SELECT_INFO_CYLINDER_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.SELECT_INFO_CYLINDER_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				Arrays.asList(text), player);
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectEllipsoid(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Ellipsoid> optRegion = player.getSelectorRegion(SelectionRegion.Ellipsoid.class);
		
		Text text = null;
		if (optRegion.isPresent()) {
			Ellipsoid region = optRegion.get();
			text = EWMessages.SELECT_INFO_ELLIPSOID_CENTER_AND_RADIUS.sender()
				.replace("{center}", EWSelect.getPositionHover(region.getCenter()))
				.replace("{width}", region.getWidth())
				.replace("{height}", region.getHeight())
				.replace("{length}", region.getLength())
				.replace("{area}", player.getSelectorVolume())
				.toText(false);
		} else if (pos1.isPresent()) {
			text = EWMessages.SELECT_INFO_ELLIPSOID_CENTER.sender()
				.replace("{center}", EWSelect.getPositionHover(pos1.get()))
				.toText(false);
		} else {
			EWMessages.SELECT_INFO_ELLIPSOID_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.SELECT_INFO_ELLIPSOID_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				Arrays.asList(text), player);
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectSphere(final EPlayer player) {
		Optional<Vector3i> pos1 = player.getSelectorPrimary();
		Optional<Ellipsoid> optRegion = player.getSelectorRegion(SelectionRegion.Ellipsoid.class);
		
		Text text = null;
		if (optRegion.isPresent()) {
			Ellipsoid region = optRegion.get();
			text = EWMessages.SELECT_INFO_SPHERE_CENTER_AND_RADIUS.sender()
				.replace("{center}", EWSelect.getPositionHover(region.getCenter()))
				.replace("{radius}", Math.round(region.getWidth()/2))
				.replace("{width}", region.getWidth())
				.replace("{height}", region.getHeight())
				.replace("{length}", region.getLength())
				.replace("{area}", player.getSelectorVolume())
				.toText(false);
		} else if (pos1.isPresent()) {
			text = EWMessages.SELECT_INFO_SPHERE_CENTER.sender()
				.replace("{center}", EWSelect.getPositionHover(pos1.get()))
				.toText(false);
		} else {
			EWMessages.SELECT_INFO_SPHERE_EMPTY.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.SELECT_INFO_SPHERE_TITLE.getFormat()
					.toText("{area}", String.valueOf(player.getSelectorVolume())).toBuilder()
				.onClick(TextActions.runCommand("/" + this.getName()))
				.build(), 
				Arrays.asList(text), player);
		return CompletableFuture.completedFuture(true);
	}
}
