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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionOwnerAdd extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_OWNER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionOwnerAdd(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "define");
        
        this.pattern = Args.builder()
			.empty(MARKER_OWNER_GROUP)
			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
			.arg((source, args) -> {
				List<String> suggests = new ArrayList<String>();
				Optional<String> optWorld = args.getValue(MARKER_WORLD);
				
				if (optWorld.isPresent()) {
					this.plugin.getEServer().getWorld(optWorld.get()).ifPresent(world -> 
						this.plugin.getService().getOrCreateWorld(world).getAll().forEach(region ->
							suggests.add(region.getIdentifier())
					));
				} else if (source instanceof Player) {
					this.plugin.getService().getOrCreateWorld(((Player) source).getWorld()).getAll().forEach(region ->
						suggests.add(region.getIdentifier())
					);
				}
				
				return suggests;
			})
			.args((source, args) -> {
				if (args.isOption(MARKER_OWNER_GROUP)) {
					return this.getAllGroups();
				} else {
					List<String> list = args.getArgs();
					return this.getAllUsers(list.get(list.size()-1));
				}
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_OWNER_ADD.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_OWNER_ADD_DESCRIPTION.getText();
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
		
		Optional<ProtectedRegion> region = this.plugin.getService().getOrCreateWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		if (args.isOption(MARKER_OWNER_GROUP)) {
			return this.commandRegionOwnerAddGroup(source, region.get(), args.getArgs(1), world);
		} else {
			return this.commandRegionOwnerAddPlayer(source, region.get(), args.getArgs(1), world);
		}
	}
	
	private boolean commandRegionOwnerAddPlayer(final CommandSource source, ProtectedRegion region, List<String> players_string, World world) {
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
		
		region.addPlayerOwner(players);
		EWMessages.REGION_OWNER_ADD_PLAYERS.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<owners>", String.join(EWMessages.REGION_OWNER_ADD_PLAYERS_JOIN.getString(), players.stream().map(owner -> owner.getName()).collect(Collectors.toList())))
			.sendTo(source);
		return true;
	}
	
	private boolean commandRegionOwnerAddGroup(final CommandSource source, ProtectedRegion region, List<String> groups_string, World world) {
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
		
		return false;
	}
}
