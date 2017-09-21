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
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsContexts;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionParent extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_EMPTY = "-e";
	
	private final Args.Builder pattern;
	
	public EWRegionParent(final EverWorldGuard plugin, final EWRegion command) {
		super(plugin, command, "setparent");
		
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
				Optional<World> optWorld = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!optWorld.isPresent()) {
					return Arrays.asList();
				}
				
				Set<String> suggests = this.plugin.getProtectionService().getOrCreateEWorld(optWorld.get()).getAll().stream()
					.map(region -> region.getName())
					.collect(Collectors.toSet());
				suggests.remove(args.getArg(0).get());
				suggests.add(MARKER_EMPTY);
				return suggests;
			});
	}
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_PARENT.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_PARENT_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">"
												 + " [" + EAMessages.ARGS_PARENT.getString() + "|" + MARKER_EMPTY + "]")
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
		Args args = this.pattern.build(this.plugin, source, args_list);
		
		if (args.getArgs().isEmpty() || args.getArgs().size() > 2) {
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
					.replace("{world}", world_arg.get())
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
		
		String parent = args.getArg(1).orElse("");
		if (parent.isEmpty() || parent.equalsIgnoreCase(MARKER_EMPTY)) {
			return this.commandRegionRemoveParent(source, region.get(), world);
		} else {
			return this.commandRegionSetParent(source, region.get(), manager, parent, world);
		}
	}

	private CompletableFuture<Boolean> commandRegionSetParent(final CommandSource source, final ProtectedRegion region, 
			final WorldGuardWorld manager, final String parent_string, final World world) {
		Optional<ProtectedRegion> optParent = manager.getRegion(parent_string);
		// Region introuvable
		if (!optParent.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{region}", parent_string)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		ProtectedRegion parent = optParent.get();
		
		if (region.equals(parent)) {
			EWMessages.REGION_PARENT_SET_EQUALS.sender()
				.replace("{region}", region.getName())
				.replace("{parent}", parent.getName())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<ProtectedRegion> region_parent = region.getParent();
		if (region_parent.isPresent() && region_parent.get().equals(parent)) {
			EWMessages.REGION_PARENT_SET_EQUALS_PARENT.sender()
				.replace("{region}", region.getName())
				.replace("{parent}", parent.getName())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		try {
			region.setParent(parent)
				.exceptionally(e -> false)
				.thenApply(result -> {
					if (!result) {
						EAMessages.COMMAND_ERROR.sendTo(source);
						return false;
					}
					
					// Héritage
					List<ProtectedRegion> parents = region.getHeritage();
						
					if (parents == null || parents.size() == 1) {
						EWMessages.REGION_PARENT_SET.sender()
							.replace("{region}", region.getName())
							.replace("{parent}", parent.getName())
							.replace("{world}", world.getName())
							.sendTo(source);
					} else {
						List<Text> messages = new ArrayList<Text>();
						Text padding = Text.EMPTY;
						
						for (int cpt=0; cpt < parents.size(); cpt++) {
							padding = padding.concat(EWMessages.REGION_PARENT_SET_HERITAGE_PADDING.getText());
							
							ProtectedRegion curParent = parents.get(cpt);
							messages.add(padding.concat(EWMessages.REGION_PARENT_SET_HERITAGE_LINE.getFormat()
								.toText("{region}", Text.builder(curParent.getName())
											.onShiftClick(TextActions.insertText(curParent.getName()))
											.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\" \"" + curParent.getName() + "\" "))
											.build(),
										"{type}", curParent.getType().getNameFormat(),
										"{priority}", String.valueOf(curParent.getPriority()))));
						}
						
						EWMessages.REGION_PARENT_SET_HERITAGE.sender()
							.replace("{region}", region.getName())
							.replace("{parent}", parent.getName())
							.replace("{world}", world.getName())
							.replace("{heritage}", Text.joinWith(Text.of("\n"), messages))
							.sendTo(source);
					}		
					return true;
				});
		} catch (CircularInheritanceException e) {
			EWMessages.REGION_PARENT_SET_CIRCULAR.sender()
				.replace("{region}", region.getName())
				.replace("{parent}", parent.getName())
				.replace("{world}", world.getName())
				.sendTo(source);
		}
		return CompletableFuture.completedFuture(false);
	}
	
	private CompletableFuture<Boolean> commandRegionRemoveParent(final CommandSource source, final ProtectedRegion region, final World world) {
		if (!region.getParent().isPresent()) {
			EWMessages.REGION_PARENT_REMOVE_EMPTY.sender()
				.replace("{region}", region.getName())
				.replace("{world}", world.getName())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		
		return region.clearParent()
			.exceptionally(e -> false)
			.thenApply(result -> {
				if (!result) {
					EAMessages.COMMAND_ERROR.sendTo(source);
					return false;
				}
				
				EWMessages.REGION_PARENT_REMOVE.sender()
					.replace("{region}", region.getName())
					.replace("{world}", world.getName())
					.sendTo(source);
				return true;
			});
	}
	
	private boolean hasPermission(final CommandSource source, final ProtectedRegion region, final World world) {
		if (source.hasPermission(EWPermissions.REGION_PARENT_REGIONS.get() + "." + region.getName().toLowerCase())) {
			return true;
		}
		
		if (!(source instanceof EPlayer)) {
			EPlayer player = (EPlayer) source;
			
			if (region.isPlayerOwner(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_PARENT_OWNER.get())) {
				return true;
			}
			
			if (region.isPlayerMember(player, UtilsContexts.get(world.getName())) && source.hasPermission(EWPermissions.REGION_PARENT_MEMBER.get())) {
				return true;
			}
		}
		return false;
	}
}
