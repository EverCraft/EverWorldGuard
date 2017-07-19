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
import java.util.concurrent.CompletableFuture;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

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
		return EWMessages.MIGRATE_DESCRIPTION.getText();
	}

	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " ").onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.append(Text.of(" [confirmation]"))
				.color(TextColors.RED).build();
	}
	
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return Arrays.asList();
	}
	
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 0) {
			EWMessages.MIGRATE_CONF_CONFIRMATION.sender()
				.replace("<confirmation>", () -> this.getButtonConfirmation())
				.sendTo(source);
			return CompletableFuture.completedFuture(false);
		} else if (args.size() == 1 && args.get(1).equalsIgnoreCase("confirmation")) {
			return CompletableFuture.supplyAsync(() -> {
				if (!this.commandClear(source)) {
					EAMessages.COMMAND_ERROR.sendTo(source);
					return false;
				}
				
				this.plugin.getELogger().info(EWMessages.MIGRATE_SQL_LOG.getString());
				EWMessages.MIGRATE_SQL.sendTo(source);
				return true;
			}, this.plugin.getThreadAsync());
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}

	public Text getButtonConfirmation(){
		return EWMessages.MIGRATE_SQL_CONFIRMATION_VALID.getText().toBuilder()
					.onHover(TextActions.showText(EWMessages.MIGRATE_SQL_CONFIRMATION_VALID_HOVER.getText()))
					.onClick(TextActions.runCommand("/" + this.getName() + " sql confirmation"))
					.build();
	}
	
	private boolean commandClear(final CommandSource source) {
		for (EWWorld world : this.plugin.getProtectionService().getAllEWorld()) {
			if (!world.getStorage().isSql()) {
				this.plugin.getELogger().warn("Error: The data isn't SQL (world='" + world.getUniqueId() + "')");
				return false;
			} else {
				
			}
    	}
		return true;
	}
	
}
