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
package fr.evercraft.everworldguard.protection.storage;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public class ManagerRegionStorage implements RegionStorage {	
	private final EverWorldGuard plugin;
	private final EWWorld world;
	private RegionStorage storage;
	
	public ManagerRegionStorage(EverWorldGuard plugin, EWWorld world) {
		this.plugin = plugin;
		this.world = world;
		
		if (this.plugin.getDataBases().isEnable()) {
			this.storage = new RegionStorageSql(this.plugin, this.world);
		} else {
			this.storage = new RegionStorageConf(this.plugin, this.world);
		}
	}

	@Override
	public void reload() {
		if (this.plugin.getDataBases().isEnable() && !(this.storage instanceof RegionStorageSql)) {
			this.storage = new RegionStorageSql(this.plugin, this.world);
		} else if (!this.plugin.getDataBases().isEnable() && !(this.storage instanceof RegionStorageConf)) {
			this.storage = new RegionStorageConf(this.plugin, this.world);
		} else {
			this.storage.reload();
		}
	}

	@Override
	public CompletableFuture<Set<EProtectedRegion>> getAll() {
		return this.storage.getAll();
	}

	@Override
	public <T> CompletableFuture<Boolean> add(EProtectedRegion region) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.add(region);
	}

	@Override
	public CompletableFuture<Boolean> setName(EProtectedRegion region, String identifier) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.setName(region, identifier);
	}

	@Override
	public CompletableFuture<Boolean> setPriority(EProtectedRegion region, int priority) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.setPriority(region, priority);
	}

	@Override
	public CompletableFuture<Boolean> setParent(EProtectedRegion region, ProtectedRegion parent) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.setParent(region, parent);
	}

	@Override
	public <V> CompletableFuture<Boolean> setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.setFlag(region, flag, group, value);
	}

	@Override
	public CompletableFuture<Boolean> addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.addOwnerPlayer(region, players);
	}

	@Override
	public CompletableFuture<Boolean> addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.addOwnerGroup(region, groups);
	}

	@Override
	public CompletableFuture<Boolean> addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.addMemberPlayer(region, players);
	}

	@Override
	public CompletableFuture<Boolean> addMemberGroup(EProtectedRegion region, Set<String> groups) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.addMemberGroup(region, groups);
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.removeOwnerPlayer(region, players);
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.removeOwnerGroup(region, groups);
	}

	@Override
	public CompletableFuture<Boolean> removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.removeMemberPlayer(region, players);
	}

	@Override
	public CompletableFuture<Boolean> removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.removeMemberGroup(region, groups);
	}

	public CompletableFuture<Boolean> removeClearParent(EProtectedRegion region, Set<EProtectedRegion> regions) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.removeClearParent(region, regions);
	}

	public CompletableFuture<Boolean> removeRemoveChildren(Set<EProtectedRegion> regions) {
		return this.storage.removeRemoveChildren(regions.stream().filter(region -> !region.isTransient()).collect(Collectors.toSet()));
	}

	@Override
	public CompletableFuture<Boolean> redefine(EProtectedRegion region, EProtectedRegion newRegion) {
		if (region.isTransient()) return CompletableFuture.completedFuture(true);
		return this.storage.redefine(region, newRegion);
	}
}
