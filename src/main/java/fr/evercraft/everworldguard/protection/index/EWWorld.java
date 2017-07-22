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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.WorldGuardWorld;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.sponge.UtilsChunk;
import fr.evercraft.everapi.util.LongHashTable;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedTemplateRegion;
import fr.evercraft.everworldguard.protection.storage.ManagerRegionStorage;
import fr.evercraft.everworldguard.protection.storage.RegionStorage;

public class EWWorld implements WorldGuardWorld {
	
	// MultiThreading
	private final ReadWriteLock lock;
	private final Lock write_lock;
	private final Lock read_lock;
	
	private final EverWorldGuard plugin;
	
	private final ConcurrentHashMap<UUID, EProtectedRegion> regionsIdentifier;
	private final ConcurrentHashMap<String, EProtectedRegion> regionsName;
	private final LongHashTable<EWChunck> cache;
	
	private final World world;
	private final ManagerRegionStorage storage;
	
	public EWWorld(EverWorldGuard plugin, World world) {		
		this.plugin = plugin;
		this.world = world;
		
		// MultiThreading
		this.lock = new ReentrantReadWriteLock();
		this.write_lock = this.lock.writeLock();
		this.read_lock = this.lock.readLock();
		
		this.regionsIdentifier = new ConcurrentHashMap<UUID, EProtectedRegion>();
		this.regionsName = new ConcurrentHashMap<String, EProtectedRegion>();	
		this.cache = new LongHashTable<EWChunck>();
		
		this.storage = new ManagerRegionStorage(this.plugin, this);
		this.start();
	}

