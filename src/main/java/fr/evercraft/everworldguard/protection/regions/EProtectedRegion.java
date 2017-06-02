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
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.java.UtilsString;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.Domain;
import fr.evercraft.everworldguard.protection.domains.EDomain;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class EProtectedRegion implements ProtectedRegion {

	private final EWWorld world;
	
	private String identifier;
	private final boolean transientRegion;
	protected Vector3i min;
	protected Vector3i max;
	private int priority = 0;
	private ProtectedRegion parent;
	
	private final EDomain owners;
	private final EDomain members;
	
	private final ConcurrentMap<Flag<?>, EFlagValue<?>> flags;
	
	public EProtectedRegion(EWWorld world, String identifier, boolean transientRegion) throws RegionIdentifierException {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(identifier);
		if (!ProtectedRegion.isValidId(identifier)) throw new RegionIdentifierException();

		this.world = world;
		this.identifier = UtilsString.normalize(identifier);
		this.owners = new EDomain();
		this.members = new EDomain();
		
		this.flags = new ConcurrentHashMap<Flag<?>, EFlagValue<?>>();
		
		this.transientRegion = transientRegion;
	}
	
	public void init(EProtectedRegion parent) {
		this.parent = parent;
	}
	
	public void init(int priority, Set<UUID> owners, Set<String> group_owners, 
			Set<UUID> members, Set<String> group_members, Map<Flag<?>, EFlagValue<?>> flags) {
		this.flags.clear();
		
		this.priority = priority;
		this.owners.init(owners, group_owners);
		this.members.init(members, group_members);
		this.flags.putAll(flags);
	}
	
	/*
	 * Abstract
	 */
	
	@Override
	public abstract int getVolume();
	
	@Override
	public abstract List<Vector3i> getPoints();
	
	@Override
	public abstract boolean containsPosition(Vector3i pos);
	
	@Override
	public abstract Optional<Area> toArea();
	
	@Override
	public abstract boolean isPhysicalArea();
	
	/*
	 * Setters
	 */
	
	@Override
	public void setName(String identifier) throws RegionIdentifierException {
		if (this.identifier.equals(identifier)) return;
		if (!ProtectedRegion.isValidId(identifier)) throw new RegionIdentifierException();
		if (!this.world.setIdentifier(this, identifier)) throw new RegionIdentifierException();
		
		if (!this.isTransient()) this.world.getStorage().setIdentifier(this, identifier);
		this.identifier = identifier;
	}
	
	protected void setMinMaxPoints(List<Vector3i> points) {
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
	public void setPriority(int priority) {
		if (this.priority == priority) return;
		
		if (!this.isTransient()) this.world.getStorage().setPriority(this, priority);
		this.priority = priority;

	}
	
	@Override
	public void clearParent() {
		if (this.parent == null) return;
		
		if (!this.isTransient()) this.world.getStorage().setParent(this, null);
		this.parent = null;
	}
	
	@Override
	public void setParent(@Nullable ProtectedRegion parent) throws CircularInheritanceException {
		if (parent == null) this.clearParent();
		if (parent.equals(this.parent)) return;
		if (parent == this) throw new CircularInheritanceException();

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent == this) throw new CircularInheritanceException();
			curParent = curParent.getParent().orElse(null);
		}

		if (!this.isTransient()) this.world.getStorage().setParent(this, parent);
		this.parent = parent;
	}
	
	@Override
	public Set<UUID> addPlayerOwner(Set<UUID> players) {
		Set<UUID> difference = Sets.difference(players, this.getOwners().getPlayers());
		if (difference.isEmpty()) return difference;
		
		if (!this.isTransient()) this.world.getStorage().addOwnerPlayer(this, difference);
		difference.forEach(player -> this.owners.addPlayer(player));
		return difference;
	}

	@Override
	public Set<UUID> removePlayerOwner(Set<UUID> players) {
		Set<UUID> intersection = Sets.intersection(players, this.getOwners().getPlayers());
		if (intersection.isEmpty()) return intersection;
		
		if (!this.isTransient()) this.world.getStorage().removeOwnerPlayer(this, intersection);
		intersection.forEach(player -> this.owners.removePlayer(player));
		return intersection;
	}
	
	@Override
	public Set<String> addGroupOwner(Set<String> groups) {
		Set<String> difference = Sets.difference(groups, this.getOwners().getGroups());
		if (difference.isEmpty()) return difference;
		
		if (!this.isTransient()) this.world.getStorage().addOwnerGroup(this, difference);
		difference.forEach(subject -> this.owners.addGroup(subject));
		return difference;
	}

	@Override
	public Set<String> removeGroupOwner(Set<String> groups) {
		Set<String> intersection = Sets.intersection(groups, this.getOwners().getGroups());
		if (intersection.isEmpty()) return intersection;
		
		if (!this.isTransient()) this.world.getStorage().removeOwnerGroup(this, intersection);
		intersection.forEach(subject -> this.owners.removeGroup(subject));
		return intersection;
	}
	
	@Override
	public Set<UUID> addPlayerMember(Set<UUID> players) {
		Set<UUID> difference = Sets.difference(players, this.getMembers().getPlayers());
		if (difference.isEmpty()) return difference;
		
		if (!this.isTransient()) this.world.getStorage().addMemberPlayer(this, difference);
		difference.forEach(player -> this.members.addPlayer(player));
		return difference;
	}

	@Override
	public Set<UUID> removePlayerMember(Set<UUID> players) {
		Set<UUID> intersection = Sets.intersection(players, this.getMembers().getPlayers());
		if (intersection.isEmpty()) return intersection;
		
		if (!this.isTransient()) this.world.getStorage().removeMemberPlayer(this, intersection);
		intersection.forEach(player -> this.members.removePlayer(player));
		return intersection;
	}
	
	@Override
	public Set<String> addGroupMember(Set<String> groups) {
		Set<String> difference = Sets.difference(groups, this.getMembers().getGroups());
		if (difference.isEmpty()) return difference;
		
		if (!this.isTransient()) this.world.getStorage().addMemberGroup(this, difference);
		difference.forEach(subject -> this.members.addGroup(subject));
		return difference;
	}

	@Override
	public Set<String> removeGroupMember(Set<String> groups) {
		Set<String> intersection = Sets.intersection(groups, this.getMembers().getGroups());
		if (intersection.isEmpty()) return intersection;
		
		if (!this.isTransient()) this.world.getStorage().removeMemberGroup(this, intersection);
		intersection.forEach(subject -> this.members.removeGroup(subject));
		return intersection;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <V> void setFlag(Flag<V> flag, Group group, @Nullable V value) {
		Preconditions.checkNotNull(flag);
		
		EFlagValue<V> flag_value = (EFlagValue) this.flags.get(flag);
		if (flag_value == null && value == null) return;
		
		if (!this.isTransient()) this.world.getStorage().setFlag(this, flag, group, value);
		
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
	}
	
	
	@Override
	public Optional<ProtectedRegion.Cuboid> redefineCuboid(Vector3i pos1, Vector3i pos2) {
		// TODO
		return Optional.empty();
	}

	@Override
	public Optional<ProtectedRegion.Polygonal> redefinePolygonal(List<Vector3i> positions) {
		// TODO 
		return Optional.empty();
	}

	@Override
	public Optional<ProtectedRegion.Template> redefineTemplate() {
		// TODO 
		return Optional.empty();
	}
	/*
	 * Getters
	 */
	
	@Override
	public String getName() {
		return this.identifier;
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
		return this.priority;
	}

	@Override
	public Optional<ProtectedRegion> getParent() {
		return Optional.ofNullable(this.parent);
	}
	
	@Override
	public List<ProtectedRegion> getHeritage() throws CircularInheritanceException {
		if (this.parent == null) return ImmutableList.of();
		
		Builder<ProtectedRegion> parents = ImmutableList.builder();
		
		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent == this) throw new CircularInheritanceException();
			
			parents.add(curParent);
			curParent = curParent.getParent().orElse(null);
		}
		
		return parents.build();
	}
	
	@Override
	public Domain getOwners() {
		return this.owners;
	}
	
	@Override
	public boolean isPlayerOwner(User player, Set<Context> contexts) {
		Preconditions.checkNotNull(player);

		if (this.owners.contains(player, contexts)) return true;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getOwners().contains(player)) return true;

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@Override
	public boolean isGroupOwner(Subject group) {
		Preconditions.checkNotNull(group);

		if (this.owners.containsGroup(group.getIdentifier())) return true;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getOwners().containsGroup(group.getIdentifier())) return true;

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@Override
	public Domain getMembers() {
		return this.members;
	}
	
	@Override
	public boolean isPlayerMember(User player, Set<Context> contexts) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(contexts);

		if (this.members.contains(player, contexts)) return true;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getMembers().contains(player, contexts)) return true;

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}

	@Override
	public boolean isGroupMember(Subject group) {
		Preconditions.checkNotNull(group);

		if (this.members.containsGroup(group.getIdentifier())) return true;
		
		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getMembers().containsGroup(group.getIdentifier())) return true;

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@Override
	public boolean hasMembersOrOwners() {
		return this.owners.size() > 0 || this.members.size() > 0;
	}
	
	@Override
	public boolean isOwnerOrMember(User player, Set<Context> contexts) {
		Preconditions.checkNotNull(player);

		if (this.owners.contains(player)) return true;
		if (this.members.contains(player)) return true;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getOwners().contains(player)) return true;
			if (curParent.getMembers().contains(player)) return true;

			curParent = curParent.getParent().orElse(null);
		}
		return false;
	}
	
	@Override
	public boolean isOwnerOrMember(Subject group) {
		Preconditions.checkNotNull(group);

		if (this.owners.containsGroup(group.getIdentifier())) return true;
		if (this.members.containsGroup(group.getIdentifier())) return true;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getOwners().containsGroup(group.getIdentifier())) return true;
			if (curParent.getMembers().containsGroup(group.getIdentifier())) return true;

			curParent = curParent.getParent().orElse(null);
		}
		return false;
	}
	
	public ProtectedRegion.Group getGroup(User subject, Set<Context> contexts) {
		if (this.isPlayerOwner(subject, contexts)) return ProtectedRegion.Group.OWNER;
		if (this.isPlayerMember(subject, contexts)) return ProtectedRegion.Group.MEMBER;
		
		return ProtectedRegion.Group.DEFAULT;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <V> FlagValue<V> getFlag(Flag<V> flag) {
		Preconditions.checkNotNull(flag);

		FlagValue<?> value = this.flags.get(flag);
		
		if (value == null) return FlagValue.empty();
		return (FlagValue) value;
	}
	
	@Override
	public <V> Optional<V> getFlagInherit(Flag<V> flag, Group group) {
		Preconditions.checkNotNull(flag);

		Optional<V> value = this.getFlag(flag).getInherit(group);
		if (value.isPresent()) return value;

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			value = curParent.getFlag(flag).getInherit(group);
			if (value.isPresent()) return value;
			
			curParent = curParent.getParent().orElse(null);
		}
		
		return Optional.empty();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Map<Flag<?>, FlagValue<?>> getFlags() {
		return (Map) this.flags;
	}
	
	/*
	 * Contains
	 */
	
	@Override
	public boolean containsPosition(int x, int y, int z) {
		return this.containsPosition(new Vector3i(x, y, z));
	}
	
	@Override
	public boolean containsAnyPosition(List<Vector3i> positions) {
		Preconditions.checkNotNull(positions);

		for (Vector3i position : positions) {
			if (this.containsPosition(position)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean containsChunck(Vector3i position) {
		Preconditions.checkNotNull(position);
		
		Vector3i min = position.mul(16);
		min = Vector3i.from(min.getX(), 0, min.getZ());
		Vector3i max = position.add(1, 0, 1).mul(16);
		max = Vector3i.from(max.getX(), Integer.MAX_VALUE, max.getZ());		
		try {
			return !this.getIntersecting(new EProtectedCuboidRegion(this.world, "_", true, min , max)).isEmpty();
		} catch (RegionIdentifierException e) {
			return false;
		}
	}
	
	@Override
	public List<ProtectedRegion> getIntersecting(ProtectedRegion region) {
		Preconditions.checkNotNull(region, "region");
		
		return this.getIntersectingRegions(Arrays.asList(region));
	}

	@Override
	public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
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
	
	protected boolean intersects(ProtectedRegion region, Area thisArea) {
		if (this.intersectsBoundingBox(region)) {
			Optional<Area> testArea = region.toArea();
			if (testArea.isPresent()) {
				testArea.get().intersect(thisArea);
				return !testArea.get().isEmpty();
			}
		}
		return false;
	}
	
	protected boolean intersectsBoundingBox(ProtectedRegion region) {
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
	
	protected boolean intersectsEdges(ProtectedRegion region) {
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
	public int compareTo(ProtectedRegion other) {
		if (this.getPriority() > other.getPriority()) {
			return -1;
		} else if (this.getPriority() < other.getPriority()) {
			return 1;
		} else if (this.getType().equals(Type.GLOBAL)) {
			return 1;
		} else if (this.getType().equals(Type.GLOBAL)) {
			return -1;
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
		return "ProtectedRegion [id=" + this.identifier + ", type=" + this.getType().name() + ", transient=" + this.transientRegion
				+ ", priority=" + this.priority + ", owners=" + this.owners + ", members=" + this.members + "]";
	}
}
