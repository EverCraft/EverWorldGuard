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

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.plugin.file.EMessage;

public class EWConfig extends EConfig<EverWorldGuard> {

	public EWConfig(final EverWorldGuard plugin) {
		super(plugin);
	}
	
	public void reload() {
		super.reload();
		this.plugin.getLogger().setDebug(this.isDebug());
	}
	
	@Override
	public void loadDefault() {
		addDefault("DEBUG", false, "Displays plugin performance in the logs");
		addDefault("LANGUAGE", EMessage.FRENCH, "Select language messages", "Examples : ", "  French : FR_fr", "  English : EN_en");
		
		addComment("SQL", 	"Save the user in a database : ",
				" H2 : \"jdbc:h2:" + this.plugin.getPath().toAbsolutePath() + "/permissions\"",
				" SQL : \"jdbc:mysql://[login[:password]@]<host>:<port>/<database>\"",
				"By default users are saving in the 'users/'");
		addDefault("SQL.enable", false);
		addDefault("SQL.url", "jdbc:mysql://root:password@localhost:3306/minecraft");
		addDefault("SQL.prefix", "everworldguard_");
	}
}
