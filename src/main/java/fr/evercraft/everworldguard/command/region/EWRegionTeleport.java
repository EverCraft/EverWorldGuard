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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import fr.evercraft.everapi.exception.message.EMessageException;
import fr.evercraft.everapi.message.replace.EReplace;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.location.VirtualTransform;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.Flags;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionTeleport extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_SPAWN = "-s";
	
	private final Args.Builder pattern;
	
	public EWRegionTeleport(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "teleport");
        
        this.pattern = Args.builder()
			.empty(MARKER_SPAWN)
			.value(Args.MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				return this.plugin.getProtectionService().getOrCreateEWorld(args.getWorld()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_TELEPORT.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_TELEPORT_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_SPAWN + "]"
												 + " [" + Args.MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(this.plugin, source, args);
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args_list) throws CommandException, EMessageException {
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		EPlayer player = (EPlayer) source;
		
		Args args = this.pattern.build(this.plugin, source, args_list);
		
		if (args.getArgs().size() != 1) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
		}
		List<String> args_string = args.getArgs();
		
		World world = args.getWorld();
		Optional<ProtectedRegion> region = this.plugin.getProtectionService().getOrCreateEWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{region}", args_string.get(0))
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!this.hasPermission(source, region.get(), world)) {
			EWMessages.REGION_NO_PERMISSION.sender()
				.replace("{region}", region.get().getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		 if (args.isOption(MARKER_SPAWN)) {
			return this.commandRegionSpawn(player, region.get(), world);
		} else {
			return this.commandRegionTeleport(player, region.get(), world);
		}
	}

	private CompletableFuture<Boolean> commandRegionTeleport(final EPlayer player, final ProtectedRegion region, final World world) {
		VirtualTransform location = region.getFlag(Flags.TELEPORT)
				.getInherit(region.getGroup(player, UtilsContexts.get(world.getName())))
				.orElseGet(() -> Flags.TELEPORT.getDefault(region));
		
		if (location.isEmpty()) {
			EWMessages.REGION_TELEPORT_TELEPORT_ERROR.sender()
				.replace("{region}", region.getName())
				.replace("{world}", world.getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!player.teleportSafeZone(location.getTransform(player.getTransform()), true)) {
			EAMessages.PLAYER_ERROR_TELEPORT.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		EWMessages.REGION_TELEPORT_TELEPORT.sender()
			.replace("{region}", region.getName())
			.replace("{world}", world.getName())
			.replace("{position}", () -> this.getTeleportHover(location)) 
			.sendTo(player);
		return CompletableFuture.completedFuture(true);
	}
	
	private Text getTeleportHover(final VirtualTransform location) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("{x}", EReplace.of(String.valueOf(location.getPosition().getFloorX())));
		replaces.put("{y}", EReplace.of(String.valueOf(location.getPosition().getFloorY())));
		replaces.put("{z}", EReplace.of(String.valueOf(location.getPosition().getFloorZ())));
		replaces.put("{pitch}", EReplace.of(String.valueOf(location.getPitch())));
		replaces.put("{yaw}", EReplace.of(String.valueOf(location.getYaw())));
		replaces.put("{world}", EReplace.of(location.getWorldName()));
		return EWMessages.REGION_TELEPORT_TELEPORT_POSITION.getFormat().toText2(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.REGION_TELEPORT_TELEPORT_POSITION_HOVER.getFormat().toText2(replaces)))
				.build();
	}

	private CompletableFuture<Boolean> commandRegionSpawn(final EPlayer player, final ProtectedRegion region, final World world) {
		if (!player.hasPermission(EWPermissions.REGION_TELEPORT_SPAWN.get())) {
			EAMessages.NO_PERMISSION.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<VirtualTransform> optLocation = region.getFlag(Flags.SPAWN).getInherit(region.getGroup(player, UtilsContexts.get(world.getName())));
		if (!optLocation.isPresent()) {
			EWMessages.REGION_TELEPORT_SPAWN_EMPTY.sender()
				.replace("{region}", region.getName())
				.replace("{world}", world.getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		VirtualTransform location = optLocation.get();
		
		if (location.isEmpty()) {
			EWMessages.REGION_TELEPORT_SPAWN_ERROR.sender()
				.replace("{region}", region.getName())
				.replace("{world}", world.getName())
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		if (!player.teleportSafe(location.getTransform().get(), true)) {
			EAMessages.PLAYER_ERROR_TELEPORT.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		EWMessages.REGION_TELEPORT_SPAWN.sender()
			.replace("{region}", region.getName())
			.replace("{world}", world.getName())
			.replace("{position}", () -> this.getSpawnHover(location)) 
			.sendTo(player);
		return CompletableFuture.completedFuture(true);
	}
	
	private Text getSpawnHover(final VirtualTransform location) {
		Map<String, EReplace<?>> replaces = new HashMap<String, EReplace<?>>();
		replaces.put("{x}", EReplace.of(String.valueOf(location.getPosition().getFloorX())));
		replaces.put("{y}", EReplace.of(String.valueOf(location.getPosition().getFloorY())));
		replaces.put("{z}", EReplace.of(String.valueOf(location.getPosition().getFloorZ())));
		replaces.put("{pitch}", EReplace.of(String.valueOf(location.getPitch())));
		replaces.put("{yaw}", EReplace.of(String.valueOf(location.getYaw())));
		replaces.put("{world}", EReplace.of(location.getWorldName()));
		return EWMessages.REGION_TELEPORT_SPAWN_POSITION.getFormat().toText2(replaces)
				.toBuilder()
				.onHover(TextActions.showText(EWMessages.REGION_TELEPORT_SPAWN_POSITION_HOVER.getFormat().toText2(replaces)))
				.build();
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_TELEPORT_REGIONS.get() + "." + region.getName().toLowerCase())) {
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
