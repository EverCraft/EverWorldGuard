/*
 * This file is part of EverSanctions.
 *
 * EverSanctions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverSanctions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverSanctions.  If not, see <http://www.gnu.org/licenses/>.
 */
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
