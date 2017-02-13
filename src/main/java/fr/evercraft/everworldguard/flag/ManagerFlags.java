package fr.evercraft.everworldguard.flag;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.Flags;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.flags.FlagBuild;
import fr.evercraft.everworldguard.flags.FlagInvincibility;
import fr.evercraft.everworldguard.flags.FlagPvp;
import fr.evercraft.everworldguard.flags.FlagSpawn;
import fr.evercraft.everworldguard.flags.FlagTeleport;

public class ManagerFlags {

	private final EverWorldGuard plugin;
	
	public ManagerFlags(EverWorldGuard plugin) {
		this.plugin = plugin;
		
		this.load();
		this.register();
	}

	private void load() {
		Flags.BUILD = new FlagBuild();
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
						this.plugin.getService().registerFlag((Flag<?>) flag);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
