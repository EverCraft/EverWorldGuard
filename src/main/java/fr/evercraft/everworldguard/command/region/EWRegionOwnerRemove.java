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
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
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
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionOwnerRemove extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_OWNER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionOwnerRemove(final EverWorldGuard plugin, final EWRegion command) {
		super(plugin, command, "removeowner");
		
		this.pattern = Args.builder()
			.empty(MARKER_OWNER_GROUP,
					(source, args) -> args.getArgs().size() == 2)
			.value(MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				Optional<World> world = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getProtectionService().getOrCreateEWorld(world.get()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
			})
			.args((source, args) -> {
				Optional<World> world =EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
		
				Optional<ProtectedRegion> optRegion = this.plugin.getProtectionService().getOrCreateEWorld(world.get()).getRegion(args.getArg(0).get());
				if (!optRegion.isPresent()) {
					return Arrays.asList();
				}
		
				if (args.isOption(MARKER_OWNER_GROUP)) {
					return optRegion.get().getOwners().getGroups();
				} else {
					return optRegion.get().getOwners().getPlayers().stream()
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
		return source.hasPermission(EWPermissions.REGION_OWNER_REMOVE.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_OWNER_REMOVE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "] "
												  + "<" + EAMessages.ARGS_REGION.getString() + "> "
												  + "[" + MARKER_OWNER_GROUP + "] "
												  + "<" + EAMessages.ARGS_OWNER.getString() + "...>")
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
		
		Optional<ProtectedRegion> region = this.plugin.getProtectionService().getOrCreateEWorld(world).getRegion(args_string.get(0));
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
		
		if (args.isOption(MARKER_OWNER_GROUP)) {
			return this.commandRegionOwnerRemoveGroup(source, region.get(), args.getArgs(1), world);
		} else {
			return this.commandRegionOwnerRemovePlayer(source, region.get(), args.getArgs(1), world);
		}
	}
	
	private boolean commandRegionOwnerRemovePlayer(final CommandSource source, ProtectedRegion region, List<String> players_string, World world) {		
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
		
		if (players.size() > 1) {
			return this.commandRegionOwnerRemovePlayer(source, region, players, world);
		} else {
			return this.commandRegionOwnerRemovePlayer(source, region, players.iterator().next(), world);
		}
	}
	
	private boolean commandRegionOwnerRemovePlayer(final CommandSource source, ProtectedRegion region, Set<User> players, World world) {
		region.removePlayerOwner(players.stream()
				.map(user -> user.getUniqueId())
				.collect(Collectors.toSet()));
		EWMessages.REGION_OWNER_REMOVE_PLAYERS.sender()
			.replace("<region>", region.getName())
			.replace("<world>", world.getName())
			.replace("<players>", String.join(EWMessages.REGION_OWNER_REMOVE_PLAYERS_JOIN.getString(), players.stream().map(owner -> owner.getName()).collect(Collectors.toList())))
			.sendTo(source);
		return true;
	}
	
	private boolean commandRegionOwnerRemovePlayer(final CommandSource source, ProtectedRegion region, User player, World world) {
		if (!region.getOwners().containsPlayer(player.getUniqueId())) {
			EWMessages.REGION_OWNER_REMOVE_PLAYER_ERROR.sender()
				.replace("<region>", region.getName())
				.replace("<world>", world.getName())
				.replace("<player>", player.getName())
				.sendTo(source);
		} else {
			region.removePlayerOwner(ImmutableSet.of(player.getUniqueId()));
			EWMessages.REGION_OWNER_REMOVE_PLAYER.sender()
				.replace("<region>", region.getName())
				.replace("<world>", world.getName())
				.replace("<player>", player.getName())
				.sendTo(source);
		}
		return true;
	}
	
	private boolean commandRegionOwnerRemoveGroup(final CommandSource source, ProtectedRegion region, List<String> groups_string, World world) {
		Set<Subject> groups = new HashSet<Subject>();
		for (String group_string : groups_string) {
			Subject group = this.plugin.getEverAPI().getManagerService().getPermission().getGroupSubjects().get(group_string);
			if (group != null) {
				groups.add(group);
			} else {
				EAMessages.GROUP_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<group>", group_string)
					.sendTo(source);
				return false;
			}
		}
		
		if (groups.size() > 1) {
			return this.commandRegionOwnerRemoveGroup(source, region, groups, world);
		} else {
			return this.commandRegionOwnerRemoveGroup(source, region, groups.iterator().next(), world);
		}
	}
	
	private boolean commandRegionOwnerRemoveGroup(final CommandSource source, ProtectedRegion region, Set<Subject> groups, World world) {
		region.removeGroupOwner(groups.stream()
				.map(group -> group.getIdentifier())
				.collect(Collectors.toSet()));
		EWMessages.REGION_OWNER_REMOVE_GROUPS.sender()
			.replace("<region>", region.getName())
			.replace("<world>", world.getName())
			.replace("<groups>", String.join(EWMessages.REGION_OWNER_REMOVE_GROUPS_JOIN.getString(), groups.stream().map(owner -> owner.getIdentifier()).collect(Collectors.toList())))
			.sendTo(source);
		return true;
	}
	
	private boolean commandRegionOwnerRemoveGroup(final CommandSource source, ProtectedRegion region, Subject group, World world) {
		if (region.getOwners().containsGroup(group)) {
			EWMessages.REGION_OWNER_REMOVE_GROUP_ERROR.sender()
				.replace("<region>", region.getName())
				.replace("<world>", world.getName())
				.replace("<group>", group.getIdentifier())
				.sendTo(source);
			return false;
		}
			
		region.removeGroupOwner(ImmutableSet.of(group.getIdentifier()));
		EWMessages.REGION_OWNER_REMOVE_GROUP.sender()
			.replace("<region>", region.getName())
			.replace("<world>", world.getName())
			.replace("<group>", group.getIdentifier())
			.sendTo(source);
		return true;
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_OWNER_REMOVE_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_OWNER_REMOVE_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_OWNER_REMOVE_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
