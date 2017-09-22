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
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.RemoveTypes;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;

public class EWRegionRemove extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_FORCE = "-f";
	public static final String MARKER_UNSET_PARENT_IN_CHILDREN = "-u";
	
	private final Args.Builder pattern;
	
	public EWRegionRemove(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "remove");
        
        this.pattern = Args.builder()
        	.empty(MARKER_FORCE)
        	.empty(MARKER_UNSET_PARENT_IN_CHILDREN)
        	.value(Args.MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				Set<String> suggests = this.plugin.getProtectionService().getOrCreateEWorld(args.getWorld()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
				suggests.remove(EProtectionService.GLOBAL_REGION);
				return suggests;
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_REMOVE.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_REMOVE_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + Args.MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " [" + MARKER_FORCE + "|" + MARKER_UNSET_PARENT_IN_CHILDREN + "]"
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
		
		if (region.get().getType().equals(ProtectedRegion.Types.GLOBAL)) {
			EWMessages.REGION_REMOVE_ERROR_GLOBAL.sender()
				.replace("{region}", region.get().getName())
				.replace("{type}", region.get().getType().getNameFormat())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		if (args.isOption(MARKER_FORCE) && args.isOption(MARKER_UNSET_PARENT_IN_CHILDREN)) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
		} else if (args.isOption(MARKER_FORCE)) {
			return this.commandRegionRemoveForce(source, region.get(), world);
		} else if (args.isOption(MARKER_UNSET_PARENT_IN_CHILDREN)) {
			return this.commandRegionRemoveUnset(source, region.get(), world);
		} else {
			return this.commandRegionRemove(source, region.get(), world);
		}
	}
	
	private CompletableFuture<Boolean> commandRegionRemove(final CommandSource source, final ProtectedRegion region, final World world) {
		for (ProtectedRegion others : this.plugin.getProtectionService().getOrCreateEWorld(world).getAll()) {
			Optional<ProtectedRegion> parent = others.getParent();
			if (parent.isPresent() && parent.get().equals(region)) {
				EWMessages.REGION_REMOVE_ERROR_CHILDREN.sender()
					.replace("{region}", region.getName())
					.replace("{children}", others.getName())
					.replace("{world}", world.getName())
					.sendTo(source);
				return CompletableFuture.completedFuture(false);
			}
		}
		
		return this.plugin.getProtectionService().getOrCreateEWorld(world).removeRegion(region.getId(), RemoveTypes.UNSET_PARENT_IN_CHILDREN)
			.exceptionally(e -> null)
			.thenApply(result -> {
				if (result == null) {
					EAMessages.COMMAND_ERROR.sendTo(source);
					return false;
				}
				
				EWMessages.REGION_REMOVE_REGION.sender()
					.replace("{region}", region.getName())
					.replace("{world}", world.getName())
					.sendTo(source);	
				return true;
			});
	}
	
	private CompletableFuture<Boolean> commandRegionRemoveForce(final CommandSource source, final ProtectedRegion region, final World world) {
		return this.plugin.getProtectionService().getOrCreateEWorld(world).removeRegion(region.getId(), RemoveTypes.REMOVE_CHILDREN)
			.exceptionally(e -> null)
			.thenApply(result -> {
				if (result == null) {
					EAMessages.COMMAND_ERROR.sendTo(source);
					return false;
				}
				
				EWMessages.REGION_REMOVE_CHILDREN_REMOVE.sender()
					.replace("{region}", region.getName())
					.replace("{world}", world.getName())
					.sendTo(source);	
				return true;
			});
	}
	
	private CompletableFuture<Boolean> commandRegionRemoveUnset(final CommandSource source, final ProtectedRegion region, final World world) {
		return this.plugin.getProtectionService().getOrCreateEWorld(world).removeRegion(region.getId(), RemoveTypes.UNSET_PARENT_IN_CHILDREN)
			.exceptionally(e -> null)
			.thenApply(result -> {
				if (result == null) {
					EAMessages.COMMAND_ERROR.sendTo(source);
					return false;
				}
				
				EWMessages.REGION_REMOVE_CHILDREN_UNSET.sender()
					.replace("{region}", region.getName())
					.replace("{world}", world.getName())
					.sendTo(source);
				return true;
			});
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_REMOVE_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_REMOVE_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_REMOVE_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