	public void reload() {
		this.write_lock.lock();
		try {
			this.storage.reload();
			
			this.start();
			this.rebuild();
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public void start() {
		this.plugin.getELogger().info("Loading region for world '" + this.world.getName() + "' ...");
		
		this.write_lock.lock();
		try {
			this.regionsIdentifier.clear();
			this.regionsName.clear();
			
			this.storage.getAll().get().forEach(region -> {
				this.regionsIdentifier.put(region.getId(), region);
				this.regionsName.put(region.getName().toLowerCase(), region);
			});
		} catch (InterruptedException | ExecutionException e) {
		} finally {
			this.write_lock.unlock();
		}
		
		this.plugin.getELogger().info("Loading " + this.regionsIdentifier.size() + " region(s) for world '" + this.world.getName() + "'.");
	}
	
	public Set<ProtectedRegion> getAll() {
		this.read_lock.lock();
		try {
			return ImmutableSet.copyOf(this.regionsIdentifier.values());
		} finally {
			this.read_lock.unlock();
		}
	}
	
	public RegionStorage getStorage() {
		return this.storage;
	}
	
	public World getWorld() {
		return this.world;
	}
	
	public UUID getUniqueId() {
		return this.world.getUniqueId();
	}
	
	public void rebuild() {
		this.write_lock.lock();
		try {
			this.cache.values().forEach(region -> {
				Vector3i position = region.getPosition();
				EWChunck chunck = new EWChunck(this.plugin, position, this.regionsIdentifier);
				this.cache.put(position.getX(), position.getZ(), chunck);
			});
			
			this.plugin.getProtectionService().getSubjectList().getAll().stream()
				.filter(subject -> {
					Optional<Location<World>> lastLocation = subject.getLastLocation();
					if (lastLocation.isPresent()) {
						return lastLocation.get().getExtent().equals(this.world);
					}
					return false;
				}).forEach(subject -> subject.rebuild());
		} finally {
			this.write_lock.unlock();
		}
	}
	
	/*
	 * Chunk
	 */
	public EWChunck getChunk(final Vector3i chunk) {
		return this.getChunk(chunk.getX(), chunk.getZ());
	}
	
	public EWChunck getChunk(final int x, final int z) {
		this.read_lock.lock();
		try {
			EWChunck value = this.cache.get(x, z);
			if (value == null) {
				value = new EWChunck(this.plugin, Vector3i.from(x, 0, z), this.regionsIdentifier);
			}
			return value;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	public EWChunck loadChunk(final Vector3i chunk) {
		this.write_lock.lock();
		try {
			EWChunck value = this.cache.get(chunk.getX(), chunk.getZ());
			if (value == null) {
				value = new EWChunck(this.plugin, chunk, this.regionsIdentifier);
				this.cache.put(chunk.getX(), chunk.getZ(), value);
			}
			return value;
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public boolean unLoadChunk(final int x, final int z) {
		this.write_lock.lock();
		try {
			return this.cache.remove(x, z) != null;
		} finally {
			this.write_lock.unlock();
		}
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
	public Optional<ProtectedRegion> getRegion(final String name) {
		Preconditions.checkNotNull(name, "name");
		
		this.read_lock.lock();
		try {
			ProtectedRegion region = this.regionsName.get(name.toLowerCase());
			if (region == null) {
				try {
					region = this.regionsIdentifier.get(UUID.fromString(name));
				} catch (IllegalArgumentException e) {}
			}
			return Optional.ofNullable(region);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public Optional<ProtectedRegion> getRegion(final UUID identifier) {
		Preconditions.checkNotNull(identifier, "identifier");
		
		this.read_lock.lock();
		try {
			return Optional.ofNullable(this.regionsIdentifier.get(identifier));
		} finally {
			this.read_lock.unlock();
		}
	}

	@Override
	public CompletableFuture<ProtectedRegion.Cuboid> createRegionCuboid(final String name, final Vector3i pos1, final Vector3i pos2, 
			final Set<UUID> owner_players, final Set<String> owner_groups, boolean transientRegion) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(pos1, "pos1");
		Preconditions.checkNotNull(pos2, "pos2");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedCuboidRegion region = new EProtectedCuboidRegion(this, uuid, name, pos1, pos2, transientRegion);
		region.init(0, owner_players, owner_groups, ImmutableSet.of(), ImmutableSet.of(), ImmutableMap.of());

		return this.getStorage().add(region)
			.thenApply(value -> {
				this.write_lock.lock();
				try {
					this.regionsIdentifier.put(uuid, region);
					this.regionsName.put(name.toLowerCase(), region);
					
					this.rebuild();
					return region;
				} finally {
					this.write_lock.unlock();
				}
			});
	}

	@Override
	public CompletableFuture<ProtectedRegion.Polygonal> createRegionPolygonal(final String name, final List<Vector3i> positions, 
			final Set<UUID> owner_players, final Set<String> owner_groups, boolean transientRegion) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedPolygonalRegion region = new EProtectedPolygonalRegion(this, uuid, name, positions, transientRegion);
		region.init(0, owner_players, owner_groups, ImmutableSet.of(), ImmutableSet.of(), ImmutableMap.of());
		
		return this.getStorage().add(region)
			.thenApply(value -> {
				this.write_lock.lock();
				try {
					this.regionsIdentifier.put(uuid, region);
					this.regionsName.put(name.toLowerCase(), region);
					
					this.rebuild();
					return region;
				} finally {
					this.write_lock.unlock();
				}
			});
	}

	@Override
	public CompletableFuture<ProtectedRegion.Template> createRegionTemplate(final String name, final Set<UUID> owner_players, 
			final Set<String> owner_groups, boolean transientRegion) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkNotNull(owner_players, "owner_players");
		Preconditions.checkNotNull(owner_groups, "owner_groups");
		if (this.regionsName.containsKey(name)) throw new RegionIdentifierException();
		
		UUID uuid = this.nextUUID();
		EProtectedTemplateRegion region = new EProtectedTemplateRegion(this, uuid, name, transientRegion);
		region.init(0, owner_players, owner_groups, ImmutableSet.of(), ImmutableSet.of(), ImmutableMap.of());
		
		return this.getStorage().add(region)
			.thenApply(value -> {
				this.write_lock.lock();
				try {
					this.regionsIdentifier.put(uuid, region);
					this.regionsName.put(name.toLowerCase(), region);
					
					this.rebuild();
					return region;
				} finally {
					this.write_lock.unlock();
				}
			});
	}
	
	@Override
	public CompletableFuture<Set<ProtectedRegion>> removeRegion(final UUID identifier, final ProtectedRegion.RemoveType type) {
		EProtectedRegion region = null;
		
		this.read_lock.lock();
		try {
			region = this.regionsIdentifier.get(identifier);			
		} finally {
			this.read_lock.unlock();
		}
		
		if (region == null || region.getType().equals(ProtectedRegion.Types.GLOBAL)) {
			return CompletableFuture.completedFuture(ImmutableSet.of());
		}
			
		if (type.equals(ProtectedRegion.RemoveTypes.REMOVE_CHILDREN)) {
			return this.removeRemoveChildren(region)
				.thenApply(regions -> {
					this.write_lock.lock();
					try {
						for (ProtectedRegion children : regions) {
							if (children.getType().equals(ProtectedRegion.Types.GLOBAL)) {
								if (children instanceof EProtectedRegion) {
									((EProtectedRegion) children).clearParent(false);
								} else {
									children.clearParent();
								}
							} else {
								this.regionsIdentifier.remove(children.getId());
								this.regionsName.remove(children.getName().toLowerCase());
							}
						}
						this.rebuild();
					} finally {
						this.write_lock.unlock();
					}
					return regions;
				});
		} else if (type.equals(ProtectedRegion.RemoveTypes.UNSET_PARENT_IN_CHILDREN)) {
			final EProtectedRegion regionRemove = region;
			return this.removeUnsetParentInChildren(regionRemove)
				.thenApply(regions -> {
					this.write_lock.lock();
					try {						
						for (ProtectedRegion children : regions) {
							if (children instanceof EProtectedRegion) {
								((EProtectedRegion) children).clearParent(false);
							} else {
								children.clearParent();
							}
						}
						this.regionsIdentifier.remove(regionRemove.getId());
						this.regionsName.remove(regionRemove.getName().toLowerCase());
						this.rebuild();
					} finally {
						this.write_lock.unlock();
					}
					return regions;
				});
		}
		
		return CompletableFuture.completedFuture(ImmutableSet.of());		
	}
	
	private CompletableFuture<Set<ProtectedRegion>> removeUnsetParentInChildren(final EProtectedRegion region) {
		Set<EProtectedRegion> regions = this.regionsIdentifier.values().stream().filter(children -> {
			Optional<ProtectedRegion> parent = children.getParent();
			return parent.isPresent() && parent.get().equals(region);
		}).collect(Collectors.toSet());
		
		return this.storage.removeClearParent(region, regions)
			.thenApply(value -> {
				if (!value) return ImmutableSet.of();
				return ImmutableSet.of(region);
			});
	}
	
	private CompletableFuture<Set<ProtectedRegion>> removeRemoveChildren(final EProtectedRegion region) {
		Set<EProtectedRegion> regions = new HashSet<EProtectedRegion>();
		this.removeRemoveChildren(region, regions);
		
		return this.storage.removeRemoveChildren(regions)
			.thenApply(value -> {
				if (!value) return ImmutableSet.of();
				return ImmutableSet.copyOf(regions);
			});
	}
	
	private void removeRemoveChildren(final EProtectedRegion region, final Set<EProtectedRegion> regions) {
		regions.add(region);
		
		for (EProtectedRegion children : this.regionsIdentifier.values()) {
			if (!regions.contains(children)) {
				children.getParent().ifPresent(parent -> {
					if (parent.equals(region)) {
						if (region.getType().equals(ProtectedRegion.Types.GLOBAL)) {
							regions.add((EProtectedRegion) parent);
						} else {
							this.removeRemoveChildren(children, regions);
						}
					}
				});
			}
		}
	}

	public boolean rename(final EProtectedRegion region, final String name) {
		this.write_lock.lock();
		try {
			if (this.regionsName.containsKey(name)) return false;
			
			this.regionsName.remove(region.getName().toLowerCase());
			this.regionsName.put(name.toLowerCase(), region);
			return true;
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public boolean redefine(final UUID identifier, final EProtectedRegion region) {
		this.write_lock.lock();
		try {
			this.regionsIdentifier.put(identifier, region);
			this.regionsName.put(region.getName().toLowerCase(), region);
			this.rebuild();
			return true;
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public UUID nextUUID() {
		this.write_lock.lock();
		try {
			UUID uuid = null;
			do {
				uuid = UUID.randomUUID();
			} while (this.regionsIdentifier.containsKey(uuid));
			return uuid;
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public int getRegionMaxRegionCountPerPlayer() {
		return this.plugin.getConfigs().getRegionMaxRegionCountPerPlayer();
	}
}
