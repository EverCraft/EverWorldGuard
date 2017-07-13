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

import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everworldguard.selection.ESelectionSubject;
import fr.evercraft.everworldguard.selection.cui.CUIRegion;
import fr.evercraft.everworldguard.selection.cui.PointCuiMessage;
import fr.evercraft.everworldguard.selection.cui.ShapeCuiMessage;
import fr.evercraft.everworldguard.selection.region.ESelectionCuboidRegion;

public class ECuboidSelector extends ESelector implements Selector.Cuboid, CUIRegion {
	protected Vector3i position1;
	protected Vector3i position2;
	protected final ESelectionCuboidRegion region;
	
	public ECuboidSelector(final ESelectionSubject subject) {
		this(subject, null);
	}
	
	public ECuboidSelector(final ESelectionSubject subject, final World world) {
		super(subject);
		this.region = new ESelectionCuboidRegion(world, Vector3i.ZERO, Vector3i.ZERO);
	}
	
	public ECuboidSelector(final ESelectionSubject subject, final World world, final Vector3i min, final Vector3i max) {
		super(subject);
		
		this.position1 = min;
		this.position2 = max;
		this.region = new ESelectionCuboidRegion(world, min, max);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(final @Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public int getVolume() {
		return this.region.getArea();
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
	public boolean selectPrimary(final Vector3i position) {
		if (this.position1 != null && position != null && (position.compareTo(this.position1) == 0)) {
            return false;
        }
		
		this.position1 = position;
		this.recalculate();
		
		// CUI
		if (this.position1 != null) {
			this.subject.dispatchCUIEvent(new PointCuiMessage(0, this.position1, this.getVolume()));
		}
		return true;
	}

	@Override
	public boolean selectSecondary(final Vector3i position) {
		if (this.position2 != null && position != null && (position.compareTo(this.position2) == 0)) {
            return false;
        }
		
		this.position2 = position;
		this.recalculate();
		
		// CUI
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
		
		// CUI
		this.subject.describeCUI();
		return true;
	}
	
	@Override
	public boolean expand(final Vector3i... changes) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.expand(changes)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public boolean contract(final Vector3i... changes) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.contract(changes)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public boolean shift(final Vector3i change) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.shift(change)) return false;
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
		
		// CUI
		this.subject.describeCUI();
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

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SelectionRegion> Optional<T> getRegion(final Class<T> type) {
		if (!type.equals(SelectionRegion.Cuboid.class)) return Optional.empty();
		if (this.position1 == null || this.position2 == null) return Optional.empty();
		
		return Optional.of((T) this.region);
	}

	@Override
	public void describeCUI() {
		this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
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
		return 0;
	}

	@Override
	public String getTypeID() {
		return "cuboid";
	}

	@Override
	public String getLegacyTypeID() {
		return "cuboid";
	}
}
