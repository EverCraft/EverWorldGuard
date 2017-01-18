package fr.evercraft.everworldguard.service.storage;

import java.util.Set;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.regions.ProtectedRegion;

public interface RegionStorage {

	Set<ProtectedRegion> getAll() throws StorageException;
	
	void save(ProtectedRegion difference) throws StorageException;
	
	void save(Set<ProtectedRegion> regions) throws StorageException;
}
