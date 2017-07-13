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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ESelectionEllipsoidRegion extends ESelectionRegion implements SelectionRegion.Ellipsoid {
	
	private Vector3i center;
	private Vector3d radius;
	
	public ESelectionEllipsoidRegion(final SelectionRegion.Ellipsoid region) {
		super(region.getWorld().orElse(null));
		
		this.setCenter(region.getCenter());
		this.setRadius(this.getRadius());
	}
	
	public ESelectionEllipsoidRegion(final World world, final Vector3i center, final Vector3d radius) {
		super(world);
		
		this.setCenter(center);
		this.setRadius(radius);
	}
	
	public void setCenter(final Vector3i center) {
		this.center = center;
	}
	
	public Vector3i getCenter() {
		return this.center;
	}
	
	public void setRadius(final Vector3d radius) {
		this.radius = radius.abs();
	}
	
	public Vector3d getRadius() {
		return this.radius;
	}
	
	public void extendRadius(final Vector3d radius) {
		this.setRadius(this.radius.max(radius.abs()));
    }
	
	@Override
	public Vector3i getMinimumPoint() {
		int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
		return Vector3i.from(
				(int) Math.round(this.center.getX() - this.radius.getX()),
				Math.max(minY, Math.min(maxY, this.center.getY() - ((int) this.radius.getY()))),
				(int) Math.round(this.center.getZ() - this.radius.getZ()));
	}

	@Override
	public Vector3i getMaximumPoint() {
		int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		return Vector3i.from(
				(int) Math.round(this.center.getX() + this.radius.getX()),
				Math.max(minY, Math.min(maxY, this.center.getY() + ((int) this.radius.getY()))),
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
    public boolean containsPosition(final Vector3i position) {
    	Preconditions.checkNotNull(position, "position");
    	
        double y = position.getY();
        int minY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxY = (this.world == null) ? 255 : world.getBlockMax().getY();
		if (y < minY || y > maxY)  return false;
        
        return position.sub(this.center).toDouble().div(this.radius).lengthSquared() <= 1;
    }
	
    @Override
    public int getArea() {
    	Vector3d radius = this.radius.add(0.5, 0.5, 0.5);
    	return (int) Math.floor((4.0 / 3.0) * Math.PI * radius.getX() * radius.getY() * radius.getZ());
    }
    
    @Override
    public int getWidth() {
        return (int) (2 * this.radius.getX());
    }

    @Override
    public int getHeight() {
        return (int) (2 * this.radius.getY());
    }

    @Override
    public int getLength() {
        return (int) (2 * this.radius.getZ());
    }

    private Vector3i calculateDiff(final Vector3i... changes) {
        Vector3i diff = Vector3i.ZERO;
        for (Vector3i change : changes) {
            diff = diff.add(change);
        }
        
        return diff.toDouble().div(2).floor().toInt();
    }

    private Vector3d calculateChanges(final Vector3i... changes) {
    	Vector3i total = Vector3i.ZERO;
        for (Vector3i change : changes) {
            total = total.add(change.abs());
        }

        return total.toDouble().div(2);
    }

	@Override
	public boolean expand(final Vector3i... changes) {
		Preconditions.checkNotNull(changes, "changes");
		
		this.center = this.center.add(this.calculateDiff(changes));
		this.radius = this.radius.add(this.calculateChanges(changes));
		return true;
	}

	@Override
	public boolean contract(final Vector3i... changes) {
		Preconditions.checkNotNull(changes, "changes");
		
		this.center = this.center.sub(this.calculateDiff(changes));
		this.radius = this.radius.sub(this.calculateChanges(changes)).max(Vector3d.from(1.5, 0, 1.5));
		return true;
	}

	@Override
	public boolean shift(final Vector3i change) {
		this.center = this.center.add(change);
        return true;
	}
	
	@Override
	public ESelectionEllipsoidRegion clone() {
		return new ESelectionEllipsoidRegion(this);
	}
}
