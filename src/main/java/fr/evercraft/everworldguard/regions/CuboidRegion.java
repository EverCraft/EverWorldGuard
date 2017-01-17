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

import fr.evercraft.everapi.services.worldguard.regions.Region;
import fr.evercraft.everapi.services.worldguard.regions.RegionOperationException;
import fr.evercraft.everapi.sponge.UtilsChunk;

import java.util.HashSet;
import java.util.Set;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;

public class CuboidRegion extends ERegion {

	private Vector3i pos1;
	private Vector3i pos2;
	
	public CuboidRegion(Vector3i pos1, Vector3i pos2) {
		this(null, pos1, pos2);
	}

	public CuboidRegion(World world, Vector3i pos1, Vector3i pos2) {
		super(world);
		Preconditions.checkNotNull(pos1, "pos1 = " + pos1);
		Preconditions.checkNotNull(pos2, "pos2 = " + pos2);
		
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.recalculate();
	}

	public Vector3i getPos1() {
		return this.pos1;
	}
	
	public void setPos1(Vector3i pos1) {
		this.pos1 = pos1;
	}

	public Vector3i getPos2() {
		return pos2;
	}

	public void setPos2(Vector3i pos2) {
		this.pos2 = pos2;
	}

	private void recalculate() {
		int max = this.world == null ? 255 : this.world.getBlockMax().getY();
		this.pos1 = new Vector3i(this.pos1.getX(), Math.max(0, Math.min(max, this.pos1.getY())), this.pos1.getZ());
		this.pos2 = new Vector3i(this.pos2.getX(), Math.max(0, Math.min(max, this.pos2.getY())), this.pos2.getZ());
	}

	@Override
	public Vector3i getMinimumPoint() {
		return new Vector3i(Math.min(this.pos1.getX(), this.pos2.getX()),
				Math.min(this.pos1.getY(), this.pos2.getY()),
				Math.min(this.pos1.getZ(), this.pos2.getZ()));
	}

	@Override
	public Vector3i getMaximumPoint() {
		return new Vector3i(Math.max(this.pos1.getX(), this.pos2.getX()),
				Math.max(this.pos1.getY(), this.pos2.getY()),
				Math.max(this.pos1.getZ(), this.pos2.getZ()));
	}

	public int getMinimumY() {
		return Math.min(this.pos1.getY(), this.pos2.getY());
	}

	public int getMaximumY() {
		return Math.max(this.pos1.getY(), this.pos2.getY());
	}

	@Override
	public void expand(Vector3i... changes) {
		Preconditions.checkNotNull(changes);

		for (Vector3i change : changes) {
			if (change.getX() > 0) {
				if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
					this.pos1 = this.pos1.add(change.getX(), 0, 0);
				} else {
					this.pos2 = this.pos2.add(change.getX(), 0, 0);
				}
			} else {
				if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
					this.pos1 = this.pos1.add(change.getX(), 0, 0);
				} else {
					this.pos2 = this.pos2.add(change.getX(), 0, 0);
				}
			}

