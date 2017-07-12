/*
 * This file is part of EverWorldGuard.
 *
 * EverWorldGuard is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverWorldGuard is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverWorldGuard.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.everworldguard.protection.index;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public class EWChunck {
	
	private final EverWorldGuard plugin;

	private final Vector3i position_chunck;
	private final Set<EProtectedRegion> regions;
	private final LoadingCache<Vector3i, ESetProtectedRegion> cache;
	
	public EWChunck(EverWorldGuard plugin, Vector3i position, ConcurrentHashMap<UUID, EProtectedRegion> regions) {
		this.plugin = plugin;
		this.position_chunck = position;
		Builder<EProtectedRegion> builder = ImmutableSet.builder();
		regions.forEach((id, region) -> {
			if (region.containsChunck(position)) {
				builder.add(region);
			}
		});
		this.regions = builder.build();
		
		this.cache = CacheBuilder.newBuilder()
		    .maximumSize(32)
		    .expireAfterAccess(2, TimeUnit.MINUTES)
		    .build(new CacheLoader<Vector3i, ESetProtectedRegion>() {
		        @Override
		        public ESetProtectedRegion load(final Vector3i position){
		        	Chronometer chronometer = new Chronometer();
		        	
		        	ESetProtectedRegion regions = new ESetProtectedRegion(position, EWChunck.this.regions);
		        	
		        	EWChunck.this.plugin.getELogger().debug("Loading block (x='" + position.getX() + "';y='" + position.getY() + "';z='" + position.getZ() + "') in " +  chronometer.getMilliseconds().toString() + " ms");
		            return regions;
		        }
		    });
	}
	
	public Vector3i getPosition() {
		return this.position_chunck;
	}
	
	public ESetProtectedRegion getRegion(final Vector3i position) {
		try {
			return this.cache.get(position);
		} catch (ExecutionException e) {
			return new ESetProtectedRegion(position, EWChunck.this.regions);
		}
	}
	
	public void clear() {
		this.cache.cleanUp();
	}
	
	public Set<EProtectedRegion> getAll() {
		return this.regions;
	}

}
