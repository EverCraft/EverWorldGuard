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

import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.listeners.world.ChunkListener;
import fr.evercraft.everworldguard.listeners.entity.EntityListener;
import fr.evercraft.everworldguard.listeners.entity.PlayerListener;
import fr.evercraft.everworldguard.listeners.entity.PlayerMoveListener;
import fr.evercraft.everworldguard.listeners.world.BlockListener;
import fr.evercraft.everworldguard.listeners.world.WorldListener;

public class EWListener {
	private EverWorldGuard plugin;

	public EWListener(EverWorldGuard plugin) {
		this.plugin = plugin;
		
		this.load();
	}
	
	public void load() {
		this.register(new WorldListener(this.plugin));
		this.register(new BlockListener(this.plugin));
		this.register(new ChunkListener(this.plugin));
		this.register(new EntityListener(this.plugin));
		this.register(new PlayerListener(this.plugin));
		this.register(new PlayerMoveListener(this.plugin));
		this.register(new ItemStackListener(this.plugin));
	}
	
	public void register(Object listener) {
		this.plugin.getGame().getEventManager().registerListeners(this.plugin, listener);
	}
}
