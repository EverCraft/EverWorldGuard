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
package fr.evercraft.everworldguard.command.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.java.UtilsInteger;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everapi.services.selection.exception.NoSelectedRegionException;
import fr.evercraft.everapi.services.selection.exception.RegionOperationException;
import fr.evercraft.everapi.sponge.UtilsDirection;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWSelectShift extends ESubCommand<EverWorldGuard> {

	public EWSelectShift(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "shift");
    }

	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_SHIFT_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_AMOUNT.getString() + "> "
												  + "[" + EAMessages.ARGS_DIRECTION.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			return Arrays.asList("vert", "10", "20", "30");
		} else if (args.size() == 2) {
			List<String> suggests = new ArrayList<String>();
			suggests.add("Me");
			for (Direction direction : Direction.values()) {
				if (direction.isCardinal() || direction.isOrdinal()) {
					suggests.add(direction.name());
				}
			}
			return suggests;
		}
		return Arrays.asList();
	}
	
	@Override
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (!(source instanceof EPlayer)) {
			EAMessages.COMMAND_ERROR_FOR_PLAYER.sender()
				.prefix(EWMessages.PREFIX)
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		}
		EPlayer player = (EPlayer) source;
		
		try {
			if (args.size() == 1) {
				return this.commandSelectShift(player, args.get(0));
			} else if (args.size() == 2) {
				return this.commandSelectShift(player, args.get(0), args.get(1));
			} else {
				source.sendMessage(this.help(source));
			}
		} catch (RegionOperationException e) {
			EWMessages.SELECT_SHIFT_ERROR_OPERATION.sender()
				.replace("{exception}", e.getMessage())
				.sendTo(player);
		} catch (NoSelectedRegionException e) {
			EWMessages.SELECT_SHIFT_ERROR_NO_REGION.sender()
				.replace("{exception}", e.getMessage())
				.sendTo(player);
		}
		return CompletableFuture.completedFuture(false);
	}

	private CompletableFuture<Boolean> commandSelectShift(final EPlayer player, final String amount_string) throws RegionOperationException, NoSelectedRegionException {
		Optional<Integer> amount = UtilsInteger.parseInt(amount_string);
		if (!amount.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{number}", amount_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandSelectShift(player, amount.get(), player.getDirection());
	}
	
	private CompletableFuture<Boolean> commandSelectShift(final EPlayer player, final String amount_string, final String direction_string) 
			throws RegionOperationException, NoSelectedRegionException {
		Optional<Integer> amount = UtilsInteger.parseInt(amount_string);
		if (!amount.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{number}", amount_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<Direction> direction = null;
		if (direction_string.equalsIgnoreCase("M") || direction_string.equalsIgnoreCase("Me")) {
			direction = Optional.of(player.getDirection());
		} else {
			direction = UtilsDirection.of(direction_string);
		}
		
		if (!direction.isPresent()) {
			EAMessages.IS_NOT_DIRECTION.sender()
				.prefix(EWMessages.PREFIX)
				.replace("{direction}", direction_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandSelectShift(player, amount.get(), direction.get());
	}
	
	private CompletableFuture<Boolean> commandSelectShift(final EPlayer player, final int amount, final Direction direction) 
			throws RegionOperationException, NoSelectedRegionException {
		Selector selector = player.getSelector();
		
		selector.shift(direction.asOffset().mul(amount).round().toInt());
		
		EWMessages.SELECT_SHIFT_DIRECTION.sender()
			.replace("{amount}", String.valueOf(amount))
			.replace("{direction}", UtilsDirection.getText(direction))
			.sendTo(player);
		return CompletableFuture.completedFuture(true);
	}
}
