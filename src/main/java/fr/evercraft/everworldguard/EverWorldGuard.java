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
import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everworldguard.command.EWManagerCommands;
import fr.evercraft.everworldguard.listeners.EWListener;
import fr.evercraft.everworldguard.service.EWorldGuardService;
import fr.evercraft.everworldguard.service.storage.EWDataBases;

@Plugin(id = "everworldguard", 
		name = "EverWorldGuard", 
		version = EverAPI.VERSION, 
		description = "WorldGuard",
		url = "http://evercraft.fr/",
		authors = {"rexbut"},
		dependencies = {
		    @Dependency(id = "everapi", version = EverAPI.VERSION),
		    @Dependency(id = "spongeapi", version = EverAPI.SPONGEAPI_VERSION)
		})
public class EverWorldGuard extends EPlugin<EverWorldGuard> {
	private EWConfig configs;
	private EWMessage messages;
	
	private EWorldGuardService service;
	private EWManagerCommands commands;
	private EWDataBases database;
	
	@Override
	protected void onPreEnable() throws PluginDisableException {		
		this.configs = new EWConfig(this);
		this.messages = new EWMessage(this);
		
		this.database = new EWDataBases(this);
		
		this.service = new EWorldGuardService(this);
		this.getGame().getServiceManager().setProvider(this, WorldGuardService.class, this.service);
		
		this.getGame().getEventManager().registerListeners(this, new EWListener(this));
	}
	
	@Override
	protected void onCompleteEnable() {
		this.commands = new EWManagerCommands(this);
	}

	protected void onReload() throws PluginDisableException{
		this.reloadConfigurations();
		this.database.reload();
		this.service.reload();
	}
	
	protected void onDisable() {
	}

	/*
	 * Accesseurs
	 */
	
	public EWMessage getMessages(){
		return this.messages;
	}
	
	public EWConfig getConfigs() {
		return this.configs;
	}
	
	public EWorldGuardService getService() {
		return this.service;
	}
	
	public EWDataBases getDataBase() {
		return this.database;
	}
	
	public EWManagerCommands getManagerCommands() {
		return this.commands;
	}
}
