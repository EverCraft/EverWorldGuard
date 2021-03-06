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
package fr.evercraft.everworldguard;

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import fr.evercraft.everapi.EverAPI;
import fr.evercraft.everapi.exception.PluginDisableException;
import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.everapi.services.selection.SelectionService;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everworldguard.command.EWManagerCommands;
import fr.evercraft.everworldguard.listeners.EWListener;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.protection.flag.EWFlagConfig;
import fr.evercraft.everworldguard.protection.flag.EWManagerFlags;
import fr.evercraft.everworldguard.protection.storage.EWDataBases;
import fr.evercraft.everworldguard.selection.ESelectionService;

@Plugin(id = "everworldguard", 
		name = "EverWorldGuard", 
		version = EverAPI.VERSION, 
		description = "WorldGuard",
		url = "http://evercraft.fr/",
		authors = {"rexbut"},
		dependencies = {
		    @Dependency(id = "everapi", version = EverAPI.VERSION),
		    @Dependency(id = "spongeapi", version = EverAPI.SPONGEAPI_VERSION),
		    @Dependency(id = "worldedit", optional = true)
		})
public class EverWorldGuard extends EPlugin<EverWorldGuard> {
	
	private EWConfig configs;
	private EWFlagConfig configFlags;
	private EWMessage messages;
	
	private EProtectionService protection;
	private ESelectionService selection;
	private EWManagerCommands commands;
	private EWDataBases database;
	private EWManagerFlags flags;
	
	@Override
	protected void onPreEnable() throws PluginDisableException {		
		this.configs = new EWConfig(this);
		this.messages = new EWMessage(this);
		this.database = new EWDataBases(this);
		this.commands = new EWManagerCommands(this);
		
		this.protection = new EProtectionService(this);
		this.getGame().getServiceManager().setProvider(this, WorldGuardService.class, this.protection);	
	}
	
	@Override
	protected void onEnable() {
		this.configFlags = new EWFlagConfig(this);
		this.flags = new EWManagerFlags(this);
		
		if (!this.getGame().getServiceManager().isRegistered(SelectionService.class)) {
			this.selection = new ESelectionService(this);
			this.getGame().getServiceManager().setProvider(this, SelectionService.class, this.selection);
		}
		
		this.getGame().getEventManager().registerListeners(this, new EWListener(this));
	}
	
	@Override
	protected void onCompleteEnable() {
		this.protection.getRegister().setInitialized(false);
	}

	protected void onReload() throws PluginDisableException, ServerDisableException {
		super.onReload();
		
		this.database.reload();
		this.protection.reload();
		this.flags.reload();
		
		if (this.selection != null) {
			this.selection.reload();
		}
	}
	
	protected void onDisable() {
	}

	/*
	 * Accesseurs
	 */
	
	public EWMessage getMessages() {
		return this.messages;
	}
	
	public EWConfig getConfigs() {
		return this.configs;
	}
	
	public EWPermissions[] getPermissions() {
		return EWPermissions.values();
	}
	
	public EWFlagConfig getConfigFlags() {
		return this.configFlags;
	}
	
	public EWDataBases getDataBases() {
		return this.database;
	}
	
	public EWManagerCommands getManagerCommands() {
		return this.commands;
	}
	
	public EWManagerFlags getManagerFlags() {
		return this.flags;
	}
	
	public EProtectionService getProtectionService() {
		return this.protection;
	}
	
	public ESelectionService getSelectionService() {
		return this.selection;
	}
}
