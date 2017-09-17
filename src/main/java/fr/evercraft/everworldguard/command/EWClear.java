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
package fr.evercraft.everworldguard.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everworldguard.EWCommand;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;

public class EWClear extends ESubCommand<EverWorldGuard > {
	
	public EWClear(final EverWorldGuard plugin, final EWCommand command) {
        super(plugin, command, "clear");
    }

	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.CLEAR.get());
	}

	public Text description(final CommandSource source) {
		return EWMessages.CLEAR_DESCRIPTION.getText();
	}

	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " [" + EAMessages.ARGS_WORLD.getString() + "]")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.append(Text.of(" [-confirmation]"))
				.color(TextColors.RED).build();
	}
	
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			return this.getAllWorlds();
		}
		return Arrays.asList();
	}
	
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 0) {
			EWMessages.CLEAR_ALL_CONFIRMATION.sender()
				.replace("{confirmation}", () -> this.getButtonConfirmationAll())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		} else if (args.size() == 1) {
			if (args.get(0).equalsIgnoreCase("-confirmation")) {
				return this.commandClearAll(source);
			} else {
				Optional<World> world = this.plugin.getEServer().getEWorld(args.get(0));
				if (world.isPresent()) {
					EWMessages.CLEAR_WORLD_CONFIRMATION.sender()
						.replace("{world}", world.get().getName())
						.replace("{confirmation}", () -> this.getButtonConfirmationWorld(world.get()))
						.sendTo(source);
					return CompletableFuture.completedFuture(false);
				} else {
					EAMessages.WORLD_NOT_FOUND.sender()
						.replace("{world}", args.get(0))
						.sendTo(source);
				}
			}
		} else if (args.size() == 2) {
			Optional<World> world = this.plugin.getEServer().getEWorld(args.get(0));
			if (world.isPresent()) {
				if (args.get(1).equalsIgnoreCase("-confirmation")) {
					return this.commandClearWorld(source, world.get());
				} else {
					source.sendMessage(this.help(source));
				}
			} else {
				EAMessages.WORLD_NOT_FOUND.sender()
					.replace("{world}", args.get(0))
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}

	public Text getButtonConfirmationWorld(World world){
		return EWMessages.CLEAR_WORLD_CONFIRMATION_VALID.getFormat()
					.toText("{world}", world.getName()).toBuilder()
					.onHover(TextActions.showText(EWMessages.CLEAR_WORLD_CONFIRMATION_VALID_HOVER.getFormat()
						.toText("{world}", world.getName())))
					.onClick(TextActions.runCommand("/" + this.getName() + " \"" + world.getUniqueId() + "\" -confirmation"))
					.build();
	}
	
	public Text getButtonConfirmationAll(){
		return EWMessages.CLEAR_ALL_CONFIRMATION_VALID.getText().toBuilder()
					.onHover(TextActions.showText(EWMessages.CLEAR_ALL_CONFIRMATION_VALID_HOVER.getText()))
					.onClick(TextActions.runCommand("/" + this.getName() + " -confirmation"))
					.build();
	}
	
	private CompletableFuture<Boolean> commandClearWorld(final CommandSource source, final World world) {
		return CompletableFuture.supplyAsync(() -> {
			if (!this.commandClearWorldAsync(source, world)) {
				EAMessages.COMMAND_ERROR.sendTo(source);
				return false;
			}
			
			EWMessages.CLEAR_WORLD_PLAYER.sender()
				.replace("{world}", world.getName())
				.sendTo(source);
			this.plugin.getELogger().info(EWMessages.CLEAR_WORLD_LOG.getFormat().toString(
					"{player}", source.getName(),
					"{world}", world.getName()));
			return true;
		}, this.plugin.getThreadAsync());
	}
	
	private boolean commandClearWorldAsync(final CommandSource source, final World worldSponge) {
		try {
			EWWorld world = this.plugin.getProtectionService().getOrCreateEWorld(worldSponge);
			if (world.getStorage().clearAll().get()) {
				world.reload();
				return true;
			}
		} catch (InterruptedException | ExecutionException e) {
			this.plugin.getELogger().warn("Error during the cleaning the world (world='" + worldSponge.getUniqueId() + "')");
		}
		return false;
	}
	
	private CompletableFuture<Boolean> commandClearAll(final CommandSource source) {
		return CompletableFuture.supplyAsync(() -> {
			if (!this.commandClearAllAsync(source)) {
				EAMessages.COMMAND_ERROR.sendTo(source);
				return false;
			}
			
			EWMessages.CLEAR_ALL_PLAYER.sendTo(source);
			this.plugin.getELogger().info(EWMessages.CLEAR_ALL_LOG.getFormat().toString("{player}", source.getName()));
			return true;
		}, this.plugin.getThreadAsync());
	}
	
	private boolean commandClearAllAsync(final CommandSource source) {
		for (EWWorld world : this.plugin.getProtectionService().getAllEWorld()) {
			try {
				world.getStorage().clearAll().get();
			} catch (InterruptedException | ExecutionException e) {
				this.plugin.getELogger().warn("Error during the cleaning the world (world='" + world.getUniqueId() + "')");
				return false;
			}
    	}
		this.plugin.reload();
		return true;
	}
	
}
