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

import com.flowpowered.math.vector.Vector3i;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Sets;

import fr.evercraft.everapi.java.UtilsString;
import fr.evercraft.everapi.services.selection.RegionOperationException;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.worldguard.exception.CircularInheritanceException;
import fr.evercraft.everapi.services.worldguard.exception.RegionIdentifierException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.regions.Domain;
import fr.evercraft.everworldguard.protection.domains.EDomain;
import fr.evercraft.everworldguard.protection.flag.EFlagValue;
import fr.evercraft.everworldguard.protection.index.EWWorld;

import javax.annotation.Nullable;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.World;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class ESelectionRegion implements SelectionRegion {

	protected World world;
	
	protected Vector3i min;
	protected Vector3i max;
	
	/*
	 * Abstract
	 */
	
	@Override
	public abstract int getVolume();
	
	@Override
	public abstract List<Vector3i> getPositions();
	
	@Override
	public abstract boolean containsPosition(Vector3i pos);
	
	
	/*
	 * Setters
	 */
	
	protected void setMinMaxPoints(List<Vector3i> points) {
		int minX = points.get(0).getX();
		int minY = points.get(0).getY();
		int minZ = points.get(0).getZ();
		int maxX = minX;
		int maxY = minY;
		int maxZ = minZ;

		for (Vector3i v : points) {
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
	public Optional<World> getWorld() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWorld(World world) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean selectPrimary(Vector3i position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean selectSecondary(Vector3i position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector3i getPrimaryPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3i getSecondaryPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3i getMinimumPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3i getMaximumPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3i getCenter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void expand(Vector3i... changes) throws RegionOperationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contract(Vector3i... changes) throws RegionOperationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shift(Vector3i change) throws RegionOperationException {
		// TODO Auto-generated method stub
		
	}
	
	
}
