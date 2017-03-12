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
package fr.evercraft.everworldguard.protection.flag;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.flags.*;

public class ManagerFlags {

	private final EverWorldGuard plugin;
	
	public ManagerFlags(EverWorldGuard plugin) {
		this.plugin = plugin;
		
		this.load();
		this.register();
	}

	private void load() {
		Flags.BUILD = new FlagBuild(this.plugin);
		Flags.INTERACT = new FlagInteract(this.plugin);
		Flags.ENTRY = new FlagEntry();
		Flags.EXIT = new FlagExit();
		Flags.INVINCIBILITY = new FlagInvincibility();
		Flags.PVP = new FlagPvp();
		
		Flags.SPAWN = new FlagSpawn();
		Flags.TELEPORT = new FlagTeleport();
	}
	
	private void register() {
		for (Field field : Flags.class.getFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					Object flag = field.get(null);
					if (flag instanceof Flag) {
						this.plugin.getProtectionService().registerFlag((Flag<?>) flag);
						this.plugin.getGame().getEventManager().registerListeners(this.plugin, flag);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
