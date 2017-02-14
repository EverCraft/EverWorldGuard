package fr.evercraft.everworldguard.service.index;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.worldguard.WorldWorldGuard;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsChunk;
import fr.evercraft.everapi.util.LongHashTable;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.regions.EProtectedRegion;
import fr.evercraft.everworldguard.regions.EProtectedTemplateRegion;
import fr.evercraft.everworldguard.service.storage.RegionStorage;
import fr.evercraft.everworldguard.service.storage.conf.RegionStorageConf;
import fr.evercraft.everworldguard.service.storage.sql.RegionStorageSql;

public class EWWorld implements WorldWorldGuard {
	
	private final EverWorldGuard plugin;
	
	private RegionStorage storage;
	
	private final ConcurrentHashMap<String, EProtectedRegion> regions;
	private final LongHashTable<EWChunck> cache;
	
	private final World world;
	
	public EWWorld(EverWorldGuard plugin, World world) {
		Preconditions.checkNotNull(plugin, "plugin");
		
		this.plugin = plugin;
		this.world = world;
		this.regions = new ConcurrentHashMap<String, EProtectedRegion>();		
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
		this.plugin.getLogger().info("Loading region for world '" + this.world.getName() + "' ...");
		
		this.regions.clear();
		this.storage.getAll().forEach(region -> this.regions.put(region.getIdentifier().toLowerCase(), region));
		
		this.plugin.getLogger().info("Loading " + this.regions.size() + " region(s) for world '" + this.world.getName() + "'.");
	}
	
	public void stop() {
		this.plugin.getLogger().info("Region data changes made in '" + this.world.getName() + "' have been background saved.");
	}
	
	public Set<ProtectedRegion> getAll() {
		return ImmutableSet.copyOf(this.regions.values());
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
			EWChunck chunck = new EWChunck(this.plugin, position, this.regions);
			if (chunck.getAll().size() >= 2) {
				this.plugin.getLogger().warn("position : " + position + "; size :" + chunck.getAll().size());
			}
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
		this.plugin.getLogger().warn("x : " + x + ";z :"+ z);
		EWChunck value = this.cache.get(x, z);
		if (value == null) {
			value = new EWChunck(this.plugin, Vector3i.from(x, 0, z), this.regions);
		}
		return value;
	}
	
	public EWChunck loadChunk(final Vector3i chunk) {
		EWChunck value = this.cache.get(chunk.getX(), chunk.getZ());
		if (value == null) {
			value = new EWChunck(this.plugin, chunk, this.regions);
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
		this.plugin.getLogger().warn("size : " + this.getChunk(position.getX() >> UtilsChunk.CHUNK_SHIFTS, position.getZ() >> UtilsChunk.CHUNK_SHIFTS).getRegion(position).getAll().size());
		return this.getChunk(position.getX() >> UtilsChunk.CHUNK_SHIFTS, position.getZ() >> UtilsChunk.CHUNK_SHIFTS).getRegion(position);
	}

	/*
	 * Region
	 */
	
	@Override
	public Optional<ProtectedRegion> getRegion(String region_id) {
		Preconditions.checkNotNull(region_id, "region_id");
		
		return Optional.ofNullable(this.regions.get(region_id.toLowerCase()));
	}

	@Override
	public ProtectedRegion.Cuboid createRegionCuboid(String region_id, Vector3i pos1, Vector3i pos2, Set<EUser> owner_players, Set<Subject> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(region_id, "region_id");
		Preconditions.checkNotNull(pos1, "pos1");
		Preconditions.checkNotNull(pos2, "pos2");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regions.containsKey(region_id)) throw new RegionIdentifierException();
		
		EProtectedCuboidRegion region = new EProtectedCuboidRegion(this, region_id, pos1, pos2);
		this.regions.put(region_id.toLowerCase(), region);
		
		try {
			this.getStorage().add(region);
		} catch (StorageException e) {}
		
		this.rebuild();
		return region;
	}

	@Override
	public ProtectedRegion.Polygonal createRegionPolygonal(String region_id, List<Vector3i> positions, Set<EUser> owner_players, Set<Subject> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(region_id, "region_id");
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regions.containsKey(region_id)) throw new RegionIdentifierException();
		
		
		EProtectedPolygonalRegion region = new EProtectedPolygonalRegion(this, region_id, positions);
		this.regions.put(region_id.toLowerCase(), region);
		
		try {
			this.getStorage().add(region);
		} catch (StorageException e) {}
		
		this.rebuild();
		return region;
	}

	@Override
	public ProtectedRegion.Template createRegionTemplate(String region_id, Set<EUser> owner_players, Set<Subject> owner_groups) throws RegionIdentifierException {
		Preconditions.checkNotNull(region_id, "region_id");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regions.containsKey(region_id)) throw new RegionIdentifierException();
		
		EProtectedTemplateRegion region = new EProtectedTemplateRegion(this, region_id);
		this.regions.put(region_id.toLowerCase(), region);
		
		try {
			this.getStorage().add(region);
		} catch (StorageException e) {}
		
		this.rebuild();
		return region;
	}
	
	@Override
	public Optional<ProtectedRegion> removeRegion(String region_id, ProtectedRegion.RemoveType type) {
		EProtectedRegion region = this.regions.get(region_id.toLowerCase());
		if (region == null) {
			return Optional.empty();
		}
		
		if (type.equals(ProtectedRegion.RemoveType.REMOVE_CHILDREN)) {
			this.removeRegionChildren(region);
		} else if (type.equals(ProtectedRegion.RemoveType.UNSET_PARENT_IN_CHILDREN)) {
			this.regions.remove(region_id.toLowerCase());
			
			for (EProtectedRegion children : this.regions.values()) {
				Optional<ProtectedRegion> parent = children.getParent();
				if (parent.isPresent() && parent.get().equals(region)) {
					children.clearParent();
				}
			}
		}
		
		// TODO save
		
		this.rebuild();
		return Optional.of(region);
	}
	
	private void removeRegionChildren(EProtectedRegion region) {
		this.regions.remove(region.getIdentifier());
		
		for (EProtectedRegion children : this.regions.values()) {
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

	public boolean setIdentifier(EProtectedRegion region, String identifier) {
		if (this.regions.containsKey(identifier)) return false;
		
		this.regions.remove(region.getIdentifier().toLowerCase());
		this.regions.put(identifier.toLowerCase(), region);
		return true;
	}
}
