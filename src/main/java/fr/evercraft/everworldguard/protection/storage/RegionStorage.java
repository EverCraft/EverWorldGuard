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

import javax.annotation.Nullable;

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public interface RegionStorage {

	void reload();
	CompletableFuture<Set<EProtectedRegion>> getAll();
	
	<T> CompletableFuture<Boolean> add(EProtectedRegion region);

	CompletableFuture<Boolean> setName(EProtectedRegion region, String identifier);
	CompletableFuture<Boolean> setPriority(EProtectedRegion region, int priority);
	CompletableFuture<Boolean> setParent(EProtectedRegion region, @Nullable ProtectedRegion parent);
	<V> CompletableFuture<Boolean> setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value);

	CompletableFuture<Boolean> addOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	CompletableFuture<Boolean> addOwnerGroup(EProtectedRegion region, Set<String> groups);
	CompletableFuture<Boolean> addMemberPlayer(EProtectedRegion region, Set<UUID> players);
	CompletableFuture<Boolean> addMemberGroup(EProtectedRegion region, Set<String> groups);
	
	CompletableFuture<Boolean> removeOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	CompletableFuture<Boolean> removeOwnerGroup(EProtectedRegion region, Set<String> groups);
	CompletableFuture<Boolean> removeMemberPlayer(EProtectedRegion region, Set<UUID> players);
	CompletableFuture<Boolean> removeMemberGroup(EProtectedRegion region, Set<String> groups);
	
	CompletableFuture<Boolean> removeClearParent(EProtectedRegion region, Set<EProtectedRegion> regions);
	CompletableFuture<Boolean> removeRemoveChildren(Set<EProtectedRegion> regions);
	
	CompletableFuture<Boolean> redefine(EProtectedRegion region, EProtectedRegion newRegion);
}
