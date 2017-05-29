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
	
	public final FlagBlockBreak BLOCK_BREAK;
	public final FlagBlockPlace BLOCK_PLACE;
	public final FlagBuild BUILD;
	public final FlagChat CHAT;
	public final FlagDamageEntity DAMAGE_ENTITY;
	public final FlagEnderDragonGrief ENDERDRAGON_GRIEF;
	public final FlagEndermanGrief ENDERMAN_GRIEF;
	public final FlagEnderPearl ENDERPEARL;
	public final FlagEntityDamage ENTITY_DAMAGE;
	public final FlagEntitySpawning ENTITY_SPAWNING;
	public final FlagEntry ENTRY;
	public final FlagEntryMessage ENTRY_MESSAGE;
	public final FlagEntryDenyMessage ENTRY_DENY_MESSAGE;
	public final FlagExit EXIT;
	public final FlagExitMessage EXIT_MESSAGE;
	public final FlagExitDenyMessage EXIT_DENY_MESSAGE;
	public final FlagExpDrop EXP_DROP;
	public final FlagExplosion EXPLOSION;
	public final FlagExplosionBlock EXPLOSION_BLOCK;
	public final FlagExplosionDamage EXPLOSION_DAMAGE;
	public final FlagFire FIRE;
	public final FlagIce ICE;
	public final FlagInteractBlock INTERACT_BLOCK;
	public final FlagInteractEntity INTERACT_ENTITY;
	public final FlagInventoryDrop INVENTORY_DROP;
	public final FlagInvincibility INVINCIBILITY;
	public final FlagItemDrop ITEM_DROP;
	public final FlagItemPickup ITEM_PICKUP;
	public final FlagLightning LIGHTNING;
	public final FlagPvp PVP;
	public final FlagSnow SNOW;
	public final FlagSpawn SPAWN;
	public final FlagTeleport TELEPORT;
	
	public EWManagerFlags(EverWorldGuard plugin) {
		this.plugin = plugin;
		
		this.register();

		BLOCK_BREAK = new FlagBlockBreak(this.plugin);
		BLOCK_PLACE = new FlagBlockPlace(this.plugin);
		BUILD = new FlagBuild(this.plugin);
		CHAT = new FlagChat(this.plugin);
		DAMAGE_ENTITY = new FlagDamageEntity(this.plugin);
		ENDERDRAGON_GRIEF = new FlagEnderDragonGrief(this.plugin);
		ENDERMAN_GRIEF = new FlagEndermanGrief(this.plugin);
		ENDERPEARL = new FlagEnderPearl(this.plugin);
		ENTITY_DAMAGE = new FlagEntityDamage(this.plugin);
		ENTITY_SPAWNING = new FlagEntitySpawning(this.plugin);
		ENTRY = new FlagEntry();
		ENTRY_MESSAGE = new FlagEntryMessage();
		ENTRY_DENY_MESSAGE = new FlagEntryDenyMessage();
		EXIT = new FlagExit();
		EXIT_MESSAGE = new FlagExitMessage();
		EXIT_DENY_MESSAGE = new FlagExitDenyMessage();
		EXP_DROP = new FlagExpDrop(this.plugin);
		EXPLOSION = new FlagExplosion(this.plugin);
		EXPLOSION_BLOCK = new FlagExplosionBlock(this.plugin);
		EXPLOSION_DAMAGE = new FlagExplosionDamage(this.plugin);
		FIRE = new FlagFire(this.plugin);
		ICE = new FlagIce(this.plugin);
		INTERACT_BLOCK = new FlagInteractBlock(this.plugin);
		INTERACT_ENTITY = new FlagInteractEntity(this.plugin);
		INVENTORY_DROP = new FlagInventoryDrop(this.plugin);
		INVINCIBILITY = new FlagInvincibility();
		ITEM_DROP = new FlagItemDrop(this.plugin);
		ITEM_PICKUP = new FlagItemPickup(this.plugin);
		LIGHTNING = new FlagLightning(this.plugin);
		PVP = new FlagPvp(this.plugin);
		SNOW = new FlagSnow(this.plugin);
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
