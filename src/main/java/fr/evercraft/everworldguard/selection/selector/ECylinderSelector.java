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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.CUIRegion;
import fr.evercraft.everapi.services.selection.RegionOperationException;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everapi.services.selection.SelectorSecondaryException;
import fr.evercraft.everworldguard.selection.ESelectionSubject;
import fr.evercraft.everworldguard.selection.cui.CylinderCuiMessage;
import fr.evercraft.everworldguard.selection.cui.MinMaxCuiMessage;
import fr.evercraft.everworldguard.selection.cui.PointCuiMessage;
import fr.evercraft.everworldguard.selection.cui.ShapeCuiMessage;
import fr.evercraft.everworldguard.selection.region.ESelectionCylinderRegion;

public class ECylinderSelector extends ESelector implements Selector.Cylinder, CUIRegion {
	private Vector3i center;
	private Vector3i radius;
	private final ESelectionCylinderRegion region;
	
	public ECylinderSelector(ESelectionSubject subject) {
		this(subject, null);
	}
	
	public ECylinderSelector(ESelectionSubject subject, World world) {
		super(subject);
		this.region = new ESelectionCylinderRegion(world, Vector3i.ZERO, Vector3d.ZERO, 0, 0);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(@Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public int getVolume() {
		return this.region.getVolume();
	}

	@Override
	public Optional<Vector3i> getPrimaryPosition() {
		return Optional.ofNullable(this.center);
	}
	
	@Override
	public boolean selectPrimary(Vector3i position) {
		this.center = position;
		
		if (position == null) {
			this.region.setCenter(Vector3i.ZERO);
			this.region.setMinimumY(0);
			this.region.setMaximumY(0);
		} else {
			this.region.setCenter(position);
			this.region.setMinimumY(position.getY());
			this.region.setMaximumY(position.getY());
		}
		this.region.setRadius(Vector3d.ZERO);
		
		// CUI
		this.subject.describeCUI();
		return true;
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
		
		if (this.radius == null) {
			this.region.setRadius(Vector3d.ZERO);
		} else {
			this.region.setY(position.getY());
			this.region.extendRadius(this.center.sub(this.radius).toDouble());
		}
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public boolean clear() {
		this.center = null;
		this.radius = null;
		
		this.region.setCenter(Vector3i.ZERO);
		this.region.setMinimumY(0);
		this.region.setMaximumY(0);
		this.region.setRadius(Vector3d.ZERO);
		
		// CUI
		this.subject.describeCUI();
		return true;
	}
	
	@Override
	public boolean expand(Vector3i... changes) throws RegionOperationException {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.expand(changes)) return false;
		
		this.center = this.region.getPrimaryPosition();
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) throws RegionOperationException {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.contract(changes)) return false;
		
		this.center = this.region.getPrimaryPosition();
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.shift(change)) return false;
		
		this.center = this.region.getPrimaryPosition();
		this.radius = null;
		
		// CUI
		this.subject.describeCUI();
		return true;
	}

	@Override
	public List<Vector3i> getPositions() {
		ImmutableList.Builder<Vector3i> builder = ImmutableList.builder();
		if (this.center != null) {
			builder.add(this.center);
		}
		if (this.radius != null) {
			builder.add(this.radius);
		}
		return builder.build();
	}
	
	@Override
	public Optional<SelectionRegion> getRegion() {
		if (this.center == null || this.radius == null) return Optional.empty();
		return Optional.of(this.region);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SelectionRegion> Optional<T> getRegion(Class<T> type) {
		if (!type.equals(SelectionRegion.Cylinder.class)) return Optional.empty();
		if (this.center == null || this.radius == null) return Optional.empty();
		
		return Optional.of((T) this.region);
	}

	@Override
	public void describeCUI() {
		this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
		
		if (this.center == null && this.radius == null) return;
		this.subject.dispatchCUIEvent(new CylinderCuiMessage(this.region.getCenter(), this.region.getRadius()));
		this.subject.dispatchCUIEvent(new MinMaxCuiMessage(this.region.getMinimumY(), this.region.getMaximumY()));
	}

	@Override
	public void describeLegacyCUI() {
		this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
		
		if (this.center == null && this.radius == null) return;
	
		int volume = this.getVolume();
		this.subject.dispatchCUIEvent(new PointCuiMessage(0, this.region.getMinimumPoint(), volume));
		this.subject.dispatchCUIEvent(new PointCuiMessage(1, this.region.getMaximumPoint(), volume));
	}

	@Override
	public int getProtocolVersion() {
		return 1;
	}

	@Override
	public String getTypeID() {
		return "cylinder";
	}

	@Override
	public String getLegacyTypeID() {
		return "cuboid";
	}
}
