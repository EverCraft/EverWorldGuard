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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.RemoveType;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionRemove extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_FORCE = "-f";
	public static final String MARKER_UNSET_PARENT_IN_CHILDREN = "-u";
	
	private final Args.Builder pattern;
	
	public EWRegionRemove(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "remove");
        
        this.pattern = Args.builder()
        	.empty(MARKER_FORCE)
        	.empty(MARKER_UNSET_PARENT_IN_CHILDREN)
			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
			.arg((source, args) -> {
				Optional<World> world = EWRegion.getWorld(this.plugin, source, args, MARKER_WORLD);
				if (!world.isPresent()) {
					return Arrays.asList();
				}
				
				return this.plugin.getService().getOrCreateWorld(world.get()).getAll().stream()
							.map(region -> region.getIdentifier())
							.collect(Collectors.toSet());
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
		return Text.builder("/" + this.getName() + " [" + MARKER_WORLD + " " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " [" + MARKER_FORCE + "|" + MARKER_UNSET_PARENT_IN_CHILDREN + "]"
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
		
		Optional<ProtectedRegion> region = this.plugin.getService().getOrCreateWorld(world).getRegion(args_string.get(0));
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", args_string.get(0))
				.sendTo(source);
			return false;
		}
		
		if (region.get().getType().equals(ProtectedRegion.Type.GLOBAL)) {
			EWMessages.REGION_REMOVE_ERROR_GLOBAL.sender()
				.replace("<region>", region.get().getIdentifier())
				.replace("<type>", region.get().getType().getNameFormat())
				.replace("<world>", world.getName())
				.sendTo(source);
			return false;
		}
		
		if (args.isOption(MARKER_FORCE) && args.isOption(MARKER_UNSET_PARENT_IN_CHILDREN)) {
			source.sendMessage(this.help(source));
			return false;
		} else if (args.isOption(MARKER_FORCE)) {
			return this.commandRegionRemoveForce(source, region.get(), world);
		} else if (args.isOption(MARKER_UNSET_PARENT_IN_CHILDREN)) {
			return this.commandRegionRemoveUnset(source, region.get(), world);
		} else {
			return this.commandRegionRemove(source, region.get(), world);
		}
	}
	
	private boolean commandRegionRemove(final CommandSource player, final ProtectedRegion region, final World world) {
		for (ProtectedRegion others : this.plugin.getService().getOrCreateEWorld(world).getAll()) {
			Optional<ProtectedRegion> parent = others.getParent();
			if (parent.isPresent() && parent.get().equals(region)) {
				EWMessages.REGION_REMOVE_ERROR_CHILDREN.sender()
					.replace("<region>", region.getIdentifier())
					.replace("<children>", others.getIdentifier())
					.replace("<world>", world.getName())
					.sendTo(player);
				return false;
			}
		}
		
		this.plugin.getService().getOrCreateWorld(world).removeRegion(region.getIdentifier(), RemoveType.UNSET_PARENT_IN_CHILDREN);
		EWMessages.REGION_REMOVE_REGION.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.sendTo(player);
		return false;
	}
	
	private boolean commandRegionRemoveForce(final CommandSource player, final ProtectedRegion region, final World world) {
		this.plugin.getService().getOrCreateWorld(world).removeRegion(region.getIdentifier(), RemoveType.REMOVE_CHILDREN);
		
		EWMessages.REGION_REMOVE_CHILDREN_REMOVE.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.sendTo(player);
		return false;
	}
	
	private boolean commandRegionRemoveUnset(final CommandSource player, final ProtectedRegion region, final World world) {
		this.plugin.getService().getOrCreateWorld(world).removeRegion(region.getIdentifier(), RemoveType.UNSET_PARENT_IN_CHILDREN);
		
		EWMessages.REGION_REMOVE_CHILDREN_UNSET.sender()
			.replace("<region>", region.getIdentifier())
			.replace("<world>", world.getName())
			.sendTo(player);
		return false;
	}
}