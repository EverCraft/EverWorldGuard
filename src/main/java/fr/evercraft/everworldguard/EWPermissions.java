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
	
	DEFINE("region.define"),
	
	REDEFINE_OWN("redefine.own"),
	REDEFINE_MEMBER("redefine.member"),
	
	ADD_OWNER_OWN("addowner.own"),
	ADD_OWNER_MEMBER("addowner.member"),
	ADD_OWNER_ALL("addowner"),
	
	REMOVE_OWNER_OWN("removeowner.own"),
	REMOVE_OWNER_MEMBER("removeowner.member"),
	REMOVE_OWNER_ALL("removeowner"),
	
	ADD_MEMBER_OWN("addmember.own"),
	ADD_MEMBER_MEMBER("addmember.member"),
	ADD_MEMBER_ALL("addmember"),
	
	REMOVE_MEMBER_OWN("removemember.own"),
	REMOVE_MEMBER_MEMBER("removemember.member"),
	REMOVE_MEMBER_ALL("removemember"),
	
	SELECT("select.command"),
	SELECT_WAND("select.wand"),
	SELECT_POS("select.pos"),
	SELECT_EXPAND("select.expand"),
	
	REGION("region.command"),
	
	REGION_INFO("region.info.command"),
	REGION_INFO_REGIONS("region.info.regions"),
	
	REGION_LIST("region.list.command"),
	REGION_LIST_OWN("region.list.others"),
	
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
