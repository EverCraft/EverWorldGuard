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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.Args;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWRegionList extends ESubCommand<EverWorldGuard> {
	
	public static final String MARKER_WORLD = "-w";
	public static final String MARKER_PLAYER = "-p";
	public static final String MARKER_GROUP = "-g";
	
	private final Args.Builder pattern;
	
	public EWRegionList(final EverWorldGuard plugin, final EWRegion command) {
        super(plugin, command, "list");
        
        this.pattern = Args.builder()
    			.value(MARKER_WORLD, (source, args) -> this.getAllWorlds())
    			.value(MARKER_PLAYER, (source, args) -> this.getAllPlayers())
    			.value(MARKER_GROUP, (source, args) ->  {
    				List<String> suggests = new ArrayList<String>();
    				Optional<String> optWorld = args.getValue(MARKER_WORLD);
    				
    				if (optWorld.isPresent()) {
    					this.plugin.getEServer().getWorld(optWorld.get()).ifPresent(world -> 
    						suggests.addAll(this.getAllGroups(world)));
    				} else if (source instanceof Player) {
    					suggests.addAll(this.getAllGroups(((Player) source).getWorld()));
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
		return EWMessages.REGION_LIST_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [-w " + EAMessages.ARGS_WORLD.getString() + "]"
												 + " [-p" + EAMessages.ARGS_PLAYER.getString() + " | -g" + EAMessages.ARGS_GROUP.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return this.pattern.suggest(source, args);
	}
	
	@Override
	public boolean subExecute(final CommandSource source, final List<String> args) throws CommandException {
		// Résultat de la commande :
		boolean resultat = false;
		
		if (args.size() == 0) {
			if (source instanceof EPlayer) {
				resultat = this.commandSelectClear((EPlayer) source);
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
					.prefix(EWMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		
		return resultat;
	}

	private boolean commandSelectClear(final EPlayer player) {
		player.setSelectPos1(null);
		player.setSelectPos2(null);
		player.clearSelectPoints();
		
		EWMessages.SELECT_CLEAR_PLAYER.sendTo(player);
		return true;
	}
}
