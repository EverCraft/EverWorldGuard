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
package fr.evercraft.everworldguard.listeners;

import java.util.concurrent.ExecutionException;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.event.world.chunk.LoadChunkEvent;
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent;
import org.spongepowered.api.world.Chunk;

import fr.evercraft.everapi.sponge.UtilsCause;
import fr.evercraft.everworldguard.EverWorldGuard;

public class WorldListener {
	
	private EverWorldGuard plugin;

	public WorldListener(EverWorldGuard plugin) {
		this.plugin = plugin;
	}
	
	@Listener(order=Order.PRE)
	public void onLoadWorld(LoadWorldEvent event) {
		UtilsCause.debug(event.getCause(), "LoadWorldEvent");
		try {
			this.plugin.getProtectionService().getOrCreateWorld(event.getTargetWorld()).get();
		} catch (InterruptedException | ExecutionException e) {}
	}
	
	@Listener(order=Order.PRE)
	public void onUnloadWorld(UnloadWorldEvent event) {
		this.plugin.getProtectionService().unLoadWorld(event.getTargetWorld());
	}
	
	@Listener(order=Order.PRE)
	public void onLoadChunk(LoadChunkEvent event) {
		UtilsCause.debug(event.getCause(), "LoadChunkEvent");
		Chunk chunk = event.getTargetChunk();
		this.plugin.getProtectionService().getOrCreateEWorld(chunk.getWorld()).loadChunk(chunk.getPosition());
	}
	
	@Listener(order=Order.PRE)
	public void onUnloadChunk(UnloadChunkEvent event) {
		Chunk chunk = event.getTargetChunk();
		this.plugin.getProtectionService().getOrCreateEWorld(chunk.getWorld()).loadChunk(chunk.getPosition());
	}
}
