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

import java.util.HashSet;

import fr.evercraft.everapi.plugin.command.ECommand;
import fr.evercraft.everapi.plugin.command.ReloadCommand;
import fr.evercraft.everworldguard.EWCommand;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.command.region.*;
import fr.evercraft.everworldguard.command.select.*;

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
		EWRegion region = new EWRegion(this.plugin);
		region.add(new EWRegionBypass(this.plugin, region));
		region.add(new EWRegionCheck(this.plugin, region));
		region.add(new EWRegionDefine(this.plugin, region));
		region.add(new EWRegionFlagAdd(this.plugin, region));
		region.add(new EWRegionFlagRemove(this.plugin, region));
		region.add(new EWRegionFlags(this.plugin, region));
		region.add(new EWRegionInfo(this.plugin, region));
		region.add(new EWRegionList(this.plugin, region));
		region.add(new EWRegionLoad(this.plugin, region));
		region.add(new EWRegionMemberAdd(this.plugin, region));
		region.add(new EWRegionMemberRemove(this.plugin, region));
		region.add(new EWRegionOwnerAdd(this.plugin, region));
		region.add(new EWRegionOwnerRemove(this.plugin, region));
		region.add(new EWRegionParent(this.plugin, region));
		region.add(new EWRegionPriority(this.plugin, region));
		region.add(new EWRegionRedefine(this.plugin, region));
		region.add(new EWRegionRemove(this.plugin, region));
		region.add(new EWRegionRename(this.plugin, region));
		region.add(new EWRegionSelect(this.plugin, region));
		region.add(new EWRegionTeleport(this.plugin, region));
		register(region);
	}
	
	public void loadSelect() {	
		EWSelect select = new EWSelect(this.plugin);
		select.add(new EWSelectInfo(this.plugin, select));
		select.add(new EWSelectExpand(this.plugin, select));
		select.add(new EWSelectContract(this.plugin, select));
		select.add(new EWSelectShift(this.plugin, select));
		select.add(new EWSelectPos1(this.plugin, select));
		select.add(new EWSelectPos2(this.plugin, select));
		select.add(new EWSelectType(this.plugin, select));
		select.add(new EWSelectClear(this.plugin, select));
		select.add(new EWSelectRemove(this.plugin, select));
		select.add(new EWSelectCui(this.plugin, select));
		register(select);
	}
	
	public void reload(){
		for (ECommand<EverWorldGuard> command : this) {
			if (command instanceof ReloadCommand) {
				((ReloadCommand) command).reload();
			}
		}
	}
	
	private void register(ECommand<EverWorldGuard> command) {
		this.command.add(command);
		this.add(command);
	}
}
