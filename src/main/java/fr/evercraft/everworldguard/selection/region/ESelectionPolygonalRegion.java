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

import fr.evercraft.everapi.services.selection.RegionOperationException;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.util.Chronometer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.stream.IntStream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;


public class ESelectionPolygonalRegion extends ESelectionRegion implements SelectionRegion.Polygonal {
	
	private final List<Vector3i> positions;
	
	private Vector3i min;
	private Vector3i max;
	
	private Integer volume;
	
	public ESelectionPolygonalRegion(SelectionRegion.Polygonal region) {
		super(region.getWorld().orElse(null));
		
		this.volume = null;
		this.positions = new ArrayList<Vector3i>();
		this.setPositions(this.getPositions());
	}
	
	public ESelectionPolygonalRegion(World world, List<Vector3i> positions) {
		super(world);
		
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkArgument(!positions.isEmpty(), "positions > 0");
		
		this.positions = new ArrayList<Vector3i>();
		this.setPositions(positions);
	}

	public void setPositions(List<Vector3i> positions) {
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkArgument(!positions.isEmpty(), "positions > 0");
		
		this.volume = null;
		this.positions.clear();
		
		int minWorldY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxWorldY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
		int minX = positions.get(0).getX();
		int minY = positions.get(0).getY();
		int minZ = positions.get(0).getZ();
		int maxX = minX;
		int maxY = minY;
		int maxZ = minZ;

		for (Vector3i position : positions) {
			int x = position.getX();
			int y = Math.max(minWorldY, Math.min(maxWorldY, position.getY()));
			int z = position.getZ();

			if (x < minX) minX = x;
			if (y < minY) minY = y;
			if (z < minZ) minZ = z;

			if (x > maxX) maxX = x;
			if (y > maxY) maxY = y;
			if (z > maxZ) maxZ = z;
			
			this.positions.add(Vector3i.from(x, y, z));
		}
		this.min = new Vector3i(minX, minY, minZ);
		this.max = new Vector3i(maxX, maxY, maxZ);
	}
	
	@Override
	public Vector3i getMinimumPoint() {
		return this.min;
	}

	@Override
	public Vector3i getMaximumPoint() {
		return this.max;
	}
	
	@Override
	public Vector3i getPrimaryPosition() {
		return this.positions.get(0);
	}
	
	@Override
    public List<Vector3i> getPositions() {
		ImmutableList.Builder<Vector3i> builder = ImmutableList.builder();
		
		Vector3i vector;
		Iterator<Vector3i> iterator = this.positions.iterator();
		if (iterator.hasNext()) {
			vector = iterator.next();
			builder.add(Vector3i.from(vector.getX(), this.min.getY(), vector.getZ()));
		}
		
		while (iterator.hasNext()) {
			vector = iterator.next();
			builder.add(Vector3i.from(vector.getX(), this.max.getY(), vector.getZ()));
		}
		
		return builder.build();
    }
	
	@Override
    public boolean containsPosition(Vector3i position) {
		Preconditions.checkNotNull(position, "position");
    	
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        
        if (x < this.min.getX() || x > this.max.getX()) return false;
        if (y < this.min.getY() || y > this.max.getY()) return false;
        if (z < this.min.getZ() || z > this.max.getZ()) return false;
        
        boolean isInside = false;
        int npoints = this.positions.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;
        
        xOld = this.positions.get(npoints-1).getX();
        zOld = this.positions.get(npoints-1).getZ();
        
        for (Vector3i vNew : this.positions) {
        	xNew = vNew.getX();
        	zNew = vNew.getZ();
        	
        	if (xNew == x && zNew == z) {
        		return true;
        	}
        	
        	if (xNew > xOld) {
        		x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
        	} else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }
        	
        	if (x1 <= x && x <= x2) {
                crossproduct = ((long) z - (long) z1) * (long) (x2 - x1) - ((long) z2 - (long) z1) * (long) (x - x1);
                if (crossproduct == 0) {
                    if ((z1 <= z) == (z <= z2)) return true;
                } else if (crossproduct < 0 && (x1 != x)) {
                	isInside = !isInside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }
        return isInside;
    }
	
    @Override
    public int getVolume() {
    	if (this.volume != null) {
    		return this.volume;
    	}
    	
    	int minY = this.min.getY();
        int maxY = this.max.getY();
        
        Chronometer chronometer = new Chronometer();
    	
    	OptionalLong volume = IntStream.range(this.min.getX(), this.max.getX()+1).parallel().mapToLong(
			x -> IntStream.range(this.min.getZ(), this.max.getZ()+1).parallel().filter(
					z -> this.containsPosition(Vector3i.from(x, minY, z))).count()
    	).reduce((count1, count2) -> count1 + count2);
    	
    	this.volume = (int) (volume.orElse(0) * (maxY - minY + 1));
    	
    	Sponge.getServer().getBroadcastChannel().send(Text.of(this.volume + " : " + chronometer.getMilliseconds().toString() + " ms"));
    	
    	return this.volume;
    }

	@Override
	public boolean expand(Vector3i... changes) throws RegionOperationException {
		Preconditions.checkNotNull(changes, "changes");
		
        for (Vector3i change : changes) {
            if (change.getX() != 0 || change.getZ() != 0) {
                throw new RegionOperationException("Polygons can only be expanded vertically.");
            }
        }
        
        int minWorldY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxWorldY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
        for (Vector3i change : changes) {
        	int y = Math.max(minWorldY, Math.min(maxWorldY, change.getY()));
            if (y > 0) {
                this.max = this.max.add(0, y, 0);
            } else {
            	this.min = this.min.add(0, y, 0);
            }
        }
        
        this.volume = null;
        return true;
    }

	@Override
	public boolean contract(Vector3i... changes) throws RegionOperationException {
		Preconditions.checkNotNull(changes, "changes");
		
		for (Vector3i change : changes) {
            if (change.getX() != 0 || change.getZ() != 0) {
                throw new RegionOperationException("Polygons can only be contracted vertically.");
            }
        }

		int minWorldY = (this.world == null) ? 0 : world.getBlockMin().getY();
		int maxWorldY = (this.world == null) ? 255 : world.getBlockMax().getY();
		
        for (Vector3i change : changes) {
        	int y = Math.max(minWorldY, Math.min(maxWorldY, change.getY()));
            if (y < 0) {
                this.max = this.max.add(0, y, 0);
            } else {
            	this.min = this.min.add(0, y, 0);
            }
        }
        
        this.volume = null;
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		Preconditions.checkNotNull(change, "change");
		
		for (int i = 0; i < this.positions.size(); ++i) {
			this.positions.set(i, this.positions.get(i).add(change));
        }
		
		this.min = this.min.add(change);
		this.max = this.max.add(change);
		
		return true;
	}
	
	@Override
	public ESelectionPolygonalRegion clone() {
		return new ESelectionPolygonalRegion(this);
	}
}
