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
