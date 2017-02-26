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

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.CUIRegion;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everworldguard.selection.ESelectionSubject;
import fr.evercraft.everworldguard.selection.cui.PointCuiMessage;
import fr.evercraft.everworldguard.selection.region.ESelectionCuboidRegion;

public class ECuboidSelector extends ESelector implements Selector.Cuboid, CUIRegion {
	protected Vector3i position1;
	protected Vector3i position2;
	protected final ESelectionCuboidRegion region;
	
	public ECuboidSelector(ESelectionSubject subject) {
		this(subject, null);
	}
	
	public ECuboidSelector(ESelectionSubject subject, World world) {
		super(subject);
		this.region = new ESelectionCuboidRegion(world, Vector3i.ZERO, Vector3i.ZERO);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(@Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public boolean selectPrimary(Vector3i position) {
		if (this.position1 != null && position != null && (position.compareTo(this.position1) == 0)) {
            return false;
        }
		
		this.position1 = position;
		this.recalculate();
		
		if (this.position1 != null) {
			this.subject.dispatchCUIEvent(new PointCuiMessage(0, this.position1, this.getVolume()));
		}
		return true;
	}

	@Override
	public boolean selectSecondary(Vector3i position) {
		if (this.position2 != null && position != null && (position.compareTo(this.position2) == 0)) {
            return false;
        }
		
		this.position2 = position;
		this.recalculate();
		
		if (this.position2 != null) {
			this.subject.dispatchCUIEvent(new PointCuiMessage(1, this.position2, this.getVolume()));
		}
		return true;
	}

	@Override
	public boolean clear() {
		this.position1 = null;
		this.position2 = null;
		
		this.recalculate();
		return true;
	}

	@Override
	public int getVolume() {
		return this.region.getVolume();
	}

	@Override
	public Optional<Vector3i> getPrimaryPosition() {
		return Optional.ofNullable(this.position1);
	}
	
	@Override
	public Optional<Vector3i> getSecondaryPosition() {
		return Optional.ofNullable(this.position2);
	}
	
	public void recalculate() {
		if (this.position1 == null && this.position2 == null) {
			this.region.setPosition(Vector3i.ZERO, Vector3i.ZERO);
		} else if (this.position1 == null) {
			this.region.setPosition(this.position2, this.position2);
		} else if (this.position2 == null) {
			this.region.setPosition(this.position1, this.position1);
		} else {
			this.region.setPosition(this.position1, this.position2);
		}
	}
	
	@Override
	public boolean expand(Vector3i... changes) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.expand(changes)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		
		this.describeCUI();
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.contract(changes)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		
		this.describeCUI();
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.shift(change)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		return true;
	}

	@Override
	public List<Vector3i> getPositions() {
		ImmutableList.Builder<Vector3i> builder = ImmutableList.builder();
		if (this.position1 != null) {
			builder.add(this.position1);
		}
		if (this.position2 != null) {
			builder.add(this.position2);
		}
		return builder.build();
	}
	
	@Override
	public Optional<SelectionRegion> getRegion() {
		if (this.position1 == null || this.position2 == null) return Optional.empty();
		return Optional.of(this.region);
	}

	@Override
	public Optional<SelectionRegion.Cuboid> getRegionCuboid() {
		if (this.position1 == null || this.position2 == null) return Optional.empty();
		return Optional.of(this.region);
	}

	@Override
	public Optional<SelectionRegion.Polygonal> getRegionPolygonal() {
		return Optional.empty();
	}

	@Override
	public Optional<SelectionRegion.Cylinder> getRegionCylinder() {
		return Optional.empty();
	}

	@Override
	public void describeCUI() {
		int volume = this.getVolume();
		
		if (this.position1 != null) {
			this.subject.dispatchCUIEvent(new PointCuiMessage(0, this.position1, volume));
		}
		
		if (this.position2 != null) {
			this.subject.dispatchCUIEvent(new PointCuiMessage(1, this.position2, volume));
		}
	}

	@Override
	public void describeLegacyCUI() {
		this.describeCUI();
	}

	@Override
	public int getProtocolVersion() {
		return 1;
	}

	@Override
	public String getTypeID() {
		return "ellipsoid";
	}

	@Override
	public String getLegacyTypeID() {
		return "cuboid";
	}
}
