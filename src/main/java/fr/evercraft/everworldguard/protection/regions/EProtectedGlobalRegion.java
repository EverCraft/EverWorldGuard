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

import java.awt.geom.Area;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

public class EProtectedGlobalRegion extends EProtectedRegion implements ProtectedRegion.Global {
	
	public EProtectedGlobalRegion(EWWorld world, UUID identifier, String name) {
		this(world, identifier, name, false);
	}
	
	public EProtectedGlobalRegion(EWWorld world, UUID identifier, String name, boolean transientRegion) {
		super(world, identifier, name, transientRegion);

		this.min = Vector3i.ZERO;
		this.max = Vector3i.ZERO;
	}

	@Override
	public boolean isPhysicalArea() {
		return false;
	}

	@Override
	public List<Vector3i> getPoints() {
		return ImmutableList.of(Vector3i.ZERO);
	}

	@Override
	public boolean containsPosition(final Vector3i pos) {
		return true;
	}
	
	@Override
	public boolean containsChunck(final Vector3i pos) {
		return true;
	}

	@Override
	public Optional<Area> toArea() {
		return Optional.empty();
	}

	@Override
	public List<ProtectedRegion> getIntersectingRegions(final Collection<ProtectedRegion> regions) {
		return Collections.emptyList();
	}

	@Override
	public int getVolume() {
		return 0;
	}
}
