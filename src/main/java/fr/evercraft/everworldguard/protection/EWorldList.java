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
package fr.evercraft.everworldguard.protection;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.spongepowered.api.world.World;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;

public class EWorldList {
	
	private final EverWorldGuard plugin;
	
	private final ConcurrentMap<UUID, EWWorld> worlds;
	
	
	public EWorldList(final EverWorldGuard plugin) {		
		this.plugin = plugin;
		
		this.worlds = new ConcurrentHashMap<UUID, EWWorld>();
	}
	
	public EWWorld get(World world) {
		Preconditions.checkNotNull(world, "world");
		
		return Optional.ofNullable(this.worlds.get(world.getUniqueId())).orElseGet(() -> new EWWorld(this.plugin, world));
	}
	
	public CompletableFuture<WorldGuardWorld> getOrCreate(World world) {
		Preconditions.checkNotNull(world, "world");
		
		return CompletableFuture.supplyAsync(() -> {
			EWWorld value = this.worlds.get(world.getUniqueId());
			if (value == null) {
				value = new EWWorld(this.plugin, world);
				this.worlds.put(world.getUniqueId(), value);
			}
			return value;
		}, this.plugin.getThreadAsync());
	}

	public boolean hasRegistered(World world) {
		Preconditions.checkNotNull(world, "world");
		
		return this.worlds.containsKey(world.getUniqueId());
	}
	
	public void reload() {		
		this.worlds.forEach((uuid, world) -> world.reload());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Set<WorldGuardWorld> getAll() {
		return (Set) ImmutableSet.copyOf(this.worlds.values());
	}

	public void unLoad(World world) {
		Preconditions.checkNotNull(world, "world");
		
		this.worlds.remove(world.getUniqueId());
	}
}
