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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsChunk;
import fr.evercraft.everapi.util.LongHashTable;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedTemplateRegion;
import fr.evercraft.everworldguard.protection.storage.RegionStorage;
import fr.evercraft.everworldguard.protection.storage.RegionStorageConf;
import fr.evercraft.everworldguard.protection.storage.RegionStorageSql;

public class EWWorld implements WorldWorldGuard {
	
	private final EverWorldGuard plugin;
	
	private RegionStorage storage;
	
	private final ConcurrentHashMap<UUID, EProtectedRegion> regionsIdentifier;
	private final ConcurrentHashMap<String, EProtectedRegion> regionsName;
	private final LongHashTable<EWChunck> cache;
	
	private final World world;
	
	public EWWorld(EverWorldGuard plugin, World world) {
		Preconditions.checkNotNull(plugin, "plugin");
		
		this.plugin = plugin;
		this.world = world;
		this.regionsIdentifier = new ConcurrentHashMap<UUID, EProtectedRegion>();
		this.regionsName = new ConcurrentHashMap<String, EProtectedRegion>();	
		this.cache = new LongHashTable<EWChunck>();
		
		if (this.plugin.getDataBase().isEnable()) {
			this.storage = new RegionStorageSql(this.plugin, this);
		} else {
			this.storage = new RegionStorageConf(this.plugin, this);
		}
		
		this.start();
	}

	public void reload() {
		if (this.plugin.getDataBase().isEnable() && !(this.storage instanceof RegionStorageSql)) {
			this.storage = new RegionStorageSql(this.plugin, this);
		} else if (!this.plugin.getDataBase().isEnable() && !(this.storage instanceof RegionStorageConf)) {
			this.storage = new RegionStorageConf(this.plugin, this);
		}
		
		this.start();
		this.rebuild();
	}
	
	public void start() {
		this.plugin.getELogger().info("Loading region for world '" + this.world.getName() + "' ...");
		
		this.regionsIdentifier.clear();
		this.regionsName.clear();
		
		this.storage.getAll().forEach(region -> {
			this.regionsIdentifier.put(region.getId(), region);
			this.regionsName.put(region.getName().toLowerCase(), region);
		});
		
		this.plugin.getELogger().info("Loading " + this.regionsIdentifier.size() + " region(s) for world '" + this.world.getName() + "'.");
	}
	
	public void stop() {
		this.plugin.getELogger().info("Region data changes made in '" + this.world.getName() + "' have been background saved.");
	}
	
	public Set<ProtectedRegion> getAll() {
		return ImmutableSet.copyOf(this.regionsIdentifier.values());
	}
	
