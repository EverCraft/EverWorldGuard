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
package fr.evercraft.everworldguard.selection.region;

import fr.evercraft.everapi.services.selection.SelectionRegion;
import java.util.List;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;

public class ESelectionCuboidRegion extends ESelectionRegion implements SelectionRegion.Cuboid {
	
	public ESelectionCuboidRegion() {
	}
	
	@Override
    public List<Vector3i> getPositions() {
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
