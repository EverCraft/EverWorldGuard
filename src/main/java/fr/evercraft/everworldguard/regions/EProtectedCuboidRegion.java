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

import fr.evercraft.everapi.services.worldguard.regions.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.RegionType;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.math.LongMath;

public class EProtectedCuboidRegion extends EProtectedRegion {
	
	public EProtectedCuboidRegion(String id, Vector3i pos1, Vector3i pos2) {
		this(id, false, pos1, pos2);
	}
	
	public EProtectedCuboidRegion(String id, boolean transientRegion, Vector3i pos1, Vector3i pos2) {
		super(id, transientRegion);
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
        Builder<Vector3i> points = ImmutableList.builder();
        
        int x1 = this.min.getX();
        int x2 = this.max.getX();
        int z1 = this.min.getZ();
        int z2 = this.max.getZ();

        points.add(new Vector3i(x1, 0, z1));
        points.add(new Vector3i(x2, 0, z1));
        points.add(new Vector3i(x2, 0, z2));
        points.add(new Vector3i(x1, 0, z2));

        return points.build();
    }

    @Override
    public boolean containsPosition(Vector3i pos) {
        final double x = pos.getX();
        final double y = pos.getY();
        final double z = pos.getZ();
        return x >= this.getMinimumPoint().getX() && x < this.getMaximumPoint().getX()+1
                && y >= this.getMinimumPoint().getY() && y < this.getMaximumPoint().getY()+1
                && z >= this.getMinimumPoint().getZ() && z < this.getMaximumPoint().getZ()+1;
    }

    @Override
    public RegionType getType() {
        return RegionType.CUBOID;
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
