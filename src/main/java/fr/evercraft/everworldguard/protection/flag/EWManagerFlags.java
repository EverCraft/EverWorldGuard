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

import fr.evercraft.everapi.java.UtilsField;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.flags.*;

public class EWManagerFlags {

	private final EverWorldGuard plugin;
	
	public final FlagBuild BUILD;
	public final FlagInteractBlock INTERACT_BLOCK;
	public final FlagInteractEntity INTERACT_ENTITY;
	public final FlagDamageEntity DAMAGE_ENTITY;
	public final FlagEntry ENTRY;
	public final FlagEntryMessage ENTRY_MESSAGE;
	public final FlagExit EXIT;
	public final FlagInvincibility INVINCIBILITY;
	public final FlagPvp PVP;
	
	public final FlagSpawn SPAWN;
	public final FlagTeleport TELEPORT;
	
	public EWManagerFlags(EverWorldGuard plugin) {
		this.plugin = plugin;
		
		this.register();

		BUILD = new FlagBuild(this.plugin);
		INTERACT_BLOCK = new FlagInteractBlock(this.plugin);
		INTERACT_ENTITY = new FlagInteractEntity(this.plugin);
		DAMAGE_ENTITY = new FlagDamageEntity(this.plugin);
		ENTRY = new FlagEntry();
		ENTRY_MESSAGE = new FlagEntryMessage();
		EXIT = new FlagExit();
		INVINCIBILITY = new FlagInvincibility();
		PVP = new FlagPvp(this.plugin);
		
		SPAWN = new FlagSpawn(this.plugin);
		TELEPORT = new FlagTeleport(this.plugin);
		
		this.register();
	}
	
	private void register() {
		for (Field field : Flags.class.getFields()) {
			if (Modifier.isStatic(field.getModifiers())) {
				try {
					Field fieldFlag = this.getClass().getField(field.getName());
					try {
						Object flag = fieldFlag.get(this);
						if (flag instanceof Flag) {
							UtilsField.setFinalStatic(field, flag);
							this.plugin.getProtectionService().registerFlag((Flag<?>) flag);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					this.plugin.getELogger().warn("[Flag] Not yet implemented : " + field.getName());
				}
			}
		}
	}
}