			if (change.getY() > 0) {
				if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
					this.pos1 = this.pos1.add(0, change.getY(), 0);
				} else {
					this.pos2 = this.pos2.add(0, change.getY(), 0);
				}
			} else {
				if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
					this.pos1 = this.pos1.add(0, change.getY(), 0);
				} else {
					this.pos2 = this.pos2.add(0, change.getY(), 0);
				}
			}

			if (change.getZ() > 0) {
				if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
					this.pos1 = this.pos1.add(0, 0, change.getZ());
				} else {
					this.pos2 = this.pos2.add(0, 0, change.getZ());
				}
			} else {
				if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
					this.pos1 = this.pos1.add(0, 0, change.getZ());
				} else {
					this.pos2 = this.pos2.add(0, 0, change.getZ());
				}
			}
		}

		recalculate();
	}

	@Override
	public void contract(Vector3i... changes) {
		Preconditions.checkNotNull(changes);

		for (Vector3i change : changes) {
			if (change.getX() < 0) {
				if (Math.max(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
					this.pos1 = this.pos1.add(change.getX(), 0, 0);
				} else {
					this.pos2 = this.pos2.add(change.getX(), 0, 0);
				}
			} else {
				if (Math.min(this.pos1.getX(), this.pos2.getX()) == this.pos1.getX()) {
					this.pos1 = this.pos1.add(change.getX(), 0, 0);
				} else {
					this.pos2 = this.pos2.add(change.getX(), 0, 0);
				}
			}

			if (change.getY() < 0) {
				if (Math.max(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
					this.pos1 = this.pos1.add(0, change.getY(), 0);
				} else {
					this.pos2 = this.pos2.add(0, change.getY(), 0);
				}
			} else {
				if (Math.min(this.pos1.getY(), this.pos2.getY()) == this.pos1.getY()) {
					this.pos1 = this.pos1.add(0, change.getY(), 0);
				} else {
					this.pos2 = this.pos2.add(0, change.getY(), 0);
				}
			}

			if (change.getZ() < 0) {
				if (Math.max(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
					this.pos1 = this.pos1.add(0, 0, change.getZ());
				} else {
					this.pos2 = this.pos2.add(0, 0, change.getZ());
				}
			} else {
				if (Math.min(this.pos1.getZ(), this.pos2.getZ()) == this.pos1.getZ()) {
					this.pos1 = this.pos1.add(0, 0, change.getZ());
				} else {
					this.pos2 = this.pos2.add(0, 0, change.getZ());
				}
			}
		}

		recalculate();
	}

	@Override
	public void shift(Vector3i change) throws RegionOperationException {
		this.pos1 = this.pos1.add(change);
		this.pos2 = this.pos2.add(change);

		recalculate();
	}

	@Override
	public Set<Vector2i> getChunks() {
		Set<Vector2i> chunks = new HashSet<Vector2i>();

		Vector3i min = this.getMinimumPoint();
		Vector3i max = this.getMaximumPoint();

		for (int x = min.getX() >> UtilsChunk.CHUNK_SHIFTS; x <= max.getX() >> UtilsChunk.CHUNK_SHIFTS; x++) {
			for (int z = min.getZ() >> UtilsChunk.CHUNK_SHIFTS; z <= max.getZ() >> UtilsChunk.CHUNK_SHIFTS; z++) {
				chunks.add(new Vector2i(x, z));
			}
		}

		return chunks;
	}

	@Override
	public Set<Vector3i> getChunkCubes() {
		Set<Vector3i> chunks = new HashSet<Vector3i>();

		Vector3i min = this.getMinimumPoint();
		Vector3i max = this.getMaximumPoint();

		for (int x = min.getX() >> UtilsChunk.CHUNK_SHIFTS; x <= max.getX() >> UtilsChunk.CHUNK_SHIFTS; ++x) {
			for (int z = min.getZ() >> UtilsChunk.CHUNK_SHIFTS; z <= max.getZ() >> UtilsChunk.CHUNK_SHIFTS; ++z) {
				for (int y = min.getY() >> UtilsChunk.CHUNK_SHIFTS; y <= max.getY() >> UtilsChunk.CHUNK_SHIFTS; ++y) {
					chunks.add(new Vector3i(x, y, z));
				}
			}
		}

		return chunks;
	}

	@Override
	public boolean contains(Vector3i position) {
		double x = position.getX();
		double y = position.getY();
		double z = position.getZ();

		Vector3i min = this.getMinimumPoint();
		Vector3i max = this.getMaximumPoint();

		return x >= min.getX() && x <= max.getX()
				&& y >= min.getY() && y <= max.getY()
				&& z >= min.getZ() && z <= max.getZ();
	}

	@Override
	public String toString() {
		return getMinimumPoint() + " - " + getMaximumPoint();
	}

	@Override
	public CuboidRegion clone() {
		return (CuboidRegion) super.clone();
	}

	/**
	 * Make a cuboid region out of the given region using the minimum and maximum
	 * bounds of the provided region.
	 *
	 * @param region the region
	 * @return a new cuboid region
	 */
	public static CuboidRegion makeCuboid(Region region) {
		Preconditions.checkNotNull(region);
		
		return new CuboidRegion(region.getMinimumPoint(), region.getMaximumPoint());
	}

	/**
	 * Make a cuboid from the center.
	 *
	 * @param origin the origin
	 * @param apothem the apothem, where 0 is the minimum value to make a 1x1 cuboid
	 * @return a cuboid region
	 */
	public static CuboidRegion fromCenter(Vector3i origin, int apothem) {
		Preconditions.checkNotNull(origin);
		Preconditions.checkArgument(apothem >= 0, "apothem => 0 required");
		
		Vector3i size = new Vector3i(1, 1, 1).mul(apothem);
		return new CuboidRegion(origin.sub(size), origin.add(size));
	}

}
