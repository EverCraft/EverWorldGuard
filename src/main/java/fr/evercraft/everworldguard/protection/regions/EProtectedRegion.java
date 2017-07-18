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
package fr.evercraft.everworldguard.protection.regions;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.Domain;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.protection.flag.EFlagValue;
import fr.evercraft.everworldguard.protection.index.EWWorld;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class EProtectedRegion implements ProtectedRegion {
	
	// MultiThreading
	private final ReadWriteLock lock;
	private final Lock write_lock;
	private final Lock read_lock;

	private final EWWorld world;
	
	private UUID identifier;
	private String name;
	
	private final boolean transientRegion;
	protected Vector3i min;
	protected Vector3i max;
	private int priority = 0;
	private ProtectedRegion parent;
	
	private final EDomain owners;
	private final EDomain members;
	
	private final ConcurrentMap<Flag<?>, EFlagValue<?>> flags;
	
	public EProtectedRegion(final EWWorld world, final UUID identifier, final String name, boolean transientRegion) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(identifier);
		Preconditions.checkNotNull(name);
		
		// MultiThreading
		this.lock = new ReentrantReadWriteLock();
		this.write_lock = this.lock.writeLock();
		this.read_lock = this.lock.readLock();

		this.world = world;
		this.identifier = identifier;
		this.name = name;
		this.owners = new EDomain();
		this.members = new EDomain();
		
		this.flags = new ConcurrentHashMap<Flag<?>, EFlagValue<?>>();
		
		this.transientRegion = transientRegion;
	}
	
	public void init(final EProtectedRegion parent) {
		this.parent = parent;
	}
	
	public void init(int priority, final Set<UUID> owners, final Set<String> group_owners, 
			final Set<UUID> members, final Set<String> group_members, final Map<Flag<?>, EFlagValue<?>> flags) {
		this.flags.clear();
		
		this.priority = priority;
		this.owners.init(owners, group_owners);
		this.members.init(members, group_members);
		this.flags.putAll(flags);
	}
	
	/*
	 * Setters
	 */
	
	protected void setMinMaxPoints(final List<Vector3i> points) {
		int minX = points.get(0).getX();
		int minY = points.get(0).getY();
		int minZ = points.get(0).getZ();
		int maxX = minX;
		int maxY = minY;
		int maxZ = minZ;

		for (Vector3i v : points) {
			int x = v.getX();
			int y = v.getY();
			int z = v.getZ();

			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (z < minZ) minZ = z;

			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
			if (z > maxZ) maxZ = z;
		}
		
		this.min = new Vector3i(minX, minY, minZ);
		this.max = new Vector3i(maxX, maxY, maxZ);
	}
	
	@Override
	public CompletableFuture<Boolean> setName(final String name) throws RegionIdentifierException {
		Preconditions.checkNotNull(name, "name");
		
		if (this.name.equals(name)) return CompletableFuture.completedFuture(false);
		if (!this.world.rename(this, name)) throw new RegionIdentifierException();

		return this.world.getStorage().setName(this, name)
			.thenApply(result -> {
				if (!result) return false;
				
				this.write_lock.lock();
				try {
					this.name = name;
				} finally {
					this.write_lock.unlock();
				}
				return true;
			});
	}
	
	@Override
	public CompletableFuture<Boolean> setPriority(int priority) {
		if (this.priority == priority) CompletableFuture.completedFuture(false);
		
		return this.world.getStorage().setPriority(this, priority)
			.thenApply(result -> {
				if (!result) return false;
				
				this.write_lock.lock();
				try {
					this.priority = priority;
				} finally {
					this.write_lock.unlock();
				}
				return true;
			});

	}
	
	public void clearParent(boolean value) {
		if (value) {
			this.clearParent();
			return;
		}
		
		this.parent = null;
	}
	
	@Override
	public CompletableFuture<Boolean> clearParent() {
		if (this.parent == null) CompletableFuture.completedFuture(true);
		
		return this.world.getStorage().setParent(this, null)
			.thenApply(result -> {
				if (!result) return false;
				
				this.write_lock.lock();
				try {
					this.parent = null;
				} finally {
					this.write_lock.unlock();
				}
				return true;
			});
	}
	
	@Override
	public CompletableFuture<Boolean> setParent(final ProtectedRegion parent) throws CircularInheritanceException {
		Preconditions.checkNotNull(parent, "parent");
		
		if (parent.equals(this.parent)) CompletableFuture.completedFuture(true);
		if (parent == this) throw new CircularInheritanceException();

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent == this) throw new CircularInheritanceException();
			curParent = curParent.getParent().orElse(null);
		}

		return this.world.getStorage().setParent(this, parent)
			.thenApply(result -> {
				if (!result) return false;
				
				this.write_lock.lock();
				try {
					this.parent = parent;
				} finally {
					this.write_lock.unlock();
				}
				return true;
			});
	}
	
	@Override
	public CompletableFuture<Set<UUID>> addPlayerOwner(final Set<UUID> players) {
		Preconditions.checkNotNull(players, "players");
		
		Set<UUID> difference = Sets.difference(players, this.getOwners().getPlayers());
		if (difference.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().addOwnerPlayer(this, difference)
			.thenApply(value -> {
				if (!value) return null;
				
				this.write_lock.lock();
				try {
					difference.forEach(player -> this.owners.addPlayer(player));
				} finally {
					this.write_lock.unlock();
				}
				return difference;
			});
	}

	@Override
	public CompletableFuture<Set<UUID>> removePlayerOwner(final Set<UUID> players) {
		Preconditions.checkNotNull(players, "players");
		
		Set<UUID> intersection = Sets.intersection(players, this.getOwners().getPlayers());
		if (intersection.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().removeOwnerPlayer(this, intersection)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					intersection.forEach(player -> this.owners.removePlayer(player));
				} finally {
					this.write_lock.unlock();
				}
				return intersection;
			});
	}
	
	@Override
	public CompletableFuture<Set<String>> addGroupOwner(final Set<String> groups) {
		Preconditions.checkNotNull(groups, "groups");
		
		Set<String> difference = Sets.difference(groups, this.getOwners().getGroups());
		if (difference.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().addOwnerGroup(this, difference)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					difference.forEach(subject -> this.owners.addGroup(subject));
				} finally {
					this.write_lock.unlock();
				}
				return difference;
			});
	}

	@Override
	public CompletableFuture<Set<String>> removeGroupOwner(final Set<String> groups) {
		Preconditions.checkNotNull(groups, "groups");
		
		Set<String> intersection = Sets.intersection(groups, this.getOwners().getGroups());
		if (intersection.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().removeOwnerGroup(this, intersection)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					intersection.forEach(subject -> this.owners.removeGroup(subject));
				} finally {
					this.write_lock.unlock();
				}
				return intersection;
			});
	}
	
	@Override
	public CompletableFuture<Set<UUID>> addPlayerMember(final Set<UUID> players) {
		Preconditions.checkNotNull(players, "players");
		
		Set<UUID> difference = Sets.difference(players, this.getMembers().getPlayers());
		if (difference.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().addMemberPlayer(this, difference)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					difference.forEach(player -> this.members.addPlayer(player));
				} finally {
					this.write_lock.unlock();
				}
				return difference;
			});
	}

	@Override
	public CompletableFuture<Set<UUID>> removePlayerMember(final Set<UUID> players) {
		Preconditions.checkNotNull(players, "players");
		
		Set<UUID> intersection = Sets.intersection(players, this.getMembers().getPlayers());
		if (intersection.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().removeMemberPlayer(this, intersection)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					intersection.forEach(player -> this.members.removePlayer(player));
				} finally {
					this.write_lock.unlock();
				}
				return intersection;
			});
	}
	
	@Override
	public CompletableFuture<Set<String>> addGroupMember(final Set<String> groups) {
		Preconditions.checkNotNull(groups, "groups");
		
		Set<String> difference = Sets.difference(groups, this.getMembers().getGroups());
		if (difference.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().addMemberGroup(this, difference)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					difference.forEach(subject -> this.members.addGroup(subject));
				} finally {
					this.write_lock.unlock();
				}
				return difference;
			});
	}

	@Override
	public CompletableFuture<Set<String>> removeGroupMember(final Set<String> groups) {
		Preconditions.checkNotNull(groups, "groups");
		
		Set<String> intersection = Sets.intersection(groups, this.getMembers().getGroups());
		if (intersection.isEmpty()) return CompletableFuture.completedFuture(ImmutableSet.of());
		
		return this.world.getStorage().removeMemberGroup(this, intersection)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					intersection.forEach(subject -> this.members.removeGroup(subject));
				} finally {
					this.write_lock.unlock();
				}
				return intersection;
			});
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V> CompletableFuture<Boolean> setFlag(final Flag<V> flag, final Group group, final @Nullable V value) {
		Preconditions.checkNotNull(flag, "flag");
		Preconditions.checkNotNull(group, "group");
		
		if (this.flags.get(flag) == null && value == null) return CompletableFuture.completedFuture(true);
		
		return this.world.getStorage().setFlag(this, flag, group, value)
			.thenApply(result -> {
				if (!result) return false;
				
				this.write_lock.lock();
				try {
					EFlagValue<V> flag_value = (EFlagValue) this.flags.get(flag);
					if (flag_value == null) {	
						flag_value = new EFlagValue<V>();
						flag_value.set(group, value);
						this.flags.put(flag, flag_value);
					} else {
						flag_value.set(group, value);
						if (flag_value.isEmpty()) {
							this.flags.remove(flag);
						}
					}
				} finally {
					this.write_lock.unlock();
				}
				return true;
			});
	}
	
	
	@Override
	public CompletableFuture<ProtectedRegion.Cuboid> redefineCuboid(final Vector3i pos1, final Vector3i pos2) {
		Preconditions.checkNotNull(pos1, "pos1");
		Preconditions.checkNotNull(pos2, "pos2");
		
		EProtectedCuboidRegion newRegion = new EProtectedCuboidRegion(this.world, this.identifier, this.name, pos1, pos2, this.transientRegion);
		newRegion.init((EProtectedRegion) this.parent);
		newRegion.init(this.priority, this.owners.getPlayers(), this.owners.getGroups(), this.members.getPlayers(), this.members.getGroups(), this.flags);

		return this.world.getStorage().redefine(this, newRegion)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					this.world.redefine(this.identifier, newRegion);
				} finally {
					this.write_lock.unlock();
				}
				return newRegion;
			});
	}

	@Override
	public CompletableFuture<ProtectedRegion.Polygonal> redefinePolygonal(final List<Vector3i> positions) {
		Preconditions.checkNotNull(positions, "positions");
		
		EProtectedPolygonalRegion newRegion = new EProtectedPolygonalRegion(this.world, this.identifier, this.name, positions, this.transientRegion);
		newRegion.init((EProtectedRegion) this.parent);
		newRegion.init(this.priority, this.owners.getPlayers(), this.owners.getGroups(), this.members.getPlayers(), this.members.getGroups(), this.flags);

		return this.world.getStorage().redefine(this, newRegion)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					this.world.redefine(this.identifier, newRegion);
				} finally {
					this.write_lock.unlock();
				}
				return newRegion;
			});
	}

	@Override
	public CompletableFuture<ProtectedRegion.Template> redefineTemplate() {
		EProtectedTemplateRegion newRegion = new EProtectedTemplateRegion(this.world, this.identifier, this.name, this.transientRegion);
		newRegion.init((EProtectedRegion) this.parent);
		newRegion.init(this.priority, this.owners.getPlayers(), this.owners.getGroups(), this.members.getPlayers(), this.members.getGroups(), this.flags);

		return this.world.getStorage().redefine(this, newRegion)
			.thenApply(result -> {
				if (!result) return null;
				
				this.write_lock.lock();
				try {
					this.world.redefine(this.identifier, newRegion);
				} finally {
					this.write_lock.unlock();
				}
				return newRegion;
			});
	}
	
	/*
	 * Getters
	 */
	
	@Override
	public UUID getId() {
		return this.identifier;
	}
	
	@Override
	public String getName() {
		this.read_lock.lock();
		try {
			return this.name;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean isTransient() {
		return this.transientRegion;
	}	
	
	@Override
	public Vector3i getMinimumPoint() {
		return this.min;
	}
	
	@Override
	public Vector3i getMaximumPoint() {
		return this.max;
	}
	
	@Override
	public int getPriority() {
		this.read_lock.lock();
		try {
			return this.priority;
		} finally {
			this.read_lock.unlock();
		}
	}

	@Override
	public Optional<ProtectedRegion> getParent() {
		this.read_lock.lock();
		try {
			return Optional.ofNullable(this.parent);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public List<ProtectedRegion> getHeritage() throws CircularInheritanceException {
		if (this.parent == null) return ImmutableList.of();
		
		this.read_lock.lock();
		try {
			Builder<ProtectedRegion> parents = ImmutableList.builder();
			
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent == this) throw new CircularInheritanceException();
				
				parents.add(curParent);
				curParent = curParent.getParent().orElse(null);
			}
			
			this.read_lock.lock();
			return parents.build();
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public Domain getOwners() {
		return this.owners;
	}
	
	@Override
	public boolean isPlayerOwner(final User player, final Set<Context> contexts) {
		Preconditions.checkNotNull(player, "player");
		Preconditions.checkNotNull(contexts, "contexts");

		this.read_lock.lock();
		try {
			if (this.owners.contains(player, contexts)) return true;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getOwners().contains(player)) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean isGroupOwner(final Subject group) {
		Preconditions.checkNotNull(group, "group");

		this.read_lock.lock();
		try {
			if (this.owners.containsGroup(group.getIdentifier())) return true;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getOwners().containsGroup(group.getIdentifier())) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public Domain getMembers() {
		return this.members;
	}
	
	@Override
	public boolean isPlayerMember(final User player, final Set<Context> contexts) {
		Preconditions.checkNotNull(player, "player");
		Preconditions.checkNotNull(contexts, "contexts");
		
		this.read_lock.lock();
		try {
			if (this.members.contains(player, contexts)) return true;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getMembers().contains(player, contexts)) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}

	@Override
	public boolean isGroupMember(final Subject group) {
		Preconditions.checkNotNull(group);

		this.read_lock.lock();
		try {
			if (this.members.containsGroup(group.getIdentifier())) return true;
			
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getMembers().containsGroup(group.getIdentifier())) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean hasMembersOrOwners() {
		this.read_lock.lock();
		try {
			return this.owners.size() > 0 || this.members.size() > 0;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean isOwnerOrMember(final User player, final Set<Context> contexts) {
		Preconditions.checkNotNull(player, "player");
		Preconditions.checkNotNull(contexts, "contexts");

		this.read_lock.lock();
		try {
			if (this.owners.contains(player)) return true;
			if (this.members.contains(player)) return true;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getOwners().contains(player)) return true;
				if (curParent.getMembers().contains(player)) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean isOwnerOrMember(final Subject group) {
		Preconditions.checkNotNull(group, "group");

		this.read_lock.lock();
		try {
			if (this.owners.containsGroup(group.getIdentifier())) return true;
			if (this.members.containsGroup(group.getIdentifier())) return true;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				if (curParent.getOwners().containsGroup(group.getIdentifier())) return true;
				if (curParent.getMembers().containsGroup(group.getIdentifier())) return true;
	
				curParent = curParent.getParent().orElse(null);
			}
			return false;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	public ProtectedRegion.Group getGroup(final User subject, final Set<Context> contexts) {
		this.read_lock.lock();
		try {
			if (this.isPlayerOwner(subject, contexts)) return ProtectedRegion.Groups.OWNER;
			if (this.isPlayerMember(subject, contexts)) return ProtectedRegion.Groups.MEMBER;
			return ProtectedRegion.Groups.DEFAULT;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <V> FlagValue<V> getFlag(final Flag<V> flag) {
		Preconditions.checkNotNull(flag);

		this.read_lock.lock();
		try {
			FlagValue<?> value = this.flags.get(flag);
			
			if (value == null) return FlagValue.empty();
			return (FlagValue) value;
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public <V> Optional<V> getFlagInherit(final Flag<V> flag, final Group group) {
		Preconditions.checkNotNull(flag);

		this.read_lock.lock();
		try {
			Optional<V> value = this.getFlag(flag).getInherit(group);
			if (value.isPresent()) return value;
	
			ProtectedRegion curParent = this.parent;
			while (curParent != null) {
				value = curParent.getFlag(flag).getInherit(group);
				if (value.isPresent()) return value;
				
				curParent = curParent.getParent().orElse(null);
			}
			return Optional.empty();
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public Map<Flag<?>, FlagValue<?>> getFlags() {
		this.read_lock.lock();
		try {
			return ImmutableMap.copyOf(this.flags);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	/*
	 * Contains
	 */
	
	@Override
	public boolean containsPosition(int x, int y, int z) {
		return this.containsPosition(new Vector3i(x, y, z));
	}
	
	@Override
	public boolean containsAnyPosition(final List<Vector3i> positions) {
		Preconditions.checkNotNull(positions, "positions");

		for (Vector3i position : positions) {
			if (this.containsPosition(position)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsChunck(final Vector3i position) {
		Preconditions.checkNotNull(position, "position");
		
		Vector3i min = position.mul(16);
		min = Vector3i.from(min.getX(), 0, min.getZ());
		Vector3i max = position.add(1, 0, 1).mul(16);
		max = Vector3i.from(max.getX(), Integer.MAX_VALUE, max.getZ());	
		
		return !this.getIntersecting(new EProtectedCuboidRegion(this.world, UUID.randomUUID(), "_", min , max, true)).isEmpty();
	}
	
	@Override
	public List<ProtectedRegion> getIntersecting(final ProtectedRegion region) {
		Preconditions.checkNotNull(region, "region");
		
		return this.getIntersectingRegions(Arrays.asList(region));
	}

	@Override
	public List<ProtectedRegion> getIntersectingRegions(final Collection<ProtectedRegion> regions) {
		Preconditions.checkNotNull(regions, "regions");

		Optional<Area> optThisArea = this.toArea();
		if (!optThisArea.isPresent()) {
			return Arrays.asList();
		}
		
		Area thisArea = optThisArea.get();
		Builder<ProtectedRegion> intersecting = ImmutableList.builder();
		
		for (ProtectedRegion region : regions) {
			if (!region.isPhysicalArea()) continue;

			if (this.intersects(region, thisArea)) {
				intersecting.add(region);
			}
		}

		return intersecting.build();
	}
	
	protected boolean intersects(final ProtectedRegion region, final Area thisArea) {
		if (this.intersectsBoundingBox(region)) {
			Optional<Area> testArea = region.toArea();
			if (testArea.isPresent()) {
				testArea.get().intersect(thisArea);
				return !testArea.get().isEmpty();
			}
		}
		return false;
	}
	
	protected boolean intersectsBoundingBox(final ProtectedRegion region) {
		Vector3i rMaxPoint = region.getMaximumPoint();
		Vector3i min = this.getMinimumPoint();

		if (rMaxPoint.getX() < min.getX()) return false;
		if (rMaxPoint.getY() < min.getY()) return false;
		if (rMaxPoint.getZ() < min.getZ()) return false;

		Vector3i rMinPoint = region.getMinimumPoint();
		Vector3i max = this.getMaximumPoint();

		if (rMinPoint.getX() > max.getX()) return false;
		if (rMinPoint.getY() > max.getY()) return false;
		if (rMinPoint.getZ() > max.getZ()) return false;

		return true;
	}
	
	protected boolean intersectsEdges(final ProtectedRegion region) {
        List<Vector3i> pos1 = getPoints();
        List<Vector3i> pos2 = region.getPoints();
        Vector3i lastPos1 = pos1.get(pos1.size() - 1);
        Vector3i lastPos2 = pos2.get(pos2.size() - 1);
        for (Vector3i aPos1 : pos1) {
            for (Vector3i aPos2 : pos2) {

                Line2D line1 = new Line2D.Double(
                        lastPos1.getX(),
                        lastPos1.getZ(),
                        aPos1.getX(),
                        aPos1.getZ());

                if (line1.intersectsLine(
                        lastPos2.getX(),
                        lastPos2.getZ(),
                        aPos2.getX(),
                        aPos2.getZ())) {
                    return true;
                }
                lastPos2 = aPos2;
            }
            lastPos1 = aPos1;
        }
        return false;
    }
	
	/*
	 * Java
	 */
	
	@Override
	public int compareTo(final ProtectedRegion other) {
		Preconditions.checkNotNull(other, "other");
		
		if (this.getPriority() > other.getPriority()) {
			return -1;
		} else if (this.getPriority() < other.getPriority()) {
			return 1;
		} else if (other.getType().equals(Types.GLOBAL)) {
			return -1;
		} else if (this.getType().equals(Types.GLOBAL)) {
			return 1;
		}
		
		return this.getName().compareTo(other.getName());
	}

	@Override
	public int hashCode(){
		return this.identifier.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EProtectedRegion)) return false;

		EProtectedRegion other = (EProtectedRegion) obj;
		return other.getName().equals(getName());
	}

	@Override
	public String toString() {
		return "ProtectedRegion [id=" + this.identifier + ", type=" + this.getType().getId() + ", transient=" + this.transientRegion
				+ ", priority=" + this.priority + ", owners=" + this.owners + ", members=" + this.members + "]";
	}
}
