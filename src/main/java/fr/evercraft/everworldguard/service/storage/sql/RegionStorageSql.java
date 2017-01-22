package fr.evercraft.everworldguard.service.storage.sql;

import java.util.Set;

import org.spongepowered.api.world.World;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedRegion;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

public class RegionStorageSql implements RegionStorage {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;
	
	public RegionStorageSql(EverWorldGuard plugin, World world) {		
		this.plugin = plugin;
	}

	@Override
	public Set<EProtectedRegion> getAll() {
		return ImmutableSet.of();
	}

	@Override
	public void add(EProtectedRegion region) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(EProtectedRegion region) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(Set<EProtectedRegion> regions) throws StorageException {
		// TODO Auto-generated method stub
		
	}
}
