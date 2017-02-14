package fr.evercraft.everworldguard.service.storage;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public interface RegionStorage {

	Set<EProtectedRegion> getAll();
	
	<T> void add(EProtectedRegion region) throws StorageException;
	
	void remove(EProtectedRegion region) throws StorageException;
	
	void remove(Set<EProtectedRegion> regions) throws StorageException;

	void saveIdentifier(EProtectedRegion region, String identifier);

	void savePriority(EProtectedRegion region, int priority);

	void saveParent(EProtectedRegion region, @Nullable ProtectedRegion parent);

	void saveAddOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	void saveAddOwnerGroup(EProtectedRegion region, Set<String> groups);
	void saveAddMemberPlayer(EProtectedRegion region, Set<UUID> players);
	void saveAddMemberGroup(EProtectedRegion region, Set<String> groups);
	
	void saveRemoveOwnerPlayer(EProtectedRegion region, Set<UUID> players);
	void saveRemoveOwnerGroup(EProtectedRegion region, Set<String> groups);
	void saveRemoveMemberPlayer(EProtectedRegion region, Set<UUID> players);
	void saveRemoveMemberGroup(EProtectedRegion region, Set<String> groups);

	<V> void saveFlag(EProtectedRegion region, Flag<V> flag, Group group, V value);
}
