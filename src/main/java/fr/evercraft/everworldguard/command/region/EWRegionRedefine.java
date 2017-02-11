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
package fr.evercraft.everworldguard.command.region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.SelectType;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionRedefine extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_TEMPLATE = "-t";
	
	private final Args.Builder pattern;
	
	public EWRegionRedefine(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "define");
        
        this.pattern = Args.builder()
			.empty(MARKER_TEMPLATE)
			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
			.arg((source, args) -> {
				Optional<World> world = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getService().getOrCreateWorld(world.get()).getAll().stream()
							.map(region -> region.getIdentifier())
							.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_REDEFINE.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_INFO_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]" 
												 + " [" + MARKER_TEMPLATE + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(source, args);
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args_list) throws CommandException {
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		EPlayer player = (EPlayer) source;
		
		Args args = this.pattern.build(args_list);
		if (args.getArgs().size() != 1) {
			source.sendMessage(this.help(source));
			return false;
		}
		List<String> args_string = args.getArgs();
				
		Optional<ProtectedRegion> region = this.plugin.getService().getOrCreateWorld(player.getWorld()).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		if (region.get().getType().equals(ProtectedRegion.Type.GLOBAL)) {
			EWMessages.REGION_REDEFINE_ERROR_GLOBAL.sender()
				.replace("<region>", region.get().getIdentifier())
				.replace("<type>", region.get().getType().getNameFormat())
				.replace("<world>", player.getWorld().getName())
				.sendTo(source);
			return false;
		}
		
		if (args.isOption(MARKER_TEMPLATE)) {
			return this.commandRegionRedefineTemplate(player, region.get());
		} else {
			return this.commandRegionRedefine(player, region.get());
		}
	}
	
	private boolean commandRegionRedefine(final EPlayer player, final ProtectedRegion region) {
		if (player.getSelectType().equals(SelectType.CUBOID)) {
			return this.commandRegionRedefineCuboid(player, region);
		} else if (player.getSelectType().equals(SelectType.POLYGONAL)) {
			return this.commandRegionRedefinePolygonal(player, region);
		} else {
			EWMessages.REGION_REDEFINE_ERROR_SELECT_TYPE.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<type>", player.getSelectType().getName())
				.sendTo(player);
			return false;
		}
	}
	
	private boolean commandRegionRedefineCuboid(final EPlayer player, final ProtectedRegion region) {
		Optional<Vector3i> pos1 = player.getSelectPos1();
		Optional<Vector3i> pos2 = player.getSelectPos2();
		if (!pos1.isPresent() || !pos2.isPresent()) {
			EWMessages.REGION_REDEFINE_CUBOID_ERROR_POSITION.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<type>", ProtectedRegion.Type.CUBOID.getNameFormat())
				.sendTo(player);
			return false;
		}
		
		Optional<ProtectedRegion.Cuboid> region_new = region.redefineCuboid(pos1.get(), pos2.get());
		if (!region_new.isPresent()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		Vector3i min = region.getMinimumPoint();
		Vector3i max = region.getMaximumPoint();
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<min_x>", EReplace.of(String.valueOf(min.getX())));
		replaces.put("<min_y>", EReplace.of(String.valueOf(min.getY())));
		replaces.put("<min_z>", EReplace.of(String.valueOf(min.getZ())));
		replaces.put("<max_x>", EReplace.of(String.valueOf(max.getX())));
		replaces.put("<max_y>", EReplace.of(String.valueOf(max.getY())));
		replaces.put("<max_z>", EReplace.of(String.valueOf(max.getZ())));		
		
		EWMessages.REGION_REDEFINE_CUBOID_CREATE.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<type>", player.getSelectType().getName())
			.replace("<positions>", EWMessages.REGION_REDEFINE_CUBOID_POINTS.getFormat()
					.toText(replaces).toBuilder()
					.onHover(TextActions.showText(EWMessages.REGION_REDEFINE_CUBOID_POINTS_HOVER.getFormat()
							.toText(replaces)))
					.build())
			.sendTo(player);
		return true;
	}
	
	private boolean commandRegionRedefinePolygonal(final EPlayer player, final ProtectedRegion region) {
		List<Vector3i> positions = player.getSelectPoints();
		if (positions.size() < 3) {
			EWMessages.REGION_REDEFINE_POLYGONAL_ERROR_POSITION.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<type>", ProtectedRegion.Type.POLYGONAL.getNameFormat())
				.sendTo(player);
			return false;
		}
		
		Optional<ProtectedRegion.Polygonal> region_new = region.redefinePolygonal(positions);
		if (!region_new.isPresent()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		Vector3i min = region.getMinimumPoint();
		Vector3i max = region.getMaximumPoint();
		List<Text> positions_text = new ArrayList<Text>();
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<min_x>", EReplace.of(String.valueOf(min.getX())));
		replaces.put("<min_y>", EReplace.of(String.valueOf(min.getY())));
		replaces.put("<min_z>", EReplace.of(String.valueOf(min.getZ())));
		replaces.put("<max_x>", EReplace.of(String.valueOf(max.getX())));
		replaces.put("<max_y>", EReplace.of(String.valueOf(max.getY())));
		replaces.put("<max_z>", EReplace.of(String.valueOf(max.getZ())));
		
		for(Vector3i pos : region.getPoints()) {
			positions_text.add(EWMessages.REGION_REDEFINE_POLYGONAL_POINTS_HOVER_LINE.getFormat()
					.toText("<x>", String.valueOf(pos.getX()),
							"<y>", String.valueOf(pos.getY()),
							"<z>", String.valueOf(pos.getZ())));
		}				
		replaces.put("<positions>", EReplace.of(Text.joinWith(EWMessages.REGION_REDEFINE_POLYGONAL_POINTS_HOVER_JOIN.getText(), positions_text)));
		
		EWMessages.REGION_REDEFINE_POLYGONAL_CREATE.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<type>", region.getType().getNameFormat())
			.replace("<positions>", EWMessages.REGION_REDEFINE_POLYGONAL_POINTS.getFormat()
					.toText(replaces).toBuilder()
					.onHover(TextActions.showText(EWMessages.REGION_REDEFINE_POLYGONAL_POINTS_HOVER.getFormat()
							.toText(replaces)))
					.build())
			.sendTo(player);
		return true;
	}
	
	private boolean commandRegionRedefineTemplate(final EPlayer player, final ProtectedRegion region) {
		Optional<ProtectedRegion.Template> region_new = region.redefineTemplate();
		if (!region_new.isPresent()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		EWMessages.REGION_REDEFINE_TEMPLATE_CREATE.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<type>", region.getType().getNameFormat())
			.sendTo(player);
		return true;
	}
}