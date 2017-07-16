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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EWCommand;
import fr.evercraft.everworldguard.EWMessage.EWMessages;
import fr.evercraft.everworldguard.EWPermissions;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.storage.RegionStorageConf;

public class EWTransfert extends ESubCommand<EverWorldGuard > {
	
	public EWTransfert(final EverWorldGuard plugin, final EWCommand command) {
        super(plugin, command, "transfer");
    }

	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EWPermissions.TRANSFERT.get());
	}

	public Text description(final CommandSource source) {
		return EWMessages.TRANSFERT_DESCRIPTION.getText();
	}

	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " ").onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.append(Text.of("<"))
				.append(Text.builder("sql").onClick(TextActions.suggestCommand("/" + this.getName() + " sql")).build())
				.append(Text.of("|"))
				.append(Text.builder("conf").onClick(TextActions.suggestCommand("/" + this.getName() + " conf")).build())
				.append(Text.of("> [confirmation]"))
				.color(TextColors.RED).build();
	}
	
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1){
			return Arrays.asList("sql", "conf");
		}
		return Arrays.asList();
	}
	
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			//Si la base de donnée est activé
			if (this.plugin.getDataBases().isEnable()) {
				// Transféré vers une base de donnée SQL
				if (args.get(0).equalsIgnoreCase("sql")) {
					EWMessages.TRANSFERT_SQL_CONFIRMATION.sender()
						.replace("<confirmation>", () -> this.getButtonConfirmationSQL())
						.sendTo(source);
				// Transféré vers un fichier de config
				} else if (args.get(0).equalsIgnoreCase("conf")) {
					EWMessages.TRANSFERT_CONF_CONFIRMATION.sender()
						.replace("<confirmation>", () -> this.getButtonConfirmationConf())
						.sendTo(source);
				// Erreur : sql ou conf
				} else {
					source.sendMessage(this.help(source));
				}
			// Error : SQL disable
			} else {
				EWMessages.TRANSFERT_DISABLE.sendTo(source);
			}
		} else if (args.size() == 2 && args.get(1).equalsIgnoreCase("confirmation")) {
			//Si la base de donnée est activé
			if (this.plugin.getDataBases().isEnable()) {
				// Transféré vers une base de donnée SQL
				if (args.get(0).equalsIgnoreCase("sql")) {
					return CompletableFuture.supplyAsync(() -> {
						if (!this.commandSQL(source)) {
							EAMessages.COMMAND_ERROR.sendTo(source);
							return false;
						}
						
						this.plugin.getELogger().info(EWMessages.TRANSFERT_SQL_LOG.getString());
						EWMessages.TRANSFERT_SQL.sendTo(source);
						return true;
					}, this.plugin.getThreadAsync());
				// Transféré vers un fichier de config
				} else if (args.get(0).equalsIgnoreCase("conf")) {
					return CompletableFuture.supplyAsync(() -> {
						if (!this.commandConf(source)) {
							EAMessages.COMMAND_ERROR.sendTo(source);
							return false;
						}
						
						this.plugin.getELogger().info(EWMessages.TRANSFERT_CONF_LOG.getString());
						EWMessages.TRANSFERT_CONF.sendTo(source);
						return true;
					}, this.plugin.getThreadAsync());
				// Erreur : sql ou conf
				} else {
					source.sendMessage(this.help(source));
				}
			// Error : SQL disable
			} else {
				EWMessages.TRANSFERT_DISABLE.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}

	public Text getButtonConfirmationSQL(){
		return EWMessages.TRANSFERT_SQL_CONFIRMATION_VALID.getText().toBuilder()
					.onHover(TextActions.showText(EWMessages.TRANSFERT_SQL_CONFIRMATION_VALID_HOVER.getText()))
					.onClick(TextActions.runCommand("/" + this.getName() + " sql confirmation"))
					.build();
	}
	
	public Text getButtonConfirmationConf(){
		return EWMessages.TRANSFERT_CONF_CONFIRMATION_VALID.getText().toBuilder()
					.onHover(TextActions.showText(EWMessages.TRANSFERT_CONF_CONFIRMATION_VALID_HOVER.getText()))
					.onClick(TextActions.runCommand("/" + this.getName() + " conf confirmation"))
					.build();
	}
	
	/**
	 * Transféré les données des joueurs dans une base de donnée
	 * @param player Le joueur
	 * @return True si cela a correctement fonctionné
	 */
	@SuppressWarnings("unchecked")
	private <V> boolean commandSQL(final CommandSource player) {
		Connection connection = null;
		try {
    		connection = this.plugin.getDataBases().getConnection();
    		
    		if (!this.plugin.getDataBases().clear(connection)) {
    			this.plugin.getELogger().warn("Error during the cleaning of the database");
    			return false;
    		}
    		
			for (EWWorld world : this.plugin.getProtectionService().getAllEWorld()) {
    			File file = this.plugin.getPath().resolve(RegionStorageConf.MKDIR + "/" + world.getWorld().getName() + ".conf").toFile();
    			// Si le fichier existe
    			if (!file.exists()) {
    				continue;
    			}
    				
				for (EProtectedRegion region : (new RegionStorageConf(this.plugin, world)).getAll().get()) {
					UUID parent = region.getParent().isPresent() ? region.getParent().get().getId() : null;
					
					if(!this.plugin.getDataBases().insertRegion(connection, world.getUniqueId(), region.getId(), region.getName(), region.getType(), region.getPriority(), parent)) {
						return false;
					}
					
					if(!this.plugin.getDataBases().insertPositions(connection, world.getUniqueId(), region.getId(), region.getPoints())) {
						return false;
					}
					
					if(!this.plugin.getDataBases().insertUser(connection, world.getUniqueId(), region.getId(), Groups.OWNER, region.getOwners().getPlayers())) {
						return false;
					}
					
					if(!this.plugin.getDataBases().insertUser(connection, world.getUniqueId(), region.getId(), Groups.MEMBER, region.getMembers().getPlayers())) {
						return false;
					}
					
					if(!this.plugin.getDataBases().insertGroup(connection, world.getUniqueId(), region.getId(), Groups.OWNER, region.getOwners().getGroups())) {
						return false;
					}
					
					if(!this.plugin.getDataBases().insertGroup(connection, world.getUniqueId(), region.getId(), Groups.MEMBER, region.getMembers().getGroups())) {
						return false;
					}
					
					for (Entry<Flag<?>, FlagValue<?>> flag : region.getFlags().entrySet()) {
						for (Entry<Group, ?> value : flag.getValue().getAll().entrySet()) {
							if(!this.plugin.getDataBases().insertFlag(connection, world.getUniqueId(), region.getId(), (Flag<V>) flag.getKey(), value.getKey(), (V) value.getValue())) {
				    			return false;
				    		}
						}
					}
				}
    		}
			return true;
		} catch (ServerDisableException e) {
			e.execute();
		} catch (InterruptedException | ExecutionException e) {
			this.plugin.getELogger().warn("Error during the recovery of regions");
		} finally {
			try {if (connection != null) connection.close();} catch (SQLException e) {}
	    }
		return false;
	}

	private boolean commandConf(final CommandSource source) {
		for (EWWorld world : this.plugin.getProtectionService().getAllEWorld()) {
			try {
				if (!world.getStorage().isSql()) {
					this.plugin.getELogger().warn("Error: The data isn't SQL (world='" + world.getUniqueId() + "')");
					return false;
				}
					
				RegionStorageConf config = new RegionStorageConf(this.plugin, world);
				if (!config.clear()) {
					this.plugin.getELogger().warn("Error during the cleaning of the file of config (world='" + world.getUniqueId() + "')");
					return false;
				}
				
				if (!config.addAll(world.getStorage().getAll().get())) {
					this.plugin.getELogger().warn("Error during the addition of regions in the file of config (world='" + world.getUniqueId() + "')");
					return false;
				}
			
			} catch (InterruptedException | ExecutionException e) {
				this.plugin.getELogger().warn("Error during the recovery of regions (world='" + world.getUniqueId() + "')");
				return false;
			}
    	}
		return true;
	}
	
}
