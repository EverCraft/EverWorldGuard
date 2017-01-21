package fr.evercraft.everworldguard.listeners.world;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;

import fr.evercraft.everworldguard.EverWorldGuard;

public class WorldListener {
	
	private EverWorldGuard plugin;

	public WorldListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.PRE)
	public void onLoadWorld(LoadWorldEvent event) {
		this.plugin.getService().getOrCreate(event.getTargetWorld());
	}
	
	@Listener(order=Order.PRE)
	public void onUnloadWorld(UnloadWorldEvent event) {
		this.plugin.getService().unLoad(event.getTargetWorld());
	}

}
