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

import com.flowpowered.math.vector.Vector3i;

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

public class EWSelectExpand extends ESubCommand<EverWorldGuard> {
	private static final List<String> VERT = Arrays.asList("vert", "verticale");
	
	public EWSelectExpand(final EverWorldGuard plugin, final EWSelect command) {
        super(plugin, command, "expand");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return true;
	}

	@Override
	public Text description(final CommandSource source) {
		return EWMessages.SELECT_EXPAND_DESCRIPTION.getText();
	}

	@Override
	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <vert|<" + EAMessages.ARGS_AMOUNT.getString() + "> "
												  + "[" + EAMessages.ARGS_DIRECTION.getString() + "] "
												  + "[" + EAMessages.ARGS_REVERSE_AMOUNT.getString() + "]>")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	@Override
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			return Arrays.asList("vert", "10", "20", "30");
		} else if (args.size() == 2) {
			if (!VERT.contains(args.get(1).toLowerCase())) {
				List<String> suggests = new ArrayList<String>();
				suggests.add("Me");
				for (Direction direction : Direction.values()) {
					if (direction.isCardinal() || direction.isOrdinal()) {
						suggests.add(direction.name());
					}
				}
				return suggests;
			}
		} else if (args.size() == 3) {
			if (!(args.get(1).equalsIgnoreCase("vert") || args.get(1).equalsIgnoreCase("verticale"))) {
				return Arrays.asList("10", "20", "30");
			}
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
				if (VERT.contains(args.get(0).toLowerCase())) {
					return this.commandSelectExpandVert(player);
				} else {
					return this.commandSelectExpand(player, args.get(0));
				}
			} else if (args.size() == 2) {
				return this.commandSelectExpand(player, args.get(0), args.get(1));
			} else if (args.size() == 3) {
				return this.commandSelectExpand(player, args.get(0), args.get(1), args.get(2));
			} else {
				source.sendMessage(this.help(source));
			}
		} catch (RegionOperationException e) {
			EWMessages.SELECT_EXPAND_ERROR_OPERATION.sender()
				.replace("<exception>", e.getMessage())
				.sendTo(player);
		} catch (NoSelectedRegionException e) {
			EWMessages.SELECT_EXPAND_ERROR_NO_REGION.sender()
				.replace("<exception>", e.getMessage())
				.sendTo(player);
		}
		return CompletableFuture.completedFuture(false);
	}
	
	private CompletableFuture<Boolean> commandSelectExpandVert(final EPlayer player) throws RegionOperationException, NoSelectedRegionException {
		Selector selector = player.getSelector();
		
		int oldArea = selector.getVolume();
		int max = player.getWorld().getBlockMax().getY() + 1;			
		selector.expand(Vector3i.from(0, max, 0), Vector3i.from(0, -max, 0));
		int newArea = selector.getVolume();
		
		EWMessages.SELECT_EXPAND_VERT.sender()
			.replace("<size>", String.valueOf(newArea-oldArea))
			.sendTo(player);
		return CompletableFuture.completedFuture(true);

	}

	private CompletableFuture<Boolean> commandSelectExpand(final EPlayer player, final String amount_string) throws RegionOperationException, NoSelectedRegionException {
		Optional<Integer> amount = UtilsInteger.parseInt(amount_string);
		if (!amount.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<number>", amount_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandSelectExpand(player, amount.get(), player.getDirection());
	}
	
	private CompletableFuture<Boolean> commandSelectExpand(final EPlayer player, final String amount_string, final String direction_string) 
			throws RegionOperationException, NoSelectedRegionException {
		Optional<Integer> amount = UtilsInteger.parseInt(amount_string);
		if (!amount.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<number>", amount_string)
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
				.replace("<direction>", direction_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandSelectExpand(player, amount.get(), direction.get());
	}
	
	private CompletableFuture<Boolean> commandSelectExpand(final EPlayer player, final int amount, final Direction direction) 
			throws RegionOperationException, NoSelectedRegionException {
		Selector selector = player.getSelector();
		
		int oldArea = selector.getVolume();		
		selector.expand(direction.asOffset().mul(amount).round().toInt());
		int newArea = selector.getVolume();
		
		EWMessages.SELECT_EXPAND_DIRECTION.sender()
			.replace("<size>", String.valueOf(newArea-oldArea))
			.replace("<amount>", String.valueOf(amount))
			.replace("<direction>", UtilsDirection.getText(direction))
			.sendTo(player);
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandSelectExpand(final EPlayer player, final String amount_string, 
			final String direction_string, final String amountOpposite_string) 
					throws RegionOperationException, NoSelectedRegionException {
		Optional<Integer> amount = UtilsInteger.parseInt(amount_string);
		if (!amount.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<number>", amount_string)
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
				.replace("<direction>", direction_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		Optional<Integer> amountOpposite = UtilsInteger.parseInt(amountOpposite_string);
		if (!amountOpposite.isPresent()) {
			EAMessages.IS_NOT_NUMBER.sender()
				.prefix(EWMessages.PREFIX)
				.replace("<number>", amountOpposite_string)
				.sendTo(player);
			return CompletableFuture.completedFuture(false);
		}
		
		return this.commandSelectExpand(player, amount.get(), direction.get(), amountOpposite.get());
	}
		
	private CompletableFuture<Boolean> commandSelectExpand(final EPlayer player, final int amount, final Direction direction, final int amountOpposite) 
			throws RegionOperationException, NoSelectedRegionException {
		
		Direction directionOpposite = direction.getOpposite();
		Selector selector = player.getSelector();
		
		int oldArea = selector.getVolume();		
		selector.expand(
				direction.asOffset().mul(amount).round().toInt(),
				direction.getOpposite().asOffset().mul(amountOpposite).round().toInt());
		int newArea = selector.getVolume();
		
		EWMessages.SELECT_EXPAND_DIRECTION_OPPOSITE.sender()
			.replace("<size>", String.valueOf(newArea-oldArea))
			.replace("<amount>", String.valueOf(amount))
			.replace("<direction>", UtilsDirection.getText(direction))
			.replace("<amount_opposite>", String.valueOf(amountOpposite))
			.replace("<direction_opposite>", UtilsDirection.getText(directionOpposite))
			.sendTo(player);
		return CompletableFuture.completedFuture(true);
	}
}
