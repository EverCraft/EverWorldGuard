package fr.evercraft.everworldguard.service.storage;

import java.util.Set;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.regions.ProtectedRegion;

public interface RegionStorage {

	Set<ProtectedRegion> getAll();
	
	<T> void add(ProtectedRegion region) throws StorageException;
	
	void remove(ProtectedRegion region) throws StorageException;
	
	void remove(Set<ProtectedRegion> regions) throws StorageException;
}
