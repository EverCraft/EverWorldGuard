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
