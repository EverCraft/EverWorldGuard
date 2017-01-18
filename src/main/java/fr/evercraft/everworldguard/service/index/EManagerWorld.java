package fr.evercraft.everworldguard.service.index;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import org.spongepowered.api.world.World;

import com.google.common.base.Preconditions;
import com.google.common.cache.LoadingCache;

import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.ProtectedRegion;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

public class EManagerWorld {
	
	private final EverWorldGuard plugin;
	
	private RegionStorage storage;
	
	private final Set<ProtectedRegion> regions;
	private final LoadingCache<UUID, EIndexChunk> cache;
	
	public EManagerWorld(EverWorldGuard plugin, World world) {
		Preconditions.checkNotNull(plugin, "plugin");
		
		this.plugin = plugin;
		this.regions = new HashSet<ProtectedRegion>();
		this.cache = null;
		
		if (this.plugin.getDataBase().isEnable()) {
			this.storage = null;
		} else {
			this.storage = null;
		}
	}

	public void reload() {
	}
}
