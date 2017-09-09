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

import org.spongepowered.api.command.CommandSource;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.plugin.EnumPermission;

public enum EWPermissions implements EnumPermission {
	EVERWORLDGUARD("commands.execute"),
	
	HELP("commands.help"),
	RELOAD("commands.reload"),
	MIGRATE("commands.migrate"),
	CLEAR("commands.clear"),
	
	SELECT("commands.select.execute"),
	SELECT_WAND("commands.select.wand"),
	SELECT_POS("commands.select.pos"),
	SELECT_EXPAND("commands.select.expand"),
	SELECT_CUI("commands.select.cui"),
	
	REGION("commands.region.execute"),
	REGION_LOAD("commands.region.load"),
	REGION_FLAGS("commands.region.flags"),
	REGION_BYPASS("commands.region.bypass"),
	REGION_CHECK("commands.region.check"),
	
	REGION_INFO("commands.region.info.execute"),
	REGION_INFO_ITEM("commands.region.info.item"),
	REGION_INFO_OWNER("commands.region.info.owner"),
	REGION_INFO_MEMBER("commands.region.info.member"),
	REGION_INFO_REGIONS("commands.region.info.regions"),
	
	REGION_LIST("commands.region.list.execute"),
	REGION_LIST_OTHERS("commands.region.list.others"),
	
	REGION_DEFINE("commands.region.define.execute"),
	REGION_DEFINE_TEMPLATE("commands.region.define.template"),
	
	REGION_REMOVE("commands.region.remove.execute"),
	REGION_REMOVE_OWNER("commands.region.remove.owner"),
	REGION_REMOVE_MEMBER("commands.region.remove.member"),
	REGION_REMOVE_REGIONS("commands.region.remove.regions"),
	
	REGION_REDEFINE("commands.region.redefine.execute"),
	REGION_REDEFINE_OWNER("commands.region.redefine.owner"),
	REGION_REDEFINE_MEMBER("commands.region.redefine.member"),
	REGION_REDEFINE_REGIONS("commands.region.redefine.regions"),
	
	REGION_RENAME("commands.region.rename.execute"),
	REGION_RENAME_OWNER("commands.region.rename.owner"),
	REGION_RENAME_MEMBER("commands.region.rename.member"),
	REGION_RENAME_REGIONS("commands.region.rename.regions"),
	
	REGION_SELECT("commands.region.select.execute"),
	REGION_SELECT_OWNER("commands.region.select.owner"),
	REGION_SELECT_MEMBER("commands.region.select.member"),
	REGION_SELECT_REGIONS("commands.region.select.regions"),
	
	REGION_FLAG_ADD("commands.region.flag.add.execute"),
	REGION_FLAG_ADD_OWNER("commands.region.flag.add.owner"),
	REGION_FLAG_ADD_MEMBER("commands.region.flag.add.member"),
	REGION_FLAG_ADD_REGIONS("commands.region.flag.add.regions"),
	
	REGION_FLAG_REMOVE("commands.region.flag.remove.execute"),
	REGION_FLAG_REMOVE_OWNER("commands.region.flag.remove.owner"),
	REGION_FLAG_REMOVE_MEMBER("commands.region.flag.remove.member"),
	REGION_FLAG_REMOVE_REGIONS("commands.region.flag.remove.regions"),
	
	REGION_OWNER_ADD("commands.region.owner.add.execute"),
	REGION_OWNER_ADD_OWNER("commands.region.owner.add.owner"),
	REGION_OWNER_ADD_MEMBER("commands.region.owner.add.member"),
	REGION_OWNER_ADD_REGIONS("commands.region.owner.add.regions"),
	
	REGION_OWNER_REMOVE("commands.region.owner.remove.execute"),
	REGION_OWNER_REMOVE_OWNER("commands.region.owner.remove.owner"),
	REGION_OWNER_REMOVE_MEMBER("commands.region.owner.remove.member"),
	REGION_OWNER_REMOVE_REGIONS("commands.region.owner.remove.regions"),
	
	REGION_MEMBER_ADD("commands.region.member.add.execute"),
	REGION_MEMBER_ADD_OWNER("commands.region.member.add.owner"),
	REGION_MEMBER_ADD_MEMBER("commands.region.member.add.member"),
	REGION_MEMBER_ADD_REGIONS("commands.region.member.add.regions"),
	
	REGION_MEMBER_REMOVE("commands.region.member.remove.execute"),
	REGION_MEMBER_REMOVE_OWNER("commands.region.member.remove.owner"),
	REGION_MEMBER_REMOVE_MEMBER("commands.region.member.remove.member"),
	REGION_MEMBER_REMOVE_REGIONS("commands.region.member.remove.regions"),
	
	REGION_PARENT("commands.region.setparent.execute"),
	REGION_PARENT_OWNER("commands.region.setparent.owner"),
	REGION_PARENT_MEMBER("commands.region.setparent.member"),
	REGION_PARENT_REGIONS("commands.region.setparent.regions"),
	
	REGION_PRIORITY("commands.region.setpriority.execute"),
	REGION_PRIORITY_OWNER("commands.region.setpriority.owner"),
	REGION_PRIORITY_MEMBER("commands.region.setpriority.member"),
	REGION_PRIORITY_REGIONS("commands.region.setpriority.regions"),
	
	REGION_TELEPORT("commands.region.teleport.execute"),
	REGION_TELEPORT_SPAWN("commands.region.teleport.spawn"),
	REGION_TELEPORT_OWNER("commands.region.teleport.owner"),
	REGION_TELEPORT_MEMBER("commands.region.teleport.member"),
	REGION_TELEPORT_REGIONS("commands.region.teleport.regions"),
	
	FLAGS("flags");
	
	private final static String prefix = "everworldguard";
	
	private final String permission;
    
    private EWPermissions(final String permission) {   	
    	Preconditions.checkNotNull(permission, "La permission '" + this.name() + "' n'est pas définit");
    	
    	this.permission = permission;
    }

    public String get() {
		return EWPermissions.prefix + "." + this.permission;
	}
    
    public boolean has(CommandSource player) {
    	return player.hasPermission(this.get());
    }
}
