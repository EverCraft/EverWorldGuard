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

import javax.annotation.Nullable;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public interface RegionStorage {

	Set<EProtectedRegion> getAll();
	
	<T> void add(EProtectedRegion region);
	void remove(EProtectedRegion region);
	void remove(Set<EProtectedRegion> regions);

	void setIdentifier(EProtectedRegion region, String identifier);
	void setPriority(EProtectedRegion region, int priority);
	void setParent(EProtectedRegion region, @Nullable ProtectedRegion parent);
	<V> void setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value);

	void addOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	void addOwnerGroup(EProtectedRegion region, Set<String> groups);
	void addMemberPlayer(EProtectedRegion region, Set<UUID> players);
	void addMemberGroup(EProtectedRegion region, Set<String> groups);
	
	void removeOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	void removeOwnerGroup(EProtectedRegion region, Set<String> groups);
	void removeMemberPlayer(EProtectedRegion region, Set<UUID> players);
	void removeMemberGroup(EProtectedRegion region, Set<String> groups);
}
