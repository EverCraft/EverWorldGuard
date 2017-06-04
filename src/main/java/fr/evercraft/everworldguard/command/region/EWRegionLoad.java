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
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionLoad extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	
	private final Args.Builder pattern;
	
	public EWRegionLoad(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "load");
        
        this.pattern = Args.builder()
    		.value(MARKER_WORLD, 
					(source, args) -> this.getAllWorlds(),
					(source, args) -> args.getArgs().size() <= 1);
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.REGION_LOAD.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.REGION_LOAD_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [-w " + EAMessages.ARGS_WORLD.getString() + "]")
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
		
		Optional<String> worldString = args.getValue(MARKER_WORLD);
		if (worldString.isPresent()) {
			Optional<World> world = this.plugin.getEServer().getEWorld(worldString.get());
			// Monde introuvable
			if (!world.isPresent()) {
				EAMessages.WORLD_NOT_FOUND.sender()
					.prefix(EWMessages.PREFIX)
					.replace("<world>", worldString.get())
					.sendTo(source);
				return false;
			}
			
			return this.commandRegionLoad(source, world.get());
		}
		
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return false;
		}
		return this.commandRegionLoad(source, ((EPlayer) source).getWorld());
	}

	private boolean commandRegionLoad(CommandSource source, World world) {
		this.plugin.getProtectionService().getOrCreateEWorld(world).reload();
		EWMessages.REGION_LOAD_MESSAGE.sender()
			.replace("<world>", world.getName())
			.sendTo(source);
		return true;
	}
}
