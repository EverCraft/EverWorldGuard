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

import fr.evercraft.everapi.plugin.EnumPermission;
import fr.evercraft.everapi.plugin.file.EnumMessage;
import fr.evercraft.everworldguard.EWMessage.EWMessages;

public enum EWPermissions implements EnumPermission {
	EVERWORLDGUARD("commands.execute", EWMessages.PERMISSIONS_COMMANDS_EXECUTE),
	
	HELP("commands.help", EWMessages.PERMISSIONS_COMMANDS_HELP),
	RELOAD("commands.reload", EWMessages.PERMISSIONS_COMMANDS_RELOAD),
	MIGRATE("commands.migrate", EWMessages.PERMISSIONS_COMMANDS_MIGRATE),
	BYPASS("commands.bypass", EWMessages.PERMISSIONS_COMMANDS_BYPASS),
	CLEAR("commands.clear", EWMessages.PERMISSIONS_COMMANDS_CLEAR),
	
	SELECT("commands.select.execute", EWMessages.PERMISSIONS_COMMANDS_SELECT_EXECUTE),
	SELECT_WAND("commands.select.wand", EWMessages.PERMISSIONS_COMMANDS_SELECT_WAND),
	SELECT_POS("commands.select.pos", EWMessages.PERMISSIONS_COMMANDS_SELECT_POS),
	SELECT_EXPAND("commands.select.expand", EWMessages.PERMISSIONS_COMMANDS_SELECT_EXPAND),
	SELECT_CUI("commands.select.cui", EWMessages.PERMISSIONS_COMMANDS_SELECT_CUI),
	
