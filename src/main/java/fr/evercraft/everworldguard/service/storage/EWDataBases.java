/*
 * This file is part of EverPermissions.
 *
 * EverPermissions is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverPermissions is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverPermissions.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.service.storage;

import fr.evercraft.everapi.exception.PluginDisableException;
import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.plugin.EDataBase;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWDataBases extends EDataBase<EverWorldGuard> {
	private String table_users;

	public EWDataBases(EverWorldGuard plugin) throws PluginDisableException {
		super(plugin);
	}

	public boolean init() throws ServerDisableException {
		this.table_users = "table_users";
		String permissions ="CREATE TABLE IF NOT EXISTS <table> (" +
							"`uuid` varchar(36) NOT NULL," +
							"`region` INTEGER," +
							"PRIMARY KEY (`uuid`, `region`));";
		initTable(this.getTableUsersPermissions(), permissions);
		return true;
	}

	public String getTableUsersPermissions() {
		return this.getPrefix() + this.table_users;
	}
}
