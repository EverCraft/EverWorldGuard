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

import java.util.*;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;

public abstract class ERegion implements Region {

	protected World world;

	public ERegion(World world) {
		this.world = world;
	}

	@Override
	public Vector3i getCenter() {
		return this.getMinimumPoint().add(this.getMaximumPoint()).div(2);
	}

	@Override
	public Optional<World> getWorld() {
		return Optional.ofNullable(world);
	}

	@Override
	public void setWorld(World world) {
		this.world = world;
	}

	@Override
	public void shift(Vector3i change) throws RegionOperationException {
		this.expand(change);
		this.contract(change);
	}

	@Override
	public ERegion clone() {
		try {
			return (ERegion) super.clone();
		} catch (CloneNotSupportedException exc) {
			return null;
		}
	}

	@Override
	public List<Vector2i> polygonize(int maxPoints) {
		if (maxPoints >= 0 && maxPoints < 4) {
			throw new IllegalArgumentException("Cannot polygonize an ERegion with no overridden polygonize method into less than 4 points.");
		}

		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		final List<Vector2i> points = new ArrayList<Vector2i>(4);

		points.add(new Vector2i(min.getX(), min.getZ()));
		points.add(new Vector2i(min.getX(), max.getZ()));
		points.add(new Vector2i(max.getX(), max.getZ()));
		points.add(new Vector2i(max.getX(), min.getZ()));

		return points;
	}

	/**
	 * Get the number of blocks in the region.
	 *
	 * @return number of blocks
	 */
	@Override
	public int getArea() {
		Vector3i min = getMinimumPoint();
		Vector3i max = getMaximumPoint();

		return (int)((max.getX() - min.getX() + 1) *
					 (max.getY() - min.getY() + 1) *
					 (max.getZ() - min.getZ() + 1));
	}

	/**
	 * Get X-size.
	 *
	 * @return width
	 */
	@Override
	public int getWidth() {
		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		return (int) (max.getX() - min.getX() + 1);
	}

	/**
	 * Get Y-size.
	 *
	 * @return height
	 */
	@Override
	public int getHeight() {
		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		return (int) (max.getY() - min.getY() + 1);
	}

	/**
	 * Get Z-size.
	 *
	 * @return length
	 */
	@Override
	public int getLength() {
		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		return (int) (max.getZ() - min.getZ() + 1);
	}

	/**
	 * Get a list of chunks.
	 *
	 * @return a set of chunks
	 */
	@Override
	public Set<Vector2i> getChunks() {
		final Set<Vector2i> chunks = new HashSet<Vector2i>();

		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		final int minY = min.getY();

		for (int x = min.getX(); x <= max.getX(); ++x) {
			for (int z = min.getZ(); z <= max.getZ(); ++z) {
				if (this.contains(new Vector3i(x, minY, z))) {
					chunks.add(new Vector2i(x >> UtilsChunk.CHUNK_SHIFTS, z >> UtilsChunk.CHUNK_SHIFTS));
				}
			}
		}

		return chunks;
	}

	@Override
	public Set<Vector3i> getChunkCubes() {
		final Set<Vector3i> chunks = new HashSet<Vector3i>();

		final Vector3i min = this.getMinimumPoint();
		final Vector3i max = this.getMaximumPoint();

		for (int x = min.getX(); x <= max.getX(); ++x) {
			for (int y = min.getY(); y <= max.getY(); ++y) {
				for (int z = min.getZ(); z <= max.getZ(); ++z) {
					if (this.contains(new Vector3i(x, y, z))) {
						chunks.add(new Vector3i(x >> UtilsChunk.CHUNK_SHIFTS, y >> UtilsChunk.CHUNK_SHIFTS, z >> UtilsChunk.CHUNK_SHIFTS));
					}
				}
			}
		}

		return chunks;
	}

}
