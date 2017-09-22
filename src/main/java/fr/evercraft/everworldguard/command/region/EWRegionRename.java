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
import fr.evercraft.everapi.exception.message.EMessageException;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionRename extends ESubCommand<EverWorldGuard> {
		
	private final Args.Builder pattern;
	
	public EWRegionRename(final EverWorldGuard plugin, final EWRegion command) {
		super(plugin, command, "rename");
		
		this.pattern = Args.builder()
			.value(Args.MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1)
			.arg((source, args) -> {
				return this.plugin.getProtectionService().getOrCreateEWorld(args.getWorld()).getAll().stream()
							.map(region -> region.getName())
							.collect(Collectors.toSet());
			})
			.arg((source, args) -> Arrays.asList("region..."));
	}
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_RENAME.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_RENAME_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + Args.MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">"
												 + " <" + EAMessages.ARGS_VALUE.getString() + ">")
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
		
		if (args.getArgs().size() != 2) {
			source.sendMessage(this.help(source));
			return CompletableFuture.completedFuture(false);
		}
		List<String> args_string = args.getArgs();
		
		World world = args.getWorld();
		WorldGuardWorld manager = this.plugin.getProtectionService().getOrCreateEWorld(world);
		Optional<ProtectedRegion> region = manager.getRegion(args_string.get(0));
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
		
		return this.commandRegionRename(source, manager, region.get(), args.getArg(1).get(), world);
	}

	private CompletableFuture<Boolean> commandRegionRename(final CommandSource source, final WorldGuardWorld manager, 
			final ProtectedRegion region, final String region_string, final World world) {
		String before_identifier = region.getName();
		if (region.getType().equals(ProtectedRegion.Types.GLOBAL)) {
			EWMessages.REGION_RENAME_ERROR_GLOBAL.sender()
				.replace("{region}", region.getName())
				.replace("{type}", region.getType().getNameFormat())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		if (manager.getRegion(region_string).isPresent()) {
			EWMessages.REGION_RENAME_ERROR_IDENTIFIER_EQUALS.sender()
				.replace("{region}", region.getName())
				.replace("{identifier}", region_string)
				.replace("{type}", region.getType().getNameFormat())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		try {
			return region.setName(region_string)
				.exceptionally(e -> false)
				.thenApply(result -> {
					if (!result) {
						EAMessages.COMMAND_ERROR.sendTo(source);
						return false;
					}
					
					EWMessages.REGION_RENAME_SET.sender()
						.replace("{region}", before_identifier)
						.replace("{identifier}", region_string)
						.replace("{type}", region.getType().getNameFormat())
						.replace("{world}", world.getName())
						.sendTo(source);
					return true;
				});
		} catch (RegionIdentifierException e) {
			EWMessages.REGION_RENAME_ERROR_IDENTIFIER_INVALID.sender()
				.replace("{region}", before_identifier)
				.replace("{identifier}", region_string)
				.replace("{type}", region.getType().getNameFormat())
				.replace("{world}", world.getName())
				.sendTo(source);
		}
		return CompletableFuture.completedFuture(false);
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_RENAME_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_RENAME_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_RENAME_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
