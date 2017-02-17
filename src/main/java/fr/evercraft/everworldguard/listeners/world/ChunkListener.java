package fr.evercraft.everworldguard.listeners.world;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.world.Chunk;

import fr.evercraft.everworldguard.EverWorldGuard;

public class ChunkListener {
	
	private EverWorldGuard plugin;

	public ChunkListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.PRE)
	public void onLoadChunk(LoadChunkEvent event) {
		Chunk chunk = event.getTargetChunk();
		this.plugin.getProtectionService().getOrCreateEWorld(chunk.getWorld()).loadChunk(chunk.getPosition());
	}
	
	@Listener(order=Order.PRE)
	public void onUnloadChunk(UnloadChunkEvent event) {
		Chunk chunk = event.getTargetChunk();
		this.plugin.getProtectionService().getOrCreateEWorld(chunk.getWorld()).loadChunk(chunk.getPosition());
	}

}
