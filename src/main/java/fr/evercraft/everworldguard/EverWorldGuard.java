package fr.evercraft.everworldguard;

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.everworldguard.command.sub.EWReload;
import fr.evercraft.everworldguard.service.EWorldGuardService;

@Plugin(id = "fr.evercraft.everworldguard", 
		name = "EverWorldGuard", 
		version = "1.2", 
		description = "WorldGuard",
		url = "http://evercraft.fr/",
		authors = {"rexbut"},
		dependencies = {
		    @Dependency(id = "fr.evercraft.everapi", version = "1.2")
		})
public class EverWorldGuard extends EPlugin {
	private EWConfig configs;
	private EWMessage messages;
	
	private EWorldGuardService service;
	
	@Override
	protected void onPreEnable() {		
		this.configs = new EWConfig(this);
		this.messages = new EWMessage(this);
		
		this.service = new EWorldGuardService(this);
		//this.getGame().getServiceManager().setProvider(this, WorldGuardService.class, this.service);
		
		this.getGame().getEventManager().registerListeners(this, new EWListener(this));
	}
	
	@Override
	protected void onCompleteEnable() {
		EWCommand command = new EWCommand(this);
		
		command.add(new EWReload(this, command));
	}

	protected void onReload(){
		this.reloadConfigurations();
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
}