	public RegionStorage getStorage() {
		return this.storage;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public void rebuild() {
		this.cache.values().forEach(region -> {
			Vector3i position = region.getPosition();
			EWChunck chunck = new EWChunck(this.plugin, position, this.regionsIdentifier);
			this.cache.put(position.getX(), position.getZ(), chunck);
		});
	}
	
	/*
	 * Chunk
	 */
	public EWChunck getChunk(final Vector3i chunk) {
		return this.getChunk(chunk.getX(), chunk.getZ());
	}
	
	public EWChunck getChunk(final int x, final int z) {
		EWChunck value = this.cache.get(x, z);
		if (value == null) {
			value = new EWChunck(this.plugin, Vector3i.from(x, 0, z), this.regionsIdentifier);
		}
		return value;
	}
	
	public EWChunck loadChunk(final Vector3i chunk) {
		EWChunck value = this.cache.get(chunk.getX(), chunk.getZ());
		if (value == null) {
			value = new EWChunck(this.plugin, chunk, this.regionsIdentifier);
			this.cache.put(chunk.getX(), chunk.getZ(), value);
		}
		return value;
	}
	
	public boolean unLoadChunk(final int x, final int z) {
		return this.cache.remove(x, z) != null;
	}
	
	/*
	 * Block
	 */
	
	public ESetProtectedRegion getRegions(final Vector3i position) {
		return this.getChunk(position.getX() >> UtilsChunk.CHUNK_SHIFTS, position.getZ() >> UtilsChunk.CHUNK_SHIFTS).getRegion(position);
	}

	/*
	 * Region
	 */
	
	@Override
	public Optional<ProtectedRegion> getRegion(String name) {
		Preconditions.checkNotNull(name, "name");
		
		ProtectedRegion region = this.regionsName.get(name.toLowerCase());
		if (region == null) {
			try {
				region = this.regionsIdentifier.get(UUID.fromString(name));
			} catch (IllegalArgumentException e) {}
		}
		return Optional.ofNullable(region);
	}
	
	@Override
	public Optional<ProtectedRegion> getRegion(UUID identifier) {
		Preconditions.checkNotNull(identifier, "identifier");
		
		return Optional.ofNullable(this.regionsIdentifier.get(identifier));
	}

	@Override
	public ProtectedRegion.Cuboid createRegionCuboid(String name, Vector3i pos1, Vector3i pos2, Set<UUID> owner_players, Set<String> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(pos1, "pos1");
		Preconditions.checkNotNull(pos2, "pos2");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedCuboidRegion region = new EProtectedCuboidRegion(this, uuid, name, pos1, pos2);
		this.regionsIdentifier.put(uuid, region);
		this.regionsName.put(name.toLowerCase(), region);
		
		region.addPlayerOwner(owner_players);
		region.addGroupOwner(owner_groups);

		this.getStorage().add(region);
		
		this.rebuild();
		return region;
	}

	@Override
	public ProtectedRegion.Polygonal createRegionPolygonal(String name, List<Vector3i> positions, Set<UUID> owner_players, Set<String> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedPolygonalRegion region = new EProtectedPolygonalRegion(this, uuid, name, positions);
		this.regionsIdentifier.put(uuid, region);
		this.regionsName.put(name.toLowerCase(), region);
		
		this.regionsIdentifier.put(uuid, region);
		this.regionsName.put(name.toLowerCase(), region);

		this.getStorage().add(region);
		
		this.rebuild();
		return region;
	}

	@Override
	public ProtectedRegion.Template createRegionTemplate(String name, Set<UUID> owner_players, Set<String> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedTemplateRegion region = new EProtectedTemplateRegion(this, uuid, name);
		this.regionsIdentifier.put(uuid, region);
		this.regionsName.put(name.toLowerCase(), region);
		
		this.regionsIdentifier.put(uuid, region);
		this.regionsName.put(name.toLowerCase(), region);
		
		this.getStorage().add(region);
		
		this.rebuild();
		return region;
	}
	
	@Override
	public Optional<ProtectedRegion> removeRegion(UUID identifier, ProtectedRegion.RemoveType type) {
		EProtectedRegion region = this.regionsIdentifier.get(identifier);
		if (region == null) {
			return Optional.empty();
		}
		
		if (type.equals(ProtectedRegion.RemoveType.REMOVE_CHILDREN)) {
			this.removeRegionChildren(region);
		} else if (type.equals(ProtectedRegion.RemoveType.UNSET_PARENT_IN_CHILDREN)) {
			this.regionsIdentifier.remove(region.getId());
			this.regionsName.remove(region.getName().toLowerCase());
			
			for (EProtectedRegion children : this.regionsIdentifier.values()) {
				Optional<ProtectedRegion> parent = children.getParent();
				if (parent.isPresent() && parent.get().equals(region)) {
					children.clearParent();
				}
			}
		}

		this.getStorage().remove(region);
		// TODO save
		
		this.rebuild();
		return Optional.of(region);
	}
	
	private void removeRegionChildren(EProtectedRegion region) {
		this.regionsIdentifier.remove(region.getId());
		this.regionsName.remove(region.getName().toLowerCase());
		
		for (EProtectedRegion children : this.regionsIdentifier.values()) {
			Optional<ProtectedRegion> parent = children.getParent();
			if (parent.isPresent() && parent.get().equals(region)) {
				if (children.getType().equals(ProtectedRegion.Type.GLOBAL)) {
					children.clearParent();
				} else {
					this.removeRegionChildren(children);
				}
			}
		}
	}

	public boolean rename(EProtectedRegion region, String name) {
		if (this.regionsName.containsKey(name)) return false;
		
		this.regionsName.remove(region.getName().toLowerCase());
		this.regionsName.put(name.toLowerCase(), region);
		return true;
	}
	
	public UUID nextUUID() {
		UUID uuid = null;
		do {
			uuid = UUID.randomUUID();
		} while (this.regionsIdentifier.containsKey(uuid));
		return uuid;
	}
}
