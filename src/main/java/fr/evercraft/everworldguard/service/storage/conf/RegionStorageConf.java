package fr.evercraft.everworldguard.service.storage.conf;

import java.util.Set;

import org.spongepowered.api.world.World;

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.ProtectedRegion;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

public class RegionStorageConf extends EConfig<EverWorldGuard> implements RegionStorage {
	
	private static final String DIR = "worlds";
	
	private final EverWorldGuard plugin;
	
	public RegionStorageConf(EverWorldGuard plugin, World world) {
		super(plugin, DIR + "/" + world.getName(), false);	
		
		this.plugin = plugin;
	}
	
	@Override
	protected void loadDefault() {
		// TODO Auto-generated method stub
		
	}

	public Set<ProtectedRegion> getAll() throws StorageException {
		return null;
	}
	
	public void save(ProtectedRegion difference) throws StorageException {
		
	}
	
	public void save(Set<ProtectedRegion> regions) throws StorageException {
		
	}
}
