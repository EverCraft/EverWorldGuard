package fr.evercraft.everworldguard.command;

import java.util.HashSet;

import fr.evercraft.everapi.plugin.command.*;
import fr.evercraft.everworldguard.EWCommand;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.command.select.*;
import fr.evercraft.everworldguard.command.sub.*;

public class EWManagerCommands extends HashSet<ECommand<EverWorldGuard>> {
	
	private static final long serialVersionUID = -1;

	private final EverWorldGuard plugin;
	
	private final EWCommand command;
	
	public EWManagerCommands(EverWorldGuard plugin){
		super();
		
		this.plugin = plugin;
		
		this.command = new EWCommand(this.plugin);
		this.command.add(new EWReload(this.plugin, this.command));
		
		load();
	}
	
	public void load() {
		EWSelect select = new EWSelect(this.plugin);
		select.add(new EWSelectExpand(this.plugin, select));
		select.add(new EWSelectPos1(this.plugin, select));
		select.add(new EWSelectPos2(this.plugin, select));
		select.add(new EWSelectType(this.plugin, select));
		register(select);
	}
	
	public void reload(){
		for (ECommand<EverWorldGuard> command : this) {
			if (command instanceof EReloadCommand) {
				((EReloadCommand<EverWorldGuard>) command).reload();
			}
		}
	}
	
	private void register(ECommand<EverWorldGuard> command) {
		this.command.add(command);
		this.add(command);
	}
}
