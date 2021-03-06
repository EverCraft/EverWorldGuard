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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionDefine extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_TEMPLATE = "-t";
	public static final String MARKER_OWNER_PLAYER = "-p";
	public static final String MARKER_OWNER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionDefine(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "define");
        
        this.pattern = Args.builder()
			.empty(MARKER_TEMPLATE)
			.arg((source, args) -> Arrays.asList("region..."))
			.list(MARKER_OWNER_PLAYER, (source, args) -> this.getAllUsers(args.getList(MARKER_OWNER_PLAYER).orElse(Arrays.asList()), source))
			.list(MARKER_OWNER_GROUP, (source, args) -> this.getAllGroups(args.getList(MARKER_OWNER_GROUP).orElse(Arrays.asList())));
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_INFO.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_DEFINE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_TEMPLATE + "] <" + EAMessages.ARGS_REGION.getString() + "> "
												  + "[" + MARKER_OWNER_PLAYER + " " + EAMessages.ARGS_OWNER_PLAYER.getString() + "...] "
												  + "[" + MARKER_OWNER_GROUP + " " + EAMessages.ARGS_OWNER_GROUP.getString() + "...]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(this.plugin, source, args);
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args_list) throws CommandException {
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		EPlayer player = (EPlayer) source;
		
		Args args = this.pattern.build(this.plugin, source, args_list);
		Optional<String> region_id = args.getArg(0);
		if (args.getArgs().size() != 1 || !region_id.isPresent()) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
		}
		
		if (this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld()).getRegion(region_id.get()).isPresent()) {
			EWMessages.REGION_DEFINE_ERROR_IDENTIFIER_EQUALS.sender()
				.replace("{region}", region_id.get())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		Set<UUID> players = new HashSet<UUID>();
		Optional<List<String>> players_string = args.getList(MARKER_OWNER_PLAYER);
		if (players_string.isPresent()) {
			for (String player_string : players_string.get()) {
				Optional<EUser> user = this.plugin.getEServer().getEUser(player_string);
				if (user.isPresent()) {
					players.add(user.get().getUniqueId());
				} else {
					EAMessages.PLAYER_NOT_FOUND.sender()
						.prefix(EWMessages.PREFIX)
						.replace("{player}", player_string)
						.sendTo(source);
					return CompletableFuture.completedFuture(false);
				}
			}
		}
		
		Set<String> groups = new HashSet<String>();
		Optional<List<String>> groups_string = args.getList(MARKER_OWNER_GROUP);
		if (groups_string.isPresent()) {
			for (String group_string : groups_string.get()) {
				Subject group = this.plugin.getEverAPI().getManagerService().getPermission().getGroupSubjects().loadSubject(group_string).join();
				if (group != null) {
					groups.add(group.getIdentifier());
				} else {
					EAMessages.GROUP_NOT_FOUND.sender()
						.prefix(EWMessages.PREFIX)
						.replace("{player}", group_string)
						.sendTo(source);
					return CompletableFuture.completedFuture(false);
				}
			}
		}
				
		if (args.isOption(MARKER_TEMPLATE)) {
			return this.commandRegionDefineTemplate(player, region_id.get(), players, groups);
		} else {
			return this.commandRegionDefine(player, region_id.get(), players, groups);
		}
	}
	
	private CompletableFuture<Boolean> commandRegionDefine(final EPlayer player, final String region_id, final Set<UUID> players, final Set<String> groups) {
		if (player.getSelectorType().equals(SelectionRegion.Types.CUBOID)) {
			return this.commandRegionDefineCuboid(player, region_id, players, groups);
		} else if (player.getSelectorType().equals(SelectionRegion.Types.POLYGONAL)) {
			return this.commandRegionDefinePolygonal(player, region_id, players, groups);
		} else {
			EWMessages.REGION_DEFINE_ERROR_SELECT_TYPE.sender()
				.replace("{region}", region_id)
				.replace("{type}", player.getSelectorType().getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
	}
	
	private CompletableFuture<Boolean> commandRegionDefineCuboid(final EPlayer player, final String region_id, final Set<UUID> players, final Set<String> groups) {
		Optional<SelectionRegion.Cuboid> selection = player.getSelectorRegion(SelectionRegion.Cuboid.class);
		if (!selection.isPresent()) {
			EWMessages.REGION_DEFINE_CUBOID_ERROR_POSITION.sender()
				.replace("{region}", region_id)
				.replace("{type}", ProtectedRegion.Types.CUBOID.getNameFormat())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		try {
			return this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld()).createRegionCuboid(region_id, 
					selection.get().getPrimaryPosition(), selection.get().getSecondaryPosition(), players, groups, false)
				.exceptionally(e -> null)
				.thenApply(region -> {
					if (region == null) {
						EAMessages.COMMAND_ERROR.sendTo(player);
						return false;
					}
					
					Vector3i min = region.getMinimumPoint();
					Vector3i max = region.getMaximumPoint();
					Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
					replaces.put("{min_x}", EReplace.of(String.valueOf(min.getX())));
					replaces.put("{min_y}", EReplace.of(String.valueOf(min.getY())));
					replaces.put("{min_z}", EReplace.of(String.valueOf(min.getZ())));
					replaces.put("{max_x}", EReplace.of(String.valueOf(max.getX())));
					replaces.put("{max_y}", EReplace.of(String.valueOf(max.getY())));
					replaces.put("{max_z}", EReplace.of(String.valueOf(max.getZ())));
					replaces.put("{region}", EReplace.of(region.getName()));
					replaces.put("{type}", EReplace.of(region.getType().getNameFormat()));
					
					EWMessages.REGION_DEFINE_CUBOID_CREATE.sender()
						.replaceString(replaces)
						.replace("{points}", EWMessages.REGION_DEFINE_CUBOID_POINTS.getFormat()
								.toText2(replaces).toBuilder()
								.onHover(TextActions.showText(EWMessages.REGION_DEFINE_CUBOID_POINTS_HOVER.getFormat()
										.toText2(replaces)))
								.build())
						.sendTo(player);
					return true;
				});
		} catch (RegionIdentifierException e) {
			EWMessages.REGION_DEFINE_ERROR_IDENTIFIER_INVALID.sender()
				.replace("{region}", region_id)
				.replace("{type}", ProtectedRegion.Types.CUBOID.getNameFormat())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
	}
	
	private CompletableFuture<Boolean> commandRegionDefinePolygonal(final EPlayer player, final String region_id, final Set<UUID> players, final Set<String> groups) {
		Optional<SelectionRegion.Polygonal> selector = player.getSelectorRegion(SelectionRegion.Polygonal.class);
		if (!selector.isPresent()) {
			EWMessages.REGION_DEFINE_POLYGONAL_ERROR_POSITION.sender()
				.replace("{region}", region_id)
				.replace("{type}", ProtectedRegion.Types.POLYGONAL.getNameFormat())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		try {
			return this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld()).createRegionPolygonal(region_id, selector.get().getPositions(), players, groups, false)
				.exceptionally(e -> null)
				.thenApply(region -> {
					if (region == null) {
						EAMessages.COMMAND_ERROR.sendTo(player);
						return false;
					}
					
					Vector3i min = region.getMinimumPoint();
					Vector3i max = region.getMaximumPoint();
					List<Text> positions_text = new ArrayList<Text>();
					Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
					replaces.put("{min_x}", EReplace.of(String.valueOf(min.getX())));
					replaces.put("{min_y}", EReplace.of(String.valueOf(min.getY())));
					replaces.put("{min_z}", EReplace.of(String.valueOf(min.getZ())));
					replaces.put("{max_x}", EReplace.of(String.valueOf(max.getX())));
					replaces.put("{max_y}", EReplace.of(String.valueOf(max.getY())));
					replaces.put("{max_z}", EReplace.of(String.valueOf(max.getZ())));
					replaces.put("{region}", EReplace.of(region.getName()));
					replaces.put("{type}", EReplace.of(region.getType().getNameFormat()));
					
					int num = 1;
					for(Vector3i pos : region.getPoints()) {
						positions_text.add(EWMessages.REGION_DEFINE_POLYGONAL_POINTS_HOVER_LINE.getFormat()
								.toText("{num}", String.valueOf(num),
										"{x}", String.valueOf(pos.getX()),
										"{y}", String.valueOf(pos.getY()),
										"{z}", String.valueOf(pos.getZ())));
						num++;
					}				
					replaces.put("{positions}", EReplace.of(Text.joinWith(EWMessages.REGION_DEFINE_POLYGONAL_POINTS_HOVER_JOIN.getText(), positions_text)));
					
					EWMessages.REGION_DEFINE_POLYGONAL_CREATE.sender()
						.replaceString(replaces)
						.replace("{points}", EWMessages.REGION_DEFINE_POLYGONAL_POINTS.getFormat()
								.toText2(replaces).toBuilder()
								.onHover(TextActions.showText(EWMessages.REGION_DEFINE_POLYGONAL_POINTS_HOVER.getFormat()
										.toText2(replaces)))
								.build())
						.sendTo(player);
					return true;
				});
		} catch (RegionIdentifierException e) {
			EWMessages.REGION_DEFINE_ERROR_IDENTIFIER_INVALID.sender()
				.replace("{region}", region_id)
				.replace("{type}", ProtectedRegion.Types.POLYGONAL.getNameFormat())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
	}
	
	private CompletableFuture<Boolean> commandRegionDefineTemplate(final EPlayer player, final String region_id, final Set<UUID> players, final Set<String> groups) {
		if (!player.hasPermission(EWPermissions.REGION_DEFINE_TEMPLATE.get())) {
			EAMessages.NO_PERMISSION.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		try {
			return this.plugin.getProtectionService().getOrCreateEWorld(player.getWorld()).createRegionTemplate(region_id, players, groups, false)
				.exceptionally(e -> null)
				.thenApply(region -> {
					if (region == null) {
						EAMessages.COMMAND_ERROR.sendTo(player);
						return false;
					}
					
					EWMessages.REGION_DEFINE_TEMPLATE_CREATE.sender()
						.replace("{region}", region.getName())
						.replace("{type}", region.getType().getNameFormat())
						.sendTo(player);
					return true;
				});
		} catch (RegionIdentifierException e) {
			EWMessages.REGION_DEFINE_ERROR_IDENTIFIER_INVALID.sender()
				.replace("{region}", region_id)
				.replace("{type}", ProtectedRegion.Types.TEMPLATE.getNameFormat())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
	}
}
