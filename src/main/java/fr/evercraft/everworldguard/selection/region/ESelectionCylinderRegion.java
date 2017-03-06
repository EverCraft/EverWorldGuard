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
import fr.evercraft.everapi.services.selection.exception.RegionOperationException;

import java.util.List;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ESelectionCylinderRegion extends ESelectionRegion implements SelectionRegion.Cylinder {
	
	private Vector3i center;
	private Vector3d radius;
	
	private int minY;
	private int maxY;
	
	public ESelectionCylinderRegion(SelectionRegion.Cylinder region) {
		super(region.getWorld().orElse(null));
		
		this.setCenter(region.getCenter());
		this.setRadius(this.getRadius());
		this.setMinimumY(this.getMinimumY());
		this.setMaximumY(this.getMaximumY());
	}
	
	public ESelectionCylinderRegion(World world, Vector3i center, Vector3d radius, int minY, int maxY) {
		super(world);
		
		this.setCenter(center);
		this.setRadius(radius);
		this.setMinimumY(minY);
		this.setMaximumY(maxY);
	}
	
	public void setCenter(Vector3i center) {
		this.center = Vector3i.from(center.getX(), 0, center.getZ());
	}
	
	public Vector3i getCenter() {
		return Vector3i.from(this.center.getX(), (this.maxY + this.minY) / 2, this.center.getZ());
	}
	
	public void setRadius(Vector3d radius) {
		this.radius = radius.abs();
	}
	
	public Vector3d getRadius() {
		return this.radius;
	}
	
	public void extendRadius(Vector3d radius) {
		this.setRadius(this.radius.max(radius.abs()));
    }
	
	public void setMinimumY(int y) {
		this.minY = y;
	}
	
	public void setMaximumY(int y) {
		this.maxY = y;
	}
	
	public int getMinimumY() {
		return this.minY;
	}
	
	public int getMaximumY() {
		return this.maxY;
	}
	
	public void setY(int y) {
		if (y >= this.minY && y <= this.maxY) return;
		
		if (y < this.minY) {
			this.minY = y;
		} else if ( y > this.maxY) {
			this.maxY = y;
		}
    }
	
	@Override
	public Vector3i getMinimumPoint() {
		int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
		return Vector3i.from(
				(int) Math.round(this.center.getX() - this.radius.getX()),
				Math.max(minY, Math.min(maxY, this.minY)),
				(int) Math.round(this.center.getZ() - this.radius.getZ()));
	}

	@Override
	public Vector3i getMaximumPoint() {
		int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
		return Vector3i.from(
				(int) Math.round(this.center.getX() + this.radius.getX()),
				Math.max(minY, Math.min(maxY, this.maxY)),
				(int) Math.round(this.center.getZ() + this.radius.getZ()));
	}
	
	@Override
	public Vector3i getPrimaryPosition() {
		return this.getCenter();
	}
	
	@Override
    public List<Vector3i> getPositions() {
		return ImmutableList.of(this.getMinimumPoint(), this.getMaximumPoint());
    }
	
	@Override
    public boolean containsPosition(Vector3i position) {
    	Preconditions.checkNotNull(position, "position");
    	
        double y = position.getY();        
        if (y < this.minY || y > this.maxY)  return false;
        
        int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		if (y < minY || y > maxY)  return false;
        
        return position.sub(this.center).toDouble().div(this.radius).lengthSquared() <= 1;
    }
	
    @Override
    public int getArea() {
    	Vector3d radius = this.radius.add(0.5, 0.5, 0.5);
    	return (int) Math.floor(radius.getX() * radius.getZ() * Math.PI * this.getHeight());
    }
    
    @Override
    public int getWidth() {
        return (int) (2 * this.radius.getX());
    }

    @Override
    public int getHeight() {
        return this.maxY - this.minY + 1;
    }

    @Override
    public int getLength() {
        return (int) (2 * radius.getZ());
    }

    private Vector3i calculateDiff(Vector3i... changes) throws RegionOperationException {
        Vector3i diff = Vector3i.ZERO;
        for (Vector3i change : changes) {
            diff = diff.add(change);
        }

        if ((diff.getX() & 1) + (diff.getZ() & 1) != 0) {
            throw new RegionOperationException("Cylinders changes must be even for each horizontal dimensions.");
        }
        return diff.toDouble().div(2).floor().toInt();
    }

    private Vector3d calculateChanges(Vector3i... changes) {
    	Vector3i total = Vector3i.ZERO;
        for (Vector3i change : changes) {
            total = total.add(change.abs());
        }

        return total.toDouble().div(2);
    }

	@Override
	public boolean expand(Vector3i... changes) throws RegionOperationException {
		Preconditions.checkNotNull(changes, "changes");
		
		this.center = this.center.add(this.calculateDiff(changes));
		this.radius = this.radius.add(this.calculateChanges(changes));
		
        for (Vector3i change : changes) {
            int y = change.getY();
            if (y > 0) {
                maxY += y;
            } else {
                minY += y;
            }
        }
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) throws RegionOperationException {
		Preconditions.checkNotNull(changes, "changes");
		
		this.center = this.center.sub(this.calculateDiff(changes));
		this.radius = this.radius.sub(this.calculateChanges(changes)).max(Vector3d.from(1.5, 0, 1.5));
		
		for (Vector3i change : changes) {
            int height = this.maxY - this.minY;
            int changeY = change.getY();
            if (changeY > 0) {
            	this.minY += Math.min(height, changeY);
            } else {
            	this.maxY += Math.max(-height, changeY);
            }
        }
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		this.center = this.center.add(change);

        int changeY = change.getY();
        this.maxY += changeY;
        this.minY += changeY;
        return true;
	}
	
	@Override
	public ESelectionCylinderRegion clone() {
		return new ESelectionCylinderRegion(this);
	}
}
