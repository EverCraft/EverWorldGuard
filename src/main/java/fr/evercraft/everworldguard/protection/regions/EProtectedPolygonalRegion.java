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

import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everworldguard.protection.index.EWWorld;

import java.awt.Polygon;
import java.awt.geom.Area;
import java.util.List;
import java.util.Optional;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class EProtectedPolygonalRegion extends EProtectedRegion implements ProtectedRegion.Polygonal {
	
	private final List<Vector3i> positions;
	private final int volume;
	
	public EProtectedPolygonalRegion(EWWorld world, String identifier, List<Vector3i> positions) throws RegionIdentifierException {
		this(world, identifier, positions, false);
	}
	
	public EProtectedPolygonalRegion(EWWorld world, String identifier, List<Vector3i> positions, boolean transientRegion) 
			throws RegionIdentifierException {
		super(world, identifier, transientRegion);
		
		Preconditions.checkNotNull(positions, "positions");
		Preconditions.checkArgument(!positions.isEmpty(), "positions > 1");
		
		this.positions = ImmutableList.copyOf(positions);
		this.setMinMaxPoints(positions);

        this.volume = this.setVolume();
		System.err.println(identifier + " : " + this.volume);
	}
	
	@Override
	protected void setMinMaxPoints(List<Vector3i> positions) {
		int minX = positions.get(0).getX();
		int minY = positions.get(0).getY();
		int minZ = positions.get(0).getZ();
		int maxX = minX;
		int maxY = minY;
		int maxZ = minZ;

		for (Vector3i v : positions) {
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
	
	@Override
    public boolean isPhysicalArea() {
        return true;
    }

	@Override
    public List<Vector3i> getPoints() {
		return ImmutableList.copyOf(this.positions);
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
    public Optional<Area> toArea() {
    	List<Vector3i> points = getPoints();
        int npoints = points.size();
        int[] xpoints = new int[npoints];
        int[] ypoints = new int[npoints];

        int i = 0;
        for (Vector3i point : points) {
        	xpoints[i] = point.getX();
        	ypoints[i] = point.getZ();
            i++;
        }
        Polygon polygon = new Polygon(xpoints, ypoints, npoints);
        return Optional.of(new Area(polygon));
    }
    
    @Override
    public int getVolume() {
        return this.volume;
    }
    
    private int setVolume() {
    	int minY = this.min.getY();
        int maxY = this.max.getY();
    	int volume = 0;
    	
    	for (int x = this.min.getX(); x <= this.max.getX(); x++) {
    		for (int z = this.min.getZ(); z <= this.max.getZ(); z++) {
        		if (this.containsPosition(Vector3i.from(x, minY, z))) {
        			volume++;
        		}
        	}
    	}
        volume *= (maxY - minY) + 1;
        return volume;
    }
}
