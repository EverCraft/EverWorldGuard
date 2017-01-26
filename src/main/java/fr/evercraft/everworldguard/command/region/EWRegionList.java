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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
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
import fr.evercraft.everapi.services.worldguard.regions.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public class EWRegionList extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_PLAYER = "-p";
	public static final String MARKER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionList(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "list");
        
        this.pattern = Args.builder()
    			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
    			.value(MARKER_PLAYER, (source, args) -> this.getAllPlayers())
    			.value(MARKER_GROUP, (source, args) ->  {
    				List<String> suggests = new ArrayList<String>();
    				Optional<String> optWorld = args.getArg(0);
    				
    				if (optWorld.isPresent()) {
    					this.plugin.getEServer().getWorld(optWorld.get()).ifPresent(world -> 
    						suggests.addAll(this.getAllGroups(world)));
    				} else if (source instanceof Player) {
    					suggests.addAll(this.getAllGroups(((Player) source).getWorld()));
    				}
    				
    				return suggests;
    			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_INFO.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_LIST_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [-w" + EAMessages.ARGS_WORLD.getString() + "]"
												 + " [-p" + EAMessages.ARGS_PLAYER.getString() + " | -g" + EAMessages.ARGS_GROUP.getString() + "]")
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
		
		if (args.getArgs().size() == 0) {
			source.sendMessage(this.help(source));
			return false;
		}
		
		World world = null;
		Optional<String> arg_world = args.getValue(MARKER_WORLD);
		if (arg_world.isPresent()) {
			Optional<World> optWorld = this.plugin.getEServer().getWorld(arg_world.get());
			if (optWorld.isPresent()) {
				world = optWorld.get();
			} else {
				EAMessages.WORLD_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<world>", arg_world.get())
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
		
		Optional<String> arg_player = args.getValue(MARKER_PLAYER);
		Optional<String> arg_group = args.getValue(MARKER_GROUP);
		
		if (arg_player.isPresent() && arg_group.isPresent()) {
			source.sendMessage(this.help(source));
			return false;
		} else if (arg_player.isPresent()) {
			return this.commandRegionListPlayer(source, world, arg_player.get());
		} else if (arg_group.isPresent()) {
			return this.commandRegionListGroup(source, world, arg_group.get());
		} else {
			return this.commandRegionList(source, world);
		}
	}

	private boolean commandRegionList(CommandSource source, World world) {
		return false;
	}

	private boolean commandRegionListPlayer(CommandSource source, World world, String player_string) {
		Optional<EUser> user = this.plugin.getEServer().getEUser(player_string);
		// Le joueur est introuvable
		if (!user.isPresent()){
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		return false;
	}
	
	private boolean commandRegionListGroup(CommandSource source, World world, String group_string) {
		Optional<PermissionService> service = this.plugin.getEverAPI().getManagerService().getPermission();
		// Le joueur est introuvable
		if (!service.isPresent()){
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		Subject group = service.get().getGroupSubjects().get(group_string);
		// Le joueur est introuvable
		if (group == null){
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		Set<ProtectedRegion> regions = new HashSet<ProtectedRegion>();
		for (ProtectedRegion region : this.plugin.getService().getOrCreate(world).getAll()) {
		}
		
		return false;
	}
}
