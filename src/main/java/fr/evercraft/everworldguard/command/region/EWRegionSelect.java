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
import java.util.Iterator;
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
import fr.evercraft.everapi.services.selection.SelectionType;
import fr.evercraft.everapi.services.selection.exception.SelectorSecondaryException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Type;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Types;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionSelect extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	
	private final Args.Builder pattern;
	
	public EWRegionSelect(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "select");
        
        this.pattern = Args.builder()
        	.value(MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				Optional<World> world = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getProtectionService().getOrCreateWorld(world.get()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_SELECT.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_SELECT_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
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
		
		World world = null;
		Optional<String> world_arg = args.getValue(MARKER_WORLD);
		if (world_arg.isPresent()) {
			Optional<World> optWorld = this.plugin.getEServer().getWorld(world_arg.get());
			if (optWorld.isPresent()) {
				world = optWorld.get();
			} else {
				EAMessages.WORLD_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<world>", world_arg.get())
					.sendTo(source);
				return false;
			}
		} else if (source instanceof EPlayer) {
			world = ((EPlayer) source).getWorld();
		} else {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		Optional<ProtectedRegion> region = this.plugin.getProtectionService().getOrCreateWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		if (!this.hasPermission(source, region.get(), world)) {
			EWMessages.REGION_NO_PERMISSION.sender()
				.replace("<region>", region.get().getName())
				.sendTo(source);
			return false;
		}
		
		return this.commandRegionSelect(player, region.get(), world);
	}
	
	private boolean commandRegionSelect(final EPlayer player, final ProtectedRegion region, final World world) {
		Type type = region.getType();
		if (type.equals(Types.CUBOID)) {
			return this.commandRegionSelectCuboid(player, region, world);
		} else if (type.equals(Types.POLYGONAL)) {
			return this.commandRegionSelectPolygonal(player, region, world);
		} else if (type.equals(Types.GLOBAL)) {
			EWMessages.REGION_SELECT_GLOBAL.sender()
				.replace("<region>", region.getName())
				.replace("<world>", world.getName())
				.replace("<type>", type.getNameFormat())
				.sendTo(player);
		} else if (type.equals(Types.TEMPLATE)) {
			EWMessages.REGION_SELECT_TEMPLATE.sender()
				.replace("<region>", region.getName())
				.replace("<world>", world.getName())
				.replace("<type>", type.getNameFormat())
				.sendTo(player);
		}
		return false;
	}
	
	private boolean commandRegionSelectCuboid(final EPlayer player, final ProtectedRegion region, final World world) {
		Vector3i min = region.getMinimumPoint();
		Vector3i max = region.getMaximumPoint();
		
		player.setSelectorType(SelectionType.CUBOID);
		player.setSelectorPrimary(min);
		try {
			player.setSelectorSecondary(max);
		} catch (SelectorSecondaryException e) {}
		
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<min_x>", EReplace.of(String.valueOf(min.getX())));
		replaces.put("<min_y>", EReplace.of(String.valueOf(min.getY())));
		replaces.put("<min_z>", EReplace.of(String.valueOf(min.getZ())));
		replaces.put("<max_x>", EReplace.of(String.valueOf(max.getX())));
		replaces.put("<max_y>", EReplace.of(String.valueOf(max.getY())));
		replaces.put("<max_z>", EReplace.of(String.valueOf(max.getZ())));
		replaces.put("<region>", EReplace.of(region.getName()));
		replaces.put("<type>", EReplace.of(region.getType().getNameFormat()));
		
		EWMessages.REGION_SELECT_CUBOID.sender()
			.replaceString(replaces)
			.replace("<positions>", EWMessages.REGION_SELECT_CUBOID_POINTS.getFormat()
					.toText2(replaces).toBuilder()
					.onHover(TextActions.showText(EWMessages.REGION_SELECT_CUBOID_POINTS_HOVER.getFormat()
							.toText2(replaces)))
					.build())
			.sendTo(player);
		return false;
	}
	
	private boolean commandRegionSelectPolygonal(final EPlayer player, final ProtectedRegion region, final World world) {
		player.setSelectorType(SelectionType.POLYGONAL);
		Iterator<Vector3i> iterator = region.getPoints().iterator();
		if (iterator.hasNext()) {
			player.setSelectorPrimary(iterator.next());
			while (iterator.hasNext()) {
				try {
					player.setSelectorSecondary(iterator.next());
				} catch (SelectorSecondaryException e) {}
			}
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
		replaces.put("<region>", EReplace.of(region.getName()));
		replaces.put("<type>", EReplace.of(region.getType().getNameFormat()));
		
		for(Vector3i pos : region.getPoints()) {
			positions_text.add(EWMessages.REGION_SELECT_POLYGONAL_POINTS_HOVER_LINE.getFormat()
					.toText("<x>", String.valueOf(pos.getX()),
							"<y>", String.valueOf(pos.getY()),
							"<z>", String.valueOf(pos.getZ())));
		}				
		replaces.put("<positions>", EReplace.of(Text.joinWith(EWMessages.REGION_SELECT_POLYGONAL_POINTS_HOVER_JOIN.getText(), positions_text)));
		
		EWMessages.REGION_SELECT_POLYGONAL.sender()
			.replaceString(replaces)
			.replace("<positions>", EWMessages.REGION_SELECT_POLYGONAL_POINTS.getFormat()
					.toText2(replaces).toBuilder()
					.onHover(TextActions.showText(EWMessages.REGION_SELECT_POLYGONAL_POINTS_HOVER.getFormat()
							.toText2(replaces)))
					.build())
			.sendTo(player);
		return false;
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_SELECT_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_SELECT_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_SELECT_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
