package fr.evercraft.everworldguard.service.index;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.evercraft.everapi.util.Chronometer;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.ProtectedRegion;
import fr.evercraft.everworldguard.service.EUserSubjectList;
import fr.evercraft.everworldguard.service.storage.RegionStorage;
import fr.evercraft.everworldguard.service.storage.conf.RegionStorageConf;
import fr.evercraft.everworldguard.service.storage.sql.RegionStorageSql;
import fr.evercraft.everworldguard.service.subject.EUserSubject;

public class EManagerWorld {
	
	private final EverWorldGuard plugin;
	
	private RegionStorage storage;
	
	private final Set<ProtectedRegion> regions;
	private final LoadingCache<Vector2i, EIndexChunk> cache;
	
	private final World world;
	
	public EManagerWorld(EverWorldGuard plugin, World world) {
		Preconditions.checkNotNull(plugin, "plugin");
		
		this.plugin = plugin;
		this.world = world;
		this.regions = new HashSet<ProtectedRegion>();		
		this.cache = CacheBuilder.newBuilder()
			    .maximumSize(this.plugin.getEServer().getMaxPlayers() * 5)
			    .expireAfterAccess(2, TimeUnit.MINUTES)
			    .build(new CacheLoader<Vector2i, EIndexChunk>() {
			        @Override
			        public EIndexChunk load(Vector2i vector){
			        	Chronometer chronometer = new Chronometer();
			        	
			        	EIndexChunk chunk = new EIndexChunk(EManagerWorld.this.plugin, vector, EManagerWorld.this.regions);
			        	EManagerWorld.this.plugin.getLogger().debug("Loading chunk (x:" + vector.getX() + ";z:" + vector.getY() + ") in " +  chronometer.getMilliseconds().toString() + " ms");
			        	
			            return chunk;
			        }
			    });
		
		if (this.plugin.getDataBase().isEnable()) {
			this.storage = null;
		} else {
			this.storage = null;
		}
	}

	public void reload() {
		this.save();
		
		if (this.plugin.getDataBase().isEnable() && !(this.storage instanceof RegionStorageSql)) {
			this.storage = new RegionStorageSql(this.plugin, this.world);
		} else if (!this.plugin.getDataBase().isEnable() && !(this.storage instanceof RegionStorageConf)) {
			this.storage = new RegionStorageConf(this.plugin, this.world);
		}
	}
	
	public void save() {
		
	}
	
	public EIndexChunk getChunk(final Vector2i chunk) throws ExecutionException {
		return this.cache.get(chunk);
	}
}
