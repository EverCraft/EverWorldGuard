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
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.regions.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.SetProtectedRegion;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionInfo extends ESubCommand<EverWorldGuard> {
	
	private final String MARKER_WORLD = "-w";
	private final Args.Builder pattern;
	
	public EWRegionInfo(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "info");
        
        this.pattern = Args.builder()
			.value(MARKER_WORLD, (source, args) -> command.getAllWorlds())
			.arg((source, args) -> {
				List<String> suggests = new ArrayList<String>();
				Optional<String> optWorld = args.getValue(MARKER_WORLD);
				
				if (optWorld.isPresent()) {
					this.plugin.getEServer().getWorld(optWorld.get()).ifPresent(world -> 
						this.plugin.getService().getRegion(world).forEach(region ->
							suggests.add(region.getIdentifier())
					));
				} else if (source instanceof Player) {
					this.plugin.getService().getRegion(((Player) source).getWorld()).forEach(region ->
						suggests.add(region.getIdentifier())
					);
				}
				
				return suggests;
			});
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_INFO_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [-w " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " [" + EAMessages.ARGS_REGION.getString() + "]")
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
		boolean resultat = false;
		Args args = this.pattern.build(args_list);
		
		if (args.getArgs().size() == 0) {
			if (source instanceof EPlayer) {
				EPlayer player = (EPlayer) source;
				resultat = this.commandRegionInfo(source, player.getRegions(), player.getWorld());
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else if (args.getArgs().size() == 1) {
			Optional<String> world = args.getValue(MARKER_WORLD);
			if (world.isPresent()) {
				resultat = this.commandRegionInfo(source, args.getArgs().get(0), world.get());
			} else {
				if (source instanceof EPlayer) {
					resultat = this.commandRegionInfo(source, args.getArgs().get(0), ((EPlayer) source).getWorld());
				} else {
					EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
						.prefix(EWMessages.PREFIX)
						.sendTo(source);
				}
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}
	
	private boolean commandRegionInfo(final CommandSource player, final SetProtectedRegion regions, final World world) {
		if (regions.getAll().isEmpty()) {
			EAMessages.COMMAND_ERROR.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(player);
			return false;
		}
		
		if (regions.getAll().size() == 1) {
			return this.commandRegionInfo(player, regions.getAll().iterator().next(), world);
		} else {
			return this.commandRegionInfo(player, regions.getAll(), world);
		}
	}	
	
	private boolean commandRegionInfo(final CommandSource player, final String region_string, final String world_string) {
		Optional<World> world = this.plugin.getEServer().getEWorld(world_string);
		// Monde introuvable
		if (!world.isPresent()) {
			EAMessages.WORLD_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<world>", world_string)
				.sendTo(player);
			return false;
		}
		
		return this.commandRegionInfo(player, region_string, world.get());
	}
	
	private boolean commandRegionInfo(final CommandSource player, final String region_string, final World world) {
		Optional<ProtectedRegion> region = this.plugin.getService().getOrCreate(world).getRegion(region_string);
		// Region introuvable
		if (!region.isPresent()) {
			EAMessages.REGION_NOT_FOUND.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<region>", region_string)
				.sendTo(player);
			return false;
		}
		
		return this.commandRegionInfo(player, region.get(), world);
	}
	
	private boolean commandRegionInfo(final CommandSource player, final Set<ProtectedRegion> regions, final World world) {
		return true;
	}

	private boolean commandRegionInfo(final CommandSource player, final ProtectedRegion region, final World world) {
		List<Text> list = new ArrayList<Text>();
		
		list.add(EWMessages.REGION_INFO_ONE_WORLD.getFormat()
				.toText("<world>", world.getName()));
		
		list.add(EWMessages.REGION_INFO_ONE_TYPE.getFormat()
				.toText("<type>", region.getType().getNameFormat()));
		
		list.add(EWMessages.REGION_INFO_ONE_PRIORITY.getFormat()
				.toText("<prority>", Text.builder(String.valueOf(region.getPriority()))
					.onClick(TextActions.suggestCommand(
						"/" + this.getParentName() + " setpriority -w \"" + world.getName() + "\" \"" + region.getIdentifier() + "\" " + region.getPriority()))
					.build()));
		Optional<ProtectedRegion> parent = region.getParent();
		if (parent.isPresent()) {
			list.add(EWMessages.REGION_INFO_ONE_PARENT.getFormat()
					.toText("<parent>", Text.builder(String.valueOf(region.getPriority()))
						.onClick(TextActions.suggestCommand(
							"/" + this.getParentName() + " setparent -w \"" + world.getName() + "\" \"" + region.getIdentifier() + "\" \"" + parent.get().getIdentifier() + "\""))
						.build()));
		}
		
		try {
			List<ProtectedRegion> parents = region.getHeritage();
			if (parents.size() > 1) {
				list.add(EWMessages.REGION_INFO_ONE_PARENT.getFormat()
						.toText("<parent>", Text.builder(String.valueOf(region.getPriority()))
							.onClick(TextActions.suggestCommand(
								"/" + this.getParentName() + " setparent -w \"" + world.getName() + "\" \"" + region.getIdentifier() + "\" \"" + parent.get().getIdentifier() + "\""))
							.build()));
			}
		} catch (CircularInheritanceException e) {}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EWMessages.REGION_INFO_ONE_TITLE.getFormat()
					.toText("<region>", region.getIdentifier())
					.toBuilder()
					.onClick(TextActions.runCommand("/" + this.getName() + " -w \"" + world.getName() + "\" \"" + region.getIdentifier() + "\" "))
					.build(), 
				list, player);
		
		return true;
	}
}
