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
package fr.evercraft.everworldguard.selection.selector;

import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import fr.evercraft.everapi.services.selection.SelectionType;
import fr.evercraft.everapi.services.selection.exception.SelectorSecondaryException;
import fr.evercraft.everworldguard.selection.ESelectionSubject;

public class ESphereSelector extends EEllipsoidSelector {
	public ESphereSelector(ESelectionSubject subject) {
		this(subject, null);
	}
	
	public ESphereSelector(ESelectionSubject subject, World world) {
		super(subject, world);
	}
	
	public ESphereSelector(ESelectionSubject subject, World world, Vector3i min, Vector3i max) {
		super(subject, world, min, max);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(@Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public boolean selectSecondary(Vector3i position) throws SelectorSecondaryException {
		if (this.center == null) {
			throw new SelectorSecondaryException("");
		}
		
		if (this.radius != null && position != null && (position.compareTo(this.radius) == 0)) {
            return false;
        }
		
		this.radius = position;
		
		if (position == null) {
			this.region.setRadius(Vector3d.ZERO);
		} else {
			double radius = Math.ceil(this.center.distance(this.radius));
			this.region.setRadius(Vector3d.from(radius, radius, radius));
		}
		
		this.subject.describeCUI();
		return true;
	}
	
	@Override
	public SelectionType getType() {
		return SelectionType.SPHERE;
	}
}
