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

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.location.VirtualTransform;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionTeleport extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_SPAWN = "-s";
	
	private final Args.Builder pattern;
	
	public EWRegionTeleport(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "teleport");
        
        this.pattern = Args.builder()
			.empty(MARKER_SPAWN)
			.value(MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				Optional<World> world = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getProtectionService().getOrCreateWorld(world.get()).getAll().stream()
							.map(region -> region.getIdentifier())
							.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_TELEPORT.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_LIST_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_SPAWN + "]"
												 + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "] "
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
				.replace("<region>", region.get().getIdentifier())
				.sendTo(source);
			return false;
		}
		
		 if (args.isOption(MARKER_SPAWN)) {
			return this.commandRegionSpawn(player, region.get(), world);
		} else {
			return this.commandRegionTeleport(player, region.get(), world);
		}
	}

	private boolean commandRegionTeleport(EPlayer player, ProtectedRegion region, World world) {
		VirtualTransform location = region.getFlag(Flags.TELEPORT)
				.getInherit(region.getGroup(player, UtilsContexts.get(world.getName())))
				.orElseGet(() -> Flags.TELEPORT.getDefault(region));
		
		if (location.isEmpty()) {
			EWMessages.REGION_TELEPORT_TELEPORT_ERROR.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(player);
			return false;
		}
		
		if (!player.teleportSafeZone(location.getTransform(player.getTransform()), true)) {
			EAMessages.PLAYER_ERROR_TELEPORT.sendTo(player);
			return false;
		}
		
		EWMessages.REGION_TELEPORT_TELEPORT.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<position>", () -> this.getTeleportHover(location)) 
			.sendTo(player);
		return true;
	}
	
	private Text getTeleportHover(final VirtualTransform location) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<x>", EReplace.of(String.valueOf(location.getPosition().getFloorX())));
		replaces.put("<y>", EReplace.of(String.valueOf(location.getPosition().getFloorY())));
		replaces.put("<z>", EReplace.of(String.valueOf(location.getPosition().getFloorZ())));
		replaces.put("<pitch>", EReplace.of(String.valueOf(location.getPitch())));
		replaces.put("<yaw>", EReplace.of(String.valueOf(location.getYaw())));
		replaces.put("<world>", EReplace.of(location.getWorldName()));
		return EWMessages.REGION_TELEPORT_TELEPORT_POSITION.getFormat().toText(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.REGION_TELEPORT_TELEPORT_POSITION_HOVER.getFormat().toText(replaces)))
				.build();
	}

	private boolean commandRegionSpawn(EPlayer player, ProtectedRegion region, World world) {
		if (!player.hasPermission(EWPermissions.REGION_TELEPORT_SPAWN.get())) {
			EAMessages.NO_PERMISSION.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		Optional<VirtualTransform> optLocation = region.getFlag(Flags.SPAWN).getInherit(region.getGroup(player, UtilsContexts.get(world.getName())));
		if (!optLocation.isPresent()) {
			EWMessages.REGION_TELEPORT_SPAWN_EMPTY.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(player);
			return false;
		}
		VirtualTransform location = optLocation.get();
		
		if (location.isEmpty()) {
			EWMessages.REGION_TELEPORT_SPAWN_ERROR.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(player);
			return false;
		}
		
		if (!player.teleportSafe(location.getTransform().get(), true)) {
			EAMessages.PLAYER_ERROR_TELEPORT.sendTo(player);
			return false;
		}
		
		EWMessages.REGION_TELEPORT_SPAWN.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.replace("<position>", () -> this.getSpawnHover(location)) 
			.sendTo(player);
		return true;
	}
	
	private Text getSpawnHover(final VirtualTransform location) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("<x>", EReplace.of(String.valueOf(location.getPosition().getFloorX())));
		replaces.put("<y>", EReplace.of(String.valueOf(location.getPosition().getFloorY())));
		replaces.put("<z>", EReplace.of(String.valueOf(location.getPosition().getFloorZ())));
		replaces.put("<pitch>", EReplace.of(String.valueOf(location.getPitch())));
		replaces.put("<yaw>", EReplace.of(String.valueOf(location.getYaw())));
		replaces.put("<world>", EReplace.of(location.getWorldName()));
		return EWMessages.REGION_TELEPORT_SPAWN_POSITION.getFormat().toText(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.REGION_TELEPORT_SPAWN_POSITION_HOVER.getFormat().toText(replaces)))
				.build();
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_TELEPORT_REGIONS.get() + "." + region.getIdentifier().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_TELEPORT_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_TELEPORT_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
