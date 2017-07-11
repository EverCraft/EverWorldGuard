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

import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.protection.index.EWWorld;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;

public class EProtectedCuboidRegion extends EProtectedRegion implements ProtectedRegion.Cuboid {
	
	public EProtectedCuboidRegion(EWWorld world, UUID identifier, String name, Vector3i pos1, Vector3i pos2) {
		this(world, identifier, name, pos1, pos2, false);
	}
	
	public EProtectedCuboidRegion(EWWorld world, UUID identifier, String name, Vector3i pos1, Vector3i pos2, boolean transientRegion) {
		super(world, identifier, name, transientRegion);
		Preconditions.checkNotNull(pos1, "pos1");
		Preconditions.checkNotNull(pos2, "pos2");
		
		this.setMinMaxPoints(pos1, pos2);
	}
	
	private void setMinMaxPoints(Vector3i pos1, Vector3i pos2) {
		Preconditions.checkNotNull(pos1);
		Preconditions.checkNotNull(pos2);

		List<Vector3i> points = new ArrayList<Vector3i>();
		points.add(pos1);
		points.add(pos2);
		super.setMinMaxPoints(points);
	}
	 
	public void setMinimumPoint(Vector3i position) {
		this.setMinMaxPoints(position, this.max);
	}
	
	public void setMaximumPoint(Vector3i position) {
        this.setMinMaxPoints(this.min, position);
    }
	
	@Override
    public boolean isPhysicalArea() {
        return true;
    }

	@Override
    public List<Vector3i> getPoints() {
		return ImmutableList.of(this.min, this.max);
    }

    @Override
    public boolean containsPosition(Vector3i position) {
    	Preconditions.checkNotNull(position, "position");
    	
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        return x >= this.min.getX() && x <= this.max.getX()
                && y >= this.min.getY() && y <= this.max.getY()
                && z >= this.min.getZ() && z <= this.max.getZ();
    }

    @Override
    public Optional<Area> toArea() {
        int x = this.getMinimumPoint().getX();
        int z = this.getMinimumPoint().getZ();
        int width = this.getMaximumPoint().getX() - x;
        int height = this.getMaximumPoint().getZ() - z;
        return Optional.of(new Area(new Rectangle(x, z, width, height)));
    }

    @Override
    protected boolean intersects(ProtectedRegion region, Area thisArea) {
        if (region instanceof EProtectedCuboidRegion) {
            return this.intersectsBoundingBox(region);
        } else {
            return super.intersects(region, thisArea);
        }
    }

    @Override
    public int getVolume() {
        int xLength = this.getMaximumPoint().getX() - this.getMinimumPoint().getX() + 1;
        int yLength = this.getMaximumPoint().getY() - this.getMinimumPoint().getY() + 1;
        int zLength = this.getMaximumPoint().getZ() - this.getMinimumPoint().getZ() + 1;

        try {
            long v = LongMath.checkedMultiply(xLength, yLength);
            v = LongMath.checkedMultiply(v, zLength);
            if (v > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else {
                return (int) v;
            }
        } catch (ArithmeticException e) {
            return Integer.MAX_VALUE;
        }
    }
}
