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
	EVERWORLDGUARD("command"),
	
	HELP("help"),
	RELOAD("reload"),
	
	SELECT("select.command"),
	SELECT_WAND("select.wand"),
	SELECT_POS("select.pos"),
	SELECT_EXPAND("select.expand"),
	SELECT_CUI("select.cui"),
	
	REGION("region.command"),
	REGION_LOAD("region.load"),
	REGION_FLAGS("region.flags"),
	REGION_BYPASS("region.bypass"),
	REGION_CHECK("region.check"),
	
	REGION_INFO("region.info.command"),
	REGION_INFO_OWNER("region.info.owner"),
	REGION_INFO_MEMBER("region.info.member"),
	REGION_INFO_REGIONS("region.info.regions"),
	
	REGION_LIST("region.list.command"),
	REGION_LIST_OTHERS("region.list.others"),
	
	REGION_DEFINE("region.define.command"),
	REGION_DEFINE_TEMPLATE("region.define.template"),
	
	REGION_REMOVE("region.remove.command"),
	REGION_REMOVE_OWNER("region.remove.owner"),
	REGION_REMOVE_MEMBER("region.remove.member"),
	REGION_REMOVE_REGIONS("region.remove.regions"),
	
	REGION_REDEFINE("region.redefine.command"),
	REGION_REDEFINE_OWNER("region.redefine.owner"),
	REGION_REDEFINE_MEMBER("region.redefine.member"),
	REGION_REDEFINE_REGIONS("region.redefine.regions"),
	
	REGION_RENAME("region.rename.command"),
	REGION_RENAME_OWNER("region.rename.owner"),
	REGION_RENAME_MEMBER("region.rename.member"),
	REGION_RENAME_REGIONS("region.rename.regions"),
	
	REGION_SELECT("region.select.command"),
	REGION_SELECT_OWNER("region.select.owner"),
	REGION_SELECT_MEMBER("region.select.member"),
	REGION_SELECT_REGIONS("region.select.regions"),
	
	REGION_FLAG_ADD("region.flag.add.command"),
	REGION_FLAG_ADD_OWNER("region.flag.add.owner"),
	REGION_FLAG_ADD_MEMBER("region.flag.add.member"),
	REGION_FLAG_ADD_REGIONS("region.flag.add.regions"),
	
	REGION_FLAG_REMOVE("region.flag.remove.command"),
	REGION_FLAG_REMOVE_OWNER("region.flag.remove.owner"),
	REGION_FLAG_REMOVE_MEMBER("region.flag.remove.member"),
	REGION_FLAG_REMOVE_REGIONS("region.flag.remove.regions"),
	
	REGION_OWNER_ADD("region.owner.add.command"),
	REGION_OWNER_ADD_OWNER("region.owner.add.owner"),
	REGION_OWNER_ADD_MEMBER("region.owner.add.member"),
	REGION_OWNER_ADD_REGIONS("region.owner.add.regions"),
	
	REGION_OWNER_REMOVE("region.owner.remove.command"),
	REGION_OWNER_REMOVE_OWNER("region.owner.remove.owner"),
	REGION_OWNER_REMOVE_MEMBER("region.owner.remove.member"),
	REGION_OWNER_REMOVE_REGIONS("region.owner.remove.regions"),
	
	REGION_MEMBER_ADD("region.member.add.command"),
	REGION_MEMBER_ADD_OWNER("region.member.add.owner"),
	REGION_MEMBER_ADD_MEMBER("region.member.add.member"),
	REGION_MEMBER_ADD_REGIONS("region.member.add.regions"),
	
	REGION_MEMBER_REMOVE("region.member.remove.command"),
	REGION_MEMBER_REMOVE_OWNER("region.member.remove.owner"),
	REGION_MEMBER_REMOVE_MEMBER("region.member.remove.member"),
	REGION_MEMBER_REMOVE_REGIONS("region.member.remove.regions"),
	
	REGION_PARENT("region.setparent.command"),
	REGION_PARENT_OWNER("region.setparent.owner"),
	REGION_PARENT_MEMBER("region.setparent.member"),
	REGION_PARENT_REGIONS("region.setparent.regions"),
	
	REGION_PRIORITY("region.setpriority.command"),
	REGION_PRIORITY_OWNER("region.setpriority.owner"),
	REGION_PRIORITY_MEMBER("region.setpriority.member"),
	REGION_PRIORITY_REGIONS("region.setpriority.regions"),
	
	REGION_TELEPORT("region.teleport.command"),
	REGION_TELEPORT_SPAWN("region.teleport.spawn"),
	REGION_TELEPORT_OWNER("region.teleport.owner"),
	REGION_TELEPORT_MEMBER("region.teleport.member"),
	REGION_TELEPORT_REGIONS("region.teleport.regions"),
	
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
