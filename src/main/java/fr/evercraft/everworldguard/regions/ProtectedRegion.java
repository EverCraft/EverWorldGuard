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

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import fr.evercraft.everapi.java.UtilsString;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.regions.RegionType;
import fr.evercraft.everworldguard.domains.Association;
import fr.evercraft.everworldguard.domains.EDomain;
import fr.evercraft.everworldguard.flag.FlagValue;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.Player;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public abstract class ProtectedRegion implements Comparable<ProtectedRegion> {

	public static final String GLOBAL_REGION = "__global__";
	private static final Pattern VALID_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_,'\\-\\+/]{1,}$");

	protected Vector3i min;
	protected Vector3i max;

	private final String id;
	private final boolean transientRegion;
	private int priority = 0;
	private ProtectedRegion parent;
	
	private final EDomain owners;
	private final EDomain members;
	
	private final ConcurrentMap<Flag<?>, FlagValue<?>> flags;
	
	public ProtectedRegion(String id, boolean transientRegion) {
		Preconditions.checkNotNull(id);
		Preconditions.checkArgument(!ProtectedRegion.isValidId(id), "Invalid region ID: " + id);

		this.id = UtilsString.normalize(id);
		this.owners = new EDomain();
		this.members = new EDomain();
		
		this.flags = new ConcurrentHashMap<Flag<?>, FlagValue<?>>();
		
		this.transientRegion = transientRegion;
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
	
	public String getId() {
		return this.id;
	}
	
	public abstract boolean isPhysicalArea();
	
	public Vector3i getMinimumPoint() {
		return this.min;
	}
	
	public Vector3i getMaximumPoint() {
		return this.max;
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Optional<ProtectedRegion> getParent() {
		return Optional.of(this.parent);
	}
	
	public void setParent(@Nullable ProtectedRegion parent) throws CircularInheritanceException {
		if (parent == null) {
			this.parent = null;
			return;
		}

		if (parent == this) {
			throw new CircularInheritanceException();
		}

		ProtectedRegion p = parent.getParent().orElse(null);
		while (p != null) {
			if (p == this) {
				throw new CircularInheritanceException();
			}
			p = p.getParent().orElse(null);
		}

		this.parent = parent;
	}
	
	public void clearParent() {
		this.parent = null;
	}
	
	public EDomain getOwners() {
		return this.owners;
	}
	
	public EDomain getMembers() {
		return this.members;
	}
	
	public boolean hasMembersOrOwners() {
		return this.owners.size() > 0 || this.members.size() > 0;
	}
	
	public boolean isOwner(Player player) {
		Preconditions.checkNotNull(player);

		if (this.owners.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.getParent().orElse(null);
		while (curParent != null) {
			if (curParent.getOwners().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	public boolean isMember(Player player) {
		Preconditions.checkNotNull(player);

		if (this.isOwner(player)) {
			return true;
		}

		if (this.members.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.getParent().orElse(null);
		while (curParent != null) {
			if (curParent.getMembers().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	public boolean isMemberOnly(Player player) {
		Preconditions.checkNotNull(player);

		if (this.members.contains(player)) {
			return true;
		}

		ProtectedRegion curParent = this.getParent().orElse(null);
		while (curParent != null) {
			if (curParent.getMembers().contains(player)) {
				return true;
			}

			curParent = curParent.getParent().orElse(null);
		}

		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Nullable
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
	
	public Map<Flag<?>, FlagValue<?>> getFlags() {
		return this.flags;
	}
	
	public void setFlags(Map<Flag<?>, FlagValue<?>> flags) {
		Preconditions.checkNotNull(flags);
		
		this.flags.clear();
		this.flags.putAll(flags);
	}
	
	public abstract List<Vector2i> getPoints();
	
	public abstract int volume();
	
	public abstract boolean contains(Vector3i pt);
	
	public boolean contains(Vector2i position) {
		Preconditions.checkNotNull(position);
		
		return this.contains(new Vector3i(position.getX(), min.getY(), position.getY()));
	}
	
	public boolean contains(int x, int y, int z) {
		return this.contains(new Vector3i(x, y, z));
	}
	
	public boolean containsAny(List<Vector2i> positions) {
		Preconditions.checkNotNull(positions);

		for (Vector2i pt : positions) {
			if (this.contains(pt)) {
				return true;
			}
		}
		return false;
	}

	public abstract RegionType getType();

	public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
		Preconditions.checkNotNull(regions, "regions");

		List<ProtectedRegion> intersecting = Lists.newArrayList();
		Area thisArea = this.toArea();

		for (ProtectedRegion region : regions) {
			if (!region.isPhysicalArea()) continue;

			if (this.intersects(region, thisArea)) {
				intersecting.add(region);
			}
		}

		return intersecting;
	}
	
	protected boolean intersects(ProtectedRegion region, Area thisArea) {
		if (this.intersectsBoundingBox(region)) {
			Area testArea = region.toArea();
			testArea.intersect(thisArea);
			return !testArea.isEmpty();
		} else {
			return false;
		}
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

	/**
	 * Compares all edges of two regions to see if any of them intersect.
	 *
	 * @param region the region to check
	 * @return whether any edges of a region intersect
	 */
	protected boolean intersectsEdges(ProtectedRegion region) {
		List<Vector2i> pts1 = this.getPoints();
		List<Vector2i> pts2 = region.getPoints();
		Vector2i lastPt1 = pts1.get(pts1.size() - 1);
		Vector2i lastPt2 = pts2.get(pts2.size() - 1);
		for (Vector2i aPts1 : pts1) {
			for (Vector2i aPts2 : pts2) {

				Line2D line1 = new Line2D.Double(
						lastPt1.getX(),
						lastPt1.getY(),
						aPts1.getX(),
						aPts1.getY());

				if (line1.intersectsLine(
						lastPt2.getX(),
						lastPt2.getY(),
						aPts2.getX(),
						aPts2.getY())) {
					return true;
				}
				lastPt2 = aPts2;
			}
			lastPt1 = aPts1;
		}
		return false;
	}
	
	abstract Area toArea();
	
	public boolean isTransient() {
		return this.transientRegion;
	}
	
	@Override
	public int compareTo(ProtectedRegion other) {
		if (this.getPriority() > other.getPriority()) {
			return -1;
		} else if (this.getPriority() < other.getPriority()) {
			return 1;
		}

		return this.getId().compareTo(other.getId());
	}

	@Override
	public int hashCode(){
		return this.id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ProtectedRegion)) {
			return false;
		}

		ProtectedRegion other = (ProtectedRegion) obj;
		return other.getId().equals(getId());
	}

	@Override
	public String toString() {
		return "ProtectedRegion{" +
				"id='" + this.id + "', " +
				"type='" + this.getType() + '\'' +
				'}';
	}
	
	public static boolean isValidId(String id) {
		Preconditions.checkNotNull(id);
		
		return VALID_ID_PATTERN.matcher(id).matches();
	}

	/**
	 * Exception : Probleme du parent
	 */
	public static class CircularInheritanceException extends Exception {
		private static final long serialVersionUID = 1L;
	}

}
