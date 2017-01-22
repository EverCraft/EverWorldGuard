package fr.evercraft.everworldguard.service.storage;

import java.util.Set;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public interface RegionStorage {

	Set<EProtectedRegion> getAll();
	
	<T> void add(EProtectedRegion region) throws StorageException;
	
	void remove(EProtectedRegion region) throws StorageException;
	
	void remove(Set<EProtectedRegion> regions) throws StorageException;
}
