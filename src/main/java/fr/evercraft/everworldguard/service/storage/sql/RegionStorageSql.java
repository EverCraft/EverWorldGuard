package fr.evercraft.everworldguard.service.storage.sql;

import java.util.Set;

import org.spongepowered.api.world.World;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.ProtectedRegion;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

public class RegionStorageSql implements RegionStorage {
	
	private final EverWorldGuard plugin;
	
	public RegionStorageSql(EverWorldGuard plugin, World world) {		
		this.plugin = plugin;
	}
	
	public Set<ProtectedRegion> getAll() throws StorageException {
		return null;
	}
	
	public void save(ProtectedRegion difference) throws StorageException {
		
	}
	
	public void save(Set<ProtectedRegion> regions) throws StorageException {
		
	}
}
