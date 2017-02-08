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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionMemberRemove extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_MEMBER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionMemberRemove(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "removemember");
        
		BiFunction<CommandSource, Args, Optional<World>> world = (source, args) -> {
			Optional<String> optWorld = args.getValue(MARKER_WORLD);
			
			if (optWorld.isPresent()) {
				return this.plugin.getEServer().getWorld(optWorld.get());
			} else if (source instanceof Player) {
				return Optional.of(((Player) source).getWorld());
			}
			return Optional.empty();
		};
		
		this.pattern = Args.builder()
			.empty(MARKER_MEMBER_GROUP)
			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
			.arg((source, args) -> {
				Optional<World> optWorld = world.apply(source, args);
				if (!optWorld.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getService().getOrCreateWorld(optWorld.get()).getAll().stream()
					.map(region -> region.getIdentifier())
					.collect(Collectors.toSet());
			})
			.args((source, args) -> {
				Optional<World> optWorld = world.apply(source, args);
				if (!optWorld.isPresent()) {
					return Arrays.asList();
				}
		
				Optional<ProtectedRegion> optRegion = this.plugin.getService().getOrCreateEWorld(optWorld.get()).getRegion(args.getArg(0).get());
				if (!optRegion.isPresent()) {
					return Arrays.asList();
				}
		
				if (args.isOption(MARKER_MEMBER_GROUP)) {
					return optRegion.get().getMembers().getGroups();
				} else {
					return optRegion.get().getMembers().getPlayers().stream()
						.map(player -> {
							Optional<EUser> user = this.plugin.getEServer().getEUser(player);
							if (user.isPresent()) {
								return user.get().getName();
							} else {
								return "";
							}
						})
						.collect(Collectors.toSet());
				}
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_MEMBER_REMOVE.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_MEMBER_REMOVE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "] "
												  + "<" + EAMessages.ARGS_REGION.getString() + "> "
												  + "[" + MARKER_MEMBER_GROUP + "] "
												  + "<" + EAMessages.ARGS_MEMBER.getString() + "...>")
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
		Args args = this.pattern.build(args_list);
		
		if (args.getArgs().size() < 2) {
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
		
		Optional<ProtectedRegion> region = this.plugin.getService().getOrCreateWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		if (args.isOption(MARKER_MEMBER_GROUP)) {
			return this.commandRegionMemberRemoveGroup(source, region.get(), args.getArgs(1), world);
		} else {
			return this.commandRegionMemberRemovePlayer(source, region.get(), args.getArgs(1), world);
		}
	}
	
	private boolean commandRegionMemberRemovePlayer(final CommandSource source, ProtectedRegion region, List<String> players_string, World world) {		
		Set<User> players = new HashSet<User>();
		for (String player_string : players_string) {
			Optional<EUser> user = this.plugin.getEServer().getEUser(player_string);
			if (user.isPresent()) {
				players.add(user.get());
			} else {
				EAMessages.PLAYER_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<player>", player_string)
					.sendTo(source);
				return false;
			}
		}
		
		if (players.size() == 1) {
			return this.commandRegionMemberRemovePlayer(source, region, players, world);
		} else {
			return this.commandRegionMemberRemovePlayer(source, region, players.iterator().next(), world);
		}
	}
	
	private boolean commandRegionMemberRemovePlayer(final CommandSource source, ProtectedRegion region, Set<User> players, World world) {
		region.removePlayerMember(players);
		EWMessages.REGION_MEMBER_REMOVE_PLAYERS.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<players>", String.join(EWMessages.REGION_MEMBER_REMOVE_PLAYERS_JOIN.getString(), players.stream().map(owner -> owner.getName()).collect(Collectors.toList())))
			.sendTo(source);
		return true;
	}
	
	private boolean commandRegionMemberRemovePlayer(final CommandSource source, ProtectedRegion region, User player, World world) {
		if (region.getMembers().containsPlayer(player)) {
			EWMessages.REGION_MEMBER_REMOVE_PLAYER_ERROR.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.replace("<player>", player.getName())
				.sendTo(source);
		} else {
			region.removePlayerMember(ImmutableSet.of(player));
			EWMessages.REGION_MEMBER_REMOVE_PLAYER.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.replace("<player>", player.getName())
				.sendTo(source);
		}
		return true;
	}
	
	private boolean commandRegionMemberRemoveGroup(final CommandSource source, ProtectedRegion region, List<String> groups_string, World world) {
		Optional<PermissionService> service = this.plugin.getEverAPI().getManagerService().getPermission();
		if (!service.isPresent()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		Set<Subject> groups = new HashSet<Subject>();
		for (String group_string : groups_string) {
			Subject group = service.get().getGroupSubjects().get(group_string);
			if (group != null) {
				groups.add(group);
			} else {
				EAMessages.GROUP_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<player>", group_string)
					.sendTo(source);
				return false;
			}
		}
		
		if (groups.size() == 1) {
			return this.commandRegionMemberRemoveGroup(source, region, groups, world);
		} else {
			return this.commandRegionMemberRemoveGroup(source, region, groups.iterator().next(), world);
		}
	}
	
	private boolean commandRegionMemberRemoveGroup(final CommandSource source, ProtectedRegion region, Set<Subject> groups, World world) {
		region.removeGroupMember(groups);
		EWMessages.REGION_MEMBER_REMOVE_GROUPS.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<players>", String.join(EWMessages.REGION_MEMBER_REMOVE_GROUPS_JOIN.getString(), groups.stream().map(owner -> owner.getIdentifier()).collect(Collectors.toList())))
			.sendTo(source);
		return true;
	}
	
	private boolean commandRegionMemberRemoveGroup(final CommandSource source, ProtectedRegion region, Subject group, World world) {
		if (region.getMembers().containsGroup(group)) {
			EWMessages.REGION_MEMBER_REMOVE_GROUP_ERROR.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.replace("<group>", group.getIdentifier())
				.sendTo(source);
			return false;
		}
			
		region.removeGroupMember(ImmutableSet.of(group));
		EWMessages.REGION_MEMBER_REMOVE_GROUP.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<player>", group.getIdentifier())
			.sendTo(source);
		return true;
	}
}