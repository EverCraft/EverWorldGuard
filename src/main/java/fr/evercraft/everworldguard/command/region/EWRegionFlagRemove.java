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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.EMessageFormat;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionFlagRemove extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	
	private final Args.Builder pattern;
	
	public EWRegionFlagRemove(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "removeflag");
        
        this.pattern = Args.builder()
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
			.arg((source, args) -> {
				return this.plugin.getProtectionService().getFlags().stream()
						.map(flag -> flag.getName())
						.collect(Collectors.toSet());
			})
			.arg((source, args) -> {
				Optional<String> flag_string = args.getArg(1);
				if (!flag_string.isPresent()) {
					return Arrays.asList();
				}
				
				Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(flag_string.get());
				if (!flag_string.isPresent()) {
					return Arrays.asList();
				}
				
				List<String> suggests = new ArrayList<String>();
				for(Group group : flag.get().getGroups()) {
					suggests.add(group.getName());
				}
				return suggests;
			})
			.args((source, args) -> {
				Optional<String> flag_string = args.getArg(1);
				if (!flag_string.isPresent()) {
					return Arrays.asList();
				}
				
				Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(flag_string.get());
				if (!flag_string.isPresent()) {
					return Arrays.asList();
				}
				
				return flag.get().getSuggestRemove(source, args.getArgs(3));
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_LIST.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_FLAG_REMOVE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">"
												 + " <" + EAMessages.ARGS_FLAG.getString() + ">"
												 + " <" + EAMessages.ARGS_REGION_GROUP.getString() + ">"
												 + " [" + EAMessages.ARGS_FLAG_VALUE.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(source, args);
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args_list) throws CommandException {
		Args args = this.pattern.build(args_list);
		
		if (args.getArgs().size() < 3) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
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
				return CompletableFuture.completedFuture(false);
			}
		} else if (source instanceof EPlayer) {
			world = ((EPlayer) source).getWorld();
		} else {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<ProtectedRegion> region = this.plugin.getProtectionService().getOrCreateEWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!this.hasPermission(source, region.get(), world)) {
			EWMessages.REGION_NO_PERMISSION.sender()
				.replace("<region>", region.get().getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<Flag<?>> flag = this.plugin.getProtectionService().getFlag(args_string.get(1));
		if (!flag.isPresent()) {
			EWMessages.FLAG_NOT_FOUND.sender()
				.replace("<group>", args_string.get(1))
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<Group> group = this.plugin.getGame().getRegistry().getType(Group.class, args_string.get(2));
		if (!group.isPresent()) {
			EWMessages.GROUP_NOT_FOUND.sender()
				.replace("<group>", args_string.get(2))
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandRegionFlagRemove(source, region.get(), group.get(), flag.get(), args.getArgs(3), world);
	}

	private <T> CompletableFuture<Boolean> commandRegionFlagRemove(final CommandSource source, ProtectedRegion region, Group group, Flag<T> flag, List<String> values, World world) {
		try {
			Optional<T> value = flag.parseRemove(source, region, group, values);
			
			region.setFlag(flag, group, value.orElse(null))
				.thenApply(result -> {
					if (!result) {
						EAMessages.COMMAND_ERROR.sendTo(source);
						return false;
					}
					
					if (value.isPresent()) {
						EWMessages.REGION_FLAG_REMOVE_UPDATE.sender()
							.replace("<region>", region.getName())
							.replace("<group>", group.getNameFormat())
							.replace("<flag>", flag.getNameFormat())
							.replace("<world>", world.getName())
							.replace("<value>", flag.getValueFormat(value.get()))
							.sendTo(source);
					} else {
						EWMessages.REGION_FLAG_REMOVE_PLAYER.sender()
							.replace("<region>", region.getName())
							.replace("<group>", group.getNameFormat())
							.replace("<flag>", flag.getNameFormat())
							.replace("<world>", world.getName())
							.sendTo(source);
					}
					return true;
				});
			
			return CompletableFuture.completedFuture(true);
		} catch (IllegalArgumentException e) {
			if (e.getMessage() == null || e.getMessage().isEmpty()) {
				EWMessages.REGION_FLAG_REMOVE_ERROR.sender()
					.replace("<region>", region.getName())
					.replace("<group>", group.getNameFormat())
					.replace("<flag>", flag.getNameFormat())
					.replace("<world>", world.getName())
					.replace("<value>", String.join(" ", values))
					.sendTo(source);
			} else {
				EMessageFormat.builder()
					.prefix(EWMessages.PREFIX)
					.chatMessageString(e.getMessage())
					.build().sender()
					.sendTo(source);
			}
			return CompletableFuture.completedFuture(false);
		}
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_FLAG_REMOVE_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_FLAG_REMOVE_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_FLAG_REMOVE_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
