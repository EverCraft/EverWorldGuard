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
import java.util.stream.Collectors;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.java.UtilsInteger;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionPriority extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	
	private final Args.Builder pattern;
	
	public EWRegionPriority(final EverWorldGuard plugin, final EWRegion command) {
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
			.args((source, args) -> Arrays.asList("0", "1", "2", "3"));
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
		return source.hasPermission(EWPermissions.REGION_PRIORITY.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_PRIORITY_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " <" + EAMessages.ARGS_REGION.getString() + ">"
												 + " <" + EAMessages.ARGS_PRIORITY.getString() + ">")
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
		
		Optional<Integer> priority = UtilsInteger.parseInt(args_string.get(1));
		if (!priority.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<number>", args_string.get(1))
				.sendTo(source);
			return false;
		}
		
		return this.commandRegionSetPriority(source, region.get(), priority.get(), optWorld.get());
	}

	private boolean commandRegionSetPriority(final CommandSource source, ProtectedRegion region, Integer priority, World world) {
		region.setPriority(priority);
		
		EWMessages.REGION_PRIORITY_SET.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<priority>", priority.toString())
			.replace("<world>", world.getName())
			.sendTo(source);		
		return true;
	}
}
