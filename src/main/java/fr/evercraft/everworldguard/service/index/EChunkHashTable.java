package fr.evercraft.everworldguard.service.index;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.util.Chronometer;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.ProtectedRegion;

public class EChunkHashTable {
	
	private final EverWorldGuard plugin;

	private final Set<ProtectedRegion> regions;
	
	public EChunkHashTable(EverWorldGuard plugin, Vector2i vector, Set<ProtectedRegion> regions) {
		this.plugin = plugin;
		Builder<ProtectedRegion> builder = ImmutableSet.builder();
		regions.stream()
			.filter(region -> region.contains(vector))
			.forEach(region -> builder.add(region));
		this.regions = builder.build();
	}

}
