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

import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import java.awt.geom.Area;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

public class EProtectedTemplateRegion extends EProtectedRegion implements ProtectedRegion.Template {
	
	public EProtectedTemplateRegion(String id) {
		this(id, false);
	}
	
	public EProtectedTemplateRegion(String id, boolean transientRegion) {
		super(id, transientRegion);
		
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
	public boolean containsPosition(Vector3i pos) {
		return false;
	}

	@Override
	public Optional<Area> toArea() {
		return Optional.empty();
	}

	@Override
	public List<ProtectedRegion> getIntersectingRegions(Collection<ProtectedRegion> regions) {
		return Collections.emptyList();
	}

	@Override
	public int getVolume() {
		return 0;
	}
}
