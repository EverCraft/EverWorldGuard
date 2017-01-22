package fr.evercraft.everworldguard.service.index;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.util.Chronometer;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public class EManagerChunk {
	
	private final EverWorldGuard plugin;

	private final Set<EProtectedRegion> regions;
	private final LoadingCache<Vector3i, ESetProtectedRegion> cache;
	
	public EManagerChunk(EverWorldGuard plugin, Vector3i vector, Set<EProtectedRegion> regions) {
		this.plugin = plugin;
		Builder<EProtectedRegion> builder = ImmutableSet.builder();
		regions.stream()
			.filter(region -> region.containsPosition(vector))
			.forEach(region -> builder.add(region));
		this.regions = builder.build();
		
		this.cache = CacheBuilder.newBuilder()
					    .maximumSize(32)
					    .expireAfterAccess(2, TimeUnit.MINUTES)
					    .build(new CacheLoader<Vector3i, ESetProtectedRegion>() {
					        @Override
					        public ESetProtectedRegion load(Vector3i position){
					        	Chronometer chronometer = new Chronometer();
					        	
					        	ESetProtectedRegion regions = new ESetProtectedRegion(position, EManagerChunk.this.regions);
					        	
					        	EManagerChunk.this.plugin.getLogger().debug("Loading bloc (x:" + vector.getX() + ";y:" + vector.getY() + ";z:" + vector.getY() + ") in " +  chronometer.getMilliseconds().toString() + " ms");
					            return regions;
					        }
					    });
	}
	
	public ESetProtectedRegion getPosition(final Vector3i position) {
		try {
			return this.cache.get(position);
		} catch (ExecutionException e) {
			return new ESetProtectedRegion(position, EManagerChunk.this.regions);
		}
	}
	
	public void clear() {
		this.cache.cleanUp();
	}

}
