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
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
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
			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
			.arg((source, args) -> {
				Optional<World> optWorld = this.getWorld(source, args);
				if (!optWorld.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getService().getOrCreateWorld(optWorld.get()).getAll().stream()
					.map(region -> region.getIdentifier())
					.collect(Collectors.toSet());
			})
			.args((source, args) -> {
				Optional<World> optWorld = this.getWorld(source, args);
				if (!optWorld.isPresent()) {
					return Arrays.asList();
				}
				
				Set<String> suggests = this.plugin.getService().getOrCreateWorld(optWorld.get()).getAll().stream()
					.map(region -> region.getIdentifier())
					.collect(Collectors.toSet());
				suggests.remove(args.getArg(0).get());
				suggests.add(MARKER_EMPTY);
				return suggests;
			});
	}
	
	private Optional<World> getWorld(CommandSource source, Args args) {
		Optional<String> optWorld = args.getValue(MARKER_WORLD);
		
		if (optWorld.isPresent()) {
			return this.plugin.getEServer().getWorld(optWorld.get());
		} else if (source instanceof Player) {
			return Optional.of(((Player) source).getWorld());
		}
		return Optional.empty();
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
												 + " <" + EAMessages.ARGS_FLAG.getString() + ">"
												 + " <" + EAMessages.ARGS_REGION_GROUP.getString() + ">"
												 + " <" + EAMessages.ARGS_FLAG_VALUE.getString() + "...>")
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
		
		if (args.getArgs().size() != 2) {
			source.sendMessage(this.help(source));
			return false;
		}
		List<String> args_string = args.getArgs();
		
		Optional<World> optWorld = this.getWorld(source, args);
		if (!optWorld.isPresent()) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		
		WorldWorldGuard manager = this.plugin.getService().getOrCreateWorld(optWorld.get());
		
		Optional<ProtectedRegion> region = manager.getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		String parent = args.getArg(1).get();
		if (parent.isEmpty() || parent.equalsIgnoreCase(MARKER_EMPTY)) {
			return this.commandRegionRemoveParent(source, region.get(), optWorld.get());
		} else {
			return this.commandRegionSetParent(source, region.get(), manager, parent, optWorld.get());
		}
	}

	private boolean commandRegionSetParent(final CommandSource source, ProtectedRegion region, WorldWorldGuard manager, String parent_string, World world) {
		Optional<ProtectedRegion> parent = manager.getRegion(parent_string);
		// Region introuvable
		if (!parent.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", parent_string)
				.sendTo(source);
			return false;
		}
		
		if (region.equals(parent)) {
			EWMessages.REGION_PARENT_SET_EQUALS.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(source);
			return false;
		}
		
		Optional<ProtectedRegion> region_parent = region.getParent();
		if (region_parent.isPresent() && region_parent.get().equals(parent)) {
			EWMessages.REGION_PARENT_SET_EQUALS_PARENT.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(source);
			return false;
		}
		
		try {
			region.setParent(parent.get());
		} catch (CircularInheritanceException e) {
			EWMessages.REGION_PARENT_SET_CIRCULAR.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(source);
			return false;
		}
		
		// Héritage
		List<ProtectedRegion> parents = null;
		try {
			parents = region.getHeritage();
		} catch (CircularInheritanceException e) {}
			
		if (parents == null || parents.size() == 1) {
			EWMessages.REGION_PARENT_SET.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<parent>", parent.get().getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(source);
		} else {
			List<Text> messages = new ArrayList<Text>();
			Text padding = Text.EMPTY;
			
			for (int cpt=0; cpt < parents.size(); cpt++) {
				padding = padding.concat(EWMessages.REGION_PARENT_SET_HERITAGE_PADDING.getText());
				
				ProtectedRegion curParent = parents.get(cpt);
				messages.add(padding.concat(EWMessages.REGION_PARENT_SET_HERITAGE_LINE.getFormat()
					.toText("<region>", Text.builder(curParent.getIdentifier())
								.onShiftClick(TextActions.insertText(curParent.getIdentifier()))
								.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\" \"" + curParent.getIdentifier() + "\" "))
								.build(),
							"<type>", curParent.getType().getNameFormat(),
							"<priority>", String.valueOf(curParent.getPriority()))));
			}
			
			EWMessages.REGION_PARENT_SET_HERITAGE.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<parent>", parent.get().getIdentifier())
				.replace("<world>", world.getName())
				.replace("<heritage>", Text.joinWith(Text.of("\n"), messages))
				.sendTo(source);
		}		
		return true;
	}
	
	private boolean commandRegionRemoveParent(final CommandSource source, ProtectedRegion region, World world) {
		if (!region.getParent().isPresent()) {
			EWMessages.REGION_PARENT_REMOVE_EMPTY.sender()
				.replace("<region>", region.getIdentifier())
				.replace("<world>", world.getName())
				.sendTo(source);
			return false;
		}
		
		try {
			region.setParent(null);
		} catch (CircularInheritanceException e) {}
		EWMessages.REGION_PARENT_REMOVE.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.sendTo(source);
		return true;
	}
}
