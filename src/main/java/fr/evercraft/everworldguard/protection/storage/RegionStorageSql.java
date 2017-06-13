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

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public class RegionStorageSql implements RegionStorage {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;
	
	public RegionStorageSql(EverWorldGuard plugin, EWWorld world) {		
		this.plugin = plugin;
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public CompletableFuture<Set<EProtectedRegion>> getAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> CompletableFuture<Boolean> add(EProtectedRegion region) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> setName(EProtectedRegion region, String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> setPriority(EProtectedRegion region, int priority) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> setParent(EProtectedRegion region, ProtectedRegion parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <V> CompletableFuture<Boolean> setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> addMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeClearParent(EProtectedRegion region, Set<EProtectedRegion> regions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<Boolean> removeRemoveChildren(Set<ProtectedRegion> regions) {
		// TODO Auto-generated method stub
		return null;
	}
}
