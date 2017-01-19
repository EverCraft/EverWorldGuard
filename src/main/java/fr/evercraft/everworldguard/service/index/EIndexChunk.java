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

public class EIndexChunk {
	
	private final EverWorldGuard plugin;

	private final Set<ProtectedRegion> regions;
	private final LoadingCache<Vector2i, EIndexChunk> cache;
	
	public EIndexChunk(EverWorldGuard plugin, Vector2i vector, Set<ProtectedRegion> regions) {
		this.plugin = plugin;
		Builder<ProtectedRegion> builder = ImmutableSet.builder();
		regions.stream()
			.filter(region -> region.contains(vector))
			.forEach(region -> builder.add(region));
		this.regions = builder.build();
		
		this.cache = CacheBuilder.newBuilder()
			    .maximumSize(16)
			    .expireAfterAccess(2, TimeUnit.MINUTES)
			    .build(new CacheLoader<Vector2i, EIndexChunk>() {
			    	/**
			    	 * Ajoute un joueur au cache
			    	 */
			        @Override
			        public EIndexChunk load(Vector2i vector){
			        	Chronometer chronometer = new Chronometer();
			        	
			        	EIndexChunk chunk = new EIndexChunk(EIndexChunk.this.plugin, vector, EIndexChunk.this.regions);
			        	EIndexChunk.this.plugin.getLogger().debug("Loading chunk (x:" + vector.getX() + ";z:" + vector.getY() + ") in " +  chronometer.getMilliseconds().toString() + " ms");
			        	
			            return chunk;
			        }
			    });
	}

}
