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

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;

public class ESelectionEllipsoidRegion extends ESelectionRegion implements SelectionRegion.Cuboid {
	
	private Vector3i position1;
	private Vector3i position2;
	
	public ESelectionEllipsoidRegion(SelectionRegion.Cuboid region) {
		super(region.getWorld().orElse(null));
		
		this.position1 = region.getPrimaryPosition();
		this.position2 = region.getSecondaryPosition();
	}
	
	public ESelectionEllipsoidRegion(World world, Vector3i pos1, Vector3i pos2) {
		super(world);
		
		this.position1 = pos1;
		this.position2 = pos2;
		this.recalculate();
	}
	
	public void setPosition(Vector3i pos1, Vector3i pos2) {
		this.position1 = pos1;
		this.position2 = pos2;
		
		this.recalculate();
	}
	
	private void recalculate() {
		int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
		this.position1 = new Vector3i(
				this.position1.getX(),
				Math.max(minY, Math.min(maxY, this.position1.getY())), 
				this.position1.getZ());
		this.position2 = new Vector3i(
				this.position2.getX(),
				Math.max(minY, Math.min(maxY, this.position2.getY())), 
				this.position2.getZ());
	}
	
	@Override
	public Vector3i getMinimumPoint() {
		return Vector3i.from(
				Math.min(this.position1.getX(), this.position2.getX()),
                Math.min(this.position1.getY(), this.position2.getY()),
                Math.min(this.position1.getZ(), this.position2.getZ()));
	}

	@Override
	public Vector3i getMaximumPoint() {
		return Vector3i.from(
				Math.max(this.position1.getX(), this.position2.getX()),
                Math.max(this.position1.getY(), this.position2.getY()),
                Math.max(this.position1.getZ(), this.position2.getZ()));
	}
	
	@Override
	public Vector3i getPrimaryPosition() {
		return this.position1;
	}
	
	@Override
	public Vector3i getSecondaryPosition() {
		return this.position2;
	}
	
	@Override
    public List<Vector3i> getPositions() {
		return ImmutableList.of(this.position1, this.position2);
    }
	
	@Override
    public boolean containsPosition(Vector3i position) {
    	Preconditions.checkNotNull(position, "position");
    	
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
    public int getVolume() {
    	Vector3i min = this.getMinimumPoint();
        Vector3i max = this.getMaximumPoint();
    	
        int xLength = max.getX() - min.getX() + 1;
        int yLength = max.getY() - min.getY() + 1;
        int zLength = max.getZ() - min.getZ() + 1;

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

	@Override
	public boolean expand(Vector3i... changes) {
		Preconditions.checkNotNull(changes, "changes");
		
		for (Vector3i change : changes) {
            if (change.getX() > 0) {
                if (Math.max(this.position1.getX(), this.position2.getX()) == this.position1.getX()) {
                    this.position1 = this.position1.add(Vector3i.from(change.getX(), 0, 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(this.position1.getX(), this.position2.getX()) == this.position1.getX()) {
                    this.position1 = this.position1.add(Vector3i.from(change.getX(), 0, 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(change.getX(), 0, 0));
                }
            }

            if (change.getY() > 0) {
                if (Math.max(this.position1.getY(), this.position2.getY()) == this.position1.getY()) {
                    this.position1 = this.position1.add(Vector3i.from(0, change.getY(), 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, change.getY(), 0));
                }
            } else {
                if (Math.min(this.position1.getY(), this.position2.getY()) == this.position1.getY()) {
                    this.position1 = this.position1.add(Vector3i.from(0, change.getY(), 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, change.getY(), 0));
                }
            }

            if (change.getZ() > 0) {
                if (Math.max(this.position1.getZ(), this.position2.getZ()) == this.position1.getZ()) {
                    this.position1 = this.position1.add(Vector3i.from(0, 0, change.getZ()));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(this.position1.getZ(), this.position2.getZ()) == this.position1.getZ()) {
                    this.position1 = this.position1.add(Vector3i.from(0, 0, change.getZ()));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, 0, change.getZ()));
                }
            }
        }
		this.recalculate();
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) {
		Preconditions.checkNotNull(changes, "changes");
		
		for (Vector3i change : changes) {
            if (change.getX() < 0) {
                if (Math.max(this.position1.getX(), this.position2.getX()) == this.position1.getX()) {
                    this.position1 = this.position1.add(Vector3i.from(change.getX(), 0, 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(change.getX(), 0, 0));
                }
            } else {
                if (Math.min(this.position1.getX(), this.position2.getX()) == this.position1.getX()) {
                    this.position1 = this.position1.add(Vector3i.from(change.getX(), 0, 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(change.getX(), 0, 0));
                }
            }

            if (change.getY() < 0) {
                if (Math.max(this.position1.getY(), this.position2.getY()) == this.position1.getY()) {
                    this.position1 = this.position1.add(Vector3i.from(0, change.getY(), 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, change.getY(), 0));
                }
            } else {
                if (Math.min(this.position1.getY(), this.position2.getY()) == this.position1.getY()) {
                    this.position1 = this.position1.add(Vector3i.from(0, change.getY(), 0));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, change.getY(), 0));
                }
            }

            if (change.getZ() < 0) {
                if (Math.max(this.position1.getZ(), this.position2.getZ()) == this.position1.getZ()) {
                    this.position1 = this.position1.add(Vector3i.from(0, 0, change.getZ()));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, 0, change.getZ()));
                }
            } else {
                if (Math.min(this.position1.getZ(), this.position2.getZ()) == this.position1.getZ()) {
                    this.position1 = this.position1.add(Vector3i.from(0, 0, change.getZ()));
                } else {
                    this.position2 = this.position2.add(Vector3i.from(0, 0, change.getZ()));
                }
            }
        }
		
		this.recalculate();
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		this.position1.add(change);
		this.position2.add(change);
		
		this.recalculate();
		return true;
	}
	
	@Override
	public ESelectionEllipsoidRegion clone() {
		return new ESelectionEllipsoidRegion(this);
	}
}
