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
		Flags.BUILD = new FlagBuild();
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