	REGION("commands.region.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_EXECUTE),
	REGION_LOAD("commands.region.load", EWMessages.PERMISSIONS_COMMANDS_REGION_LOAD),
	REGION_FLAGS("commands.region.flags", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAGS),
	REGION_CHECK("commands.region.check", EWMessages.PERMISSIONS_COMMANDS_REGION_CHECK),
	
	REGION_INFO("commands.region.info.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_INFO_EXECUTE),
	REGION_INFO_ITEM("commands.region.info.item", EWMessages.PERMISSIONS_COMMANDS_REGION_INFO_ITEM),
	REGION_INFO_OWNER("commands.region.info.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_INFO_OWNER),
	REGION_INFO_MEMBER("commands.region.info.member", EWMessages.PERMISSIONS_COMMANDS_REGION_INFO_MEMBER),
	REGION_INFO_REGIONS("commands.region.info.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_INFO_REGIONS),
	
	REGION_LIST("commands.region.list.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_LIST_EXECUTE),
	REGION_LIST_OTHERS("commands.region.list.others", EWMessages.PERMISSIONS_COMMANDS_REGION_LIST_OTHERS),
	
	REGION_DEFINE("commands.region.define.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_DEFINE_EXECUTE),
	REGION_DEFINE_TEMPLATE("commands.region.define.template", EWMessages.PERMISSIONS_COMMANDS_REGION_DEFINE_TEMPLATE),
	
	REGION_REMOVE("commands.region.remove.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_REMOVE_EXECUTE),
	REGION_REMOVE_OWNER("commands.region.remove.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_REMOVE_OWNER),
	REGION_REMOVE_MEMBER("commands.region.remove.member", EWMessages.PERMISSIONS_COMMANDS_REGION_REMOVE_MEMBER),
	REGION_REMOVE_REGIONS("commands.region.remove.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_REMOVE_REGIONS),
	
	REGION_REDEFINE("commands.region.redefine.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_REDEFINE_EXECUTE),
	REGION_REDEFINE_OWNER("commands.region.redefine.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_REDEFINE_OWNER),
	REGION_REDEFINE_MEMBER("commands.region.redefine.member", EWMessages.PERMISSIONS_COMMANDS_REGION_REDEFINE_MEMBER),
	REGION_REDEFINE_REGIONS("commands.region.redefine.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_REDEFINE_REGIONS),
	
	REGION_RENAME("commands.region.rename.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_RENAME_EXECUTE),
	REGION_RENAME_OWNER("commands.region.rename.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_RENAME_OWNER),
	REGION_RENAME_MEMBER("commands.region.rename.member", EWMessages.PERMISSIONS_COMMANDS_REGION_RENAME_MEMBER),
	REGION_RENAME_REGIONS("commands.region.rename.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_RENAME_REGIONS),
	
	REGION_SELECT("commands.region.select.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_SELECT_EXECUTE),
	REGION_SELECT_OWNER("commands.region.select.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_SELECT_OWNER),
	REGION_SELECT_MEMBER("commands.region.select.member", EWMessages.PERMISSIONS_COMMANDS_REGION_SELECT_MEMBER),
	REGION_SELECT_REGIONS("commands.region.select.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_SELECT_REGIONS),
	
	REGION_FLAG_ADD("commands.region.flag.add.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_ADD_EXECUTE),
	REGION_FLAG_ADD_OWNER("commands.region.flag.add.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_ADD_OWNER),
	REGION_FLAG_ADD_MEMBER("commands.region.flag.add.member", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_ADD_MEMBER),
	REGION_FLAG_ADD_REGIONS("commands.region.flag.add.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_ADD_REGIONS),
	
	REGION_FLAG_REMOVE("commands.region.flag.remove.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_EXECUTE),
	REGION_FLAG_REMOVE_OWNER("commands.region.flag.remove.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_OWNER),
	REGION_FLAG_REMOVE_MEMBER("commands.region.flag.remove.member", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_MEMBER),
	REGION_FLAG_REMOVE_REGIONS("commands.region.flag.remove.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_FLAG_REMOVE_REGIONS),
	
	REGION_OWNER_ADD("commands.region.owner.add.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_ADD_EXECUTE),
	REGION_OWNER_ADD_OWNER("commands.region.owner.add.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_ADD_OWNER),
	REGION_OWNER_ADD_MEMBER("commands.region.owner.add.member", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_ADD_MEMBER),
	REGION_OWNER_ADD_REGIONS("commands.region.owner.add.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_ADD_REGIONS),
	
	REGION_OWNER_REMOVE("commands.region.owner.remove.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_EXECUTE),
	REGION_OWNER_REMOVE_OWNER("commands.region.owner.remove.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_OWNER),
	REGION_OWNER_REMOVE_MEMBER("commands.region.owner.remove.member", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_MEMBER),
	REGION_OWNER_REMOVE_REGIONS("commands.region.owner.remove.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_OWNER_REMOVE_REGIONS),
	
	REGION_MEMBER_ADD("commands.region.member.add.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_EXECUTE),
	REGION_MEMBER_ADD_OWNER("commands.region.member.add.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_OWNER),
	REGION_MEMBER_ADD_MEMBER("commands.region.member.add.member", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_MEMBER),
	REGION_MEMBER_ADD_REGIONS("commands.region.member.add.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_ADD_REGIONS),
	
	REGION_MEMBER_REMOVE("commands.region.member.remove.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_EXECUTE),
	REGION_MEMBER_REMOVE_OWNER("commands.region.member.remove.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_OWNER),
	REGION_MEMBER_REMOVE_MEMBER("commands.region.member.remove.member", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_MEMBER),
	REGION_MEMBER_REMOVE_REGIONS("commands.region.member.remove.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_MEMBER_REMOVE_REGIONS),
	
	REGION_PARENT("commands.region.setparent.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPARENT_EXECUTE),
	REGION_PARENT_OWNER("commands.region.setparent.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPARENT_OWNER),
	REGION_PARENT_MEMBER("commands.region.setparent.member", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPARENT_MEMBER),
	REGION_PARENT_REGIONS("commands.region.setparent.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPARENT_REGIONS),
	
	REGION_PRIORITY("commands.region.setpriority.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPRIORITY_EXECUTE),
	REGION_PRIORITY_OWNER("commands.region.setpriority.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPRIORITY_OWNER),
	REGION_PRIORITY_MEMBER("commands.region.setpriority.member", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPRIORITY_MEMBER),
	REGION_PRIORITY_REGIONS("commands.region.setpriority.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_SETPRIORITY_REGIONS),
	
	REGION_TELEPORT("commands.region.teleport.execute", EWMessages.PERMISSIONS_COMMANDS_REGION_TELEPORT_EXECUTE),
	REGION_TELEPORT_SPAWN("commands.region.teleport.spawn", EWMessages.PERMISSIONS_COMMANDS_REGION_TELEPORT_SPAWN),
	REGION_TELEPORT_OWNER("commands.region.teleport.owner", EWMessages.PERMISSIONS_COMMANDS_REGION_TELEPORT_OWNER),
	REGION_TELEPORT_MEMBER("commands.region.teleport.member", EWMessages.PERMISSIONS_COMMANDS_REGION_TELEPORT_MEMBER),
	REGION_TELEPORT_REGIONS("commands.region.teleport.regions", EWMessages.PERMISSIONS_COMMANDS_REGION_TELEPORT_REGIONS),
	
	FLAGS("flags", EWMessages.PERMISSIONS_FLAGS);
	
	private static final String PREFIX = "everworldguard";
	
	private final String permission;
	private final EnumMessage message;
	private final boolean value;
    
    private EWPermissions(final String permission, final EnumMessage message) {
    	this(permission, message, false);
    }
    
    private EWPermissions(final String permission, final EnumMessage message, final boolean value) {   	    	
    	this.permission = PREFIX + "." + permission;
    	this.message = message;
    	this.value = value;
    }

    @Override
    public String get() {
		return this.permission;
	}

	@Override
	public boolean getDefault() {
		return this.value;
	}

	@Override
	public EnumMessage getMessage() {
		return this.message;
	}
}
