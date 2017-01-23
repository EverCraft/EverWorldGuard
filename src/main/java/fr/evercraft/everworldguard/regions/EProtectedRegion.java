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
package fr.evercraft.everworldguard.regions;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import fr.evercraft.everapi.java.UtilsString;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.regions.Association;
import fr.evercraft.everapi.services.worldguard.regions.Domain;
import fr.evercraft.everapi.services.worldguard.regions.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.RegionType;
import fr.evercraft.everworldguard.domains.EDomain;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.Player;

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

	protected Vector3i min;
	protected Vector3i max;

	private final String id;
	private final boolean transientRegion;
	private int priority = 0;
	private ProtectedRegion parent;
	
	private final EDomain owners;
	private final EDomain members;
	
	private final ConcurrentMap<Flag<?>, FlagValue<?>> flags;
	
	public EProtectedRegion(String id, boolean transientRegion) {
		Preconditions.checkNotNull(id);
		Preconditions.checkArgument(ProtectedRegion.isValidId(id), "Invalid region ID: " + id);

		this.id = UtilsString.normalize(id);
		this.owners = new EDomain();
		this.members = new EDomain();
		
		this.flags = new ConcurrentHashMap<Flag<?>, FlagValue<?>>();
		
		this.transientRegion = transientRegion;
	}
	
	public void init(EProtectedRegion parent) {
		this.parent = parent;
	}
	
	public void init(int priority, Set<UUID> owners, Set<String> group_owners, 
			Set<UUID> members, Set<String> group_members, Map<Flag<?>, FlagValue<?>> flags) {
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
	public abstract RegionType getType();
	
	@Override
	public abstract List<Vector3i> getPoints();
	
	@Override
	public abstract boolean containsPosition(Vector3i pos);
	
	@Override
	public abstract Optional<Area> toArea();
	
	@Override
	public abstract boolean isPhysicalArea();
	
	/*
	 * Accesseurs
	 */
	
	@Override
	public String getIdentifier() {
		return this.id;
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
	public int getPriority() {
		return this.priority;
	}
	
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public Optional<ProtectedRegion> getParent() {
		return Optional.ofNullable(this.parent);
	}
	
	@Override
	public List<ProtectedRegion> getHeritage() throws CircularInheritanceException {
		if (this.parent == null) {
			return ImmutableList.of();
		}
		
		Builder<ProtectedRegion> parents = ImmutableList.builder();
		
		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent == this) {
				throw new CircularInheritanceException();
			}
			parents.add(curParent);
			curParent = curParent.getParent().orElse(null);
		}
		
		return parents.build();
	}
	
	@Override
	public void setParent(@Nullable ProtectedRegion parent) throws CircularInheritanceException {
		if (parent == null) {
			this.parent = null;
			return;
		}

		if (parent == this) {
			throw new CircularInheritanceException();
		}

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent == this) {
				throw new CircularInheritanceException();
			}
			curParent = curParent.getParent().orElse(null);
		}

		this.parent = parent;
	}
	
	@Override
	public Domain getOwners() {
		return this.owners;
	}
	
	@Override
	public Domain getMembers() {
		return this.members;
	}
	
	@Override
	public boolean hasMembersOrOwners() {
		return this.owners.size() > 0 || this.members.size() > 0;
	}
	
	@Override
	public boolean isOwner(Player player) {
		Preconditions.checkNotNull(player);

		if (this.owners.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getOwners().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@Override
	public boolean isMember(Player player) {
		Preconditions.checkNotNull(player);

		if (this.isOwner(player)) {
			return true;
		}

		if (this.members.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getMembers().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@Override
	public boolean isMemberOnly(EPlayer player) {
		Preconditions.checkNotNull(player);

		if (this.members.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.parent;
		while (curParent != null) {
			if (curParent.getMembers().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	/*
	 * Flags
	 */
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T extends Flag<V>, V> FlagValue<V> getFlag(T flag) {
		Preconditions.checkNotNull(flag);

		Object obj = this.flags.get(flag);
		FlagValue<V> value;

		if (obj != null) {
			value = (FlagValue) obj;
		} else {
			return FlagValue.empty();
		}

		return value;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <T extends Flag<V>, V> void setFlag(T flag, Association association, @Nullable V value) {
		Preconditions.checkNotNull(flag);

		if (value == null) {
			this.flags.remove(flag);
		} else {
			FlagValue<V> flag_value = (FlagValue) this.flags.get(flag);
			if (flag_value != null) {
				flag_value.set(association, value);
			} else {
				flag_value = new FlagValue<V>();
				flag_value.set(association, value);
			}
			this.flags.put(flag, flag_value);
		}
	}
	
	@Override
	public Map<Flag<?>, FlagValue<?>> getFlags() {
		return this.flags;
	}
	
	@Override
	public void setFlags(Map<Flag<?>, FlagValue<?>> flags) {
		Preconditions.checkNotNull(flags);
		
		this.flags.clear();
		this.flags.putAll(flags);
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
	public boolean containsChunk(Vector3i position) {
		Preconditions.checkNotNull(position);
		
		return this.containsPosition(new Vector3i(position.getX(), this.getMinimumPoint().getY(), position.getZ()));
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
		}

		return this.getIdentifier().compareTo(other.getIdentifier());
	}

	@Override
	public int hashCode(){
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EProtectedRegion)) {
			return false;
		}

		EProtectedRegion other = (EProtectedRegion) obj;
		return other.getIdentifier().equals(getIdentifier());
	}

	@Override
	public String toString() {
		return "ProtectedRegion [id=" + this.id + ", type=" + this.getType().name() + ", transient=" + this.transientRegion
				+ ", priority=" + this.priority + ", owners=" + this.owners + ", members=" + this.members + "]";
	}
}
