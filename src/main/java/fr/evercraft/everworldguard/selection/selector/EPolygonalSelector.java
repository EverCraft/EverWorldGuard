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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everapi.services.selection.exception.RegionOperationException;
import fr.evercraft.everworldguard.selection.ESelectionSubject;
import fr.evercraft.everworldguard.selection.cui.CUIRegion;
import fr.evercraft.everworldguard.selection.cui.MinMaxCuiMessage;
import fr.evercraft.everworldguard.selection.cui.Point2DCuiMessage;
import fr.evercraft.everworldguard.selection.cui.ShapeCuiMessage;
import fr.evercraft.everworldguard.selection.region.ESelectionPolygonalRegion;

public class EPolygonalSelector extends ESelector implements Selector.Polygonal, CUIRegion {
	
	private final List<Vector3i> positions;
	private final ESelectionPolygonalRegion region;
	
	public EPolygonalSelector(final ESelectionSubject subject) {
		this(subject, null);
	}
	
	public EPolygonalSelector(final ESelectionSubject subject, final World world) {
		super(subject);
		
		this.positions = new ArrayList<Vector3i>();
		this.region = new ESelectionPolygonalRegion(world, Arrays.asList(Vector3i.ZERO));
	}
	
	public EPolygonalSelector(final ESelectionSubject subject, final World world, final Vector3i min, final Vector3i max) {
		super(subject);
		
		this.positions = new ArrayList<Vector3i>();
		this.positions.add(min);
		this.positions.add(Vector3i.from(min.getX(), min.getY(), max.getZ()));
		this.positions.add(max);
		this.positions.add(Vector3i.from(max.getX(), min.getY(), min.getZ()));
		this.region = new ESelectionPolygonalRegion(world, this.positions);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(final @Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public boolean selectPrimary(final @Nullable Vector3i position) {
		this.positions.clear();
		
		if (position == null) {
			this.recalculate();
			
			// CUI
			this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
		} else {
			this.positions.add(position);
			this.recalculate();

			// CUI
			this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
			this.subject.dispatchCUIEvent(new Point2DCuiMessage(0, position, this.getVolume()));
			this.subject.dispatchCUIEvent(new MinMaxCuiMessage(this.region.getMinimumPoint().getY(), this.region.getMaximumPoint().getY()));
		}
		return true;
	}

	@Override
	public boolean selectSecondary(final Vector3i position) {
		if (position == null) {
            if (this.positions.isEmpty())return false;
            this.positions.remove(this.positions.size() - 1);
            this.recalculate();
            
            // CUI
            this.subject.describeCUI();
            return true;
        } else if (this.positions.isEmpty() || !this.positions.get(this.positions.size() - 1).equals(position)) {
        	this.positions.add(position);
        	this.recalculate();
        	
        	// CUI
        	this.subject.dispatchCUIEvent(new Point2DCuiMessage(this.positions.size() - 1, position, this.getVolume()));
    		this.subject.dispatchCUIEvent(new MinMaxCuiMessage(this.region.getMinimumPoint().getY(), this.region.getMaximumPoint().getY()));
    		return true;
        }
		return false;
	}

	@Override
	public boolean clear() {
		this.positions.clear();
		
		this.recalculate();
		return true;
	}

	@Override
	public int getVolume() {
		if (this.positions.size() < 2) {
			return 0;
		}
		return this.region.getArea();
	}

	@Override
	public Optional<Vector3i> getPrimaryPosition() {
		if (this.positions.size() >= 2) return Optional.empty();
		return Optional.of(this.positions.get(0));
	}
	
	public void recalculate() {
		if (this.positions.isEmpty()) {
			this.region.setPositions(Arrays.asList(Vector3i.ZERO));
		} else {
			this.region.setPositions(this.positions);
		}
	}
	
	@Override
	public boolean expand(final Vector3i... changes) throws RegionOperationException {
		if (this.positions.size() >= 2) return false;
		if (!this.region.expand(changes)) return false;
		
		this.positions.clear();
		this.positions.addAll(this.region.getPositions());
		return true;
	}

	@Override
	public boolean contract(final Vector3i... changes) throws RegionOperationException {
		if (this.positions.size() >= 2) return false;
		if (!this.region.contract(changes)) return false;
		
		this.positions.clear();
		this.positions.addAll(this.region.getPositions());
		return true;
	}

	@Override
	public boolean shift(final Vector3i change) {
		if (this.positions.size() >= 2) return false;
		if (!this.region.shift(change)) return false;
		
		this.positions.clear();
		this.positions.addAll(this.region.getPositions());
		return true;
	}

	@Override
	public List<Vector3i> getPositions() {
		return ImmutableList.copyOf(this.positions);
	}

	@Override
	public Optional<SelectionRegion> getRegion() {
		if (this.positions.isEmpty()) return Optional.empty();
		return Optional.of(this.region);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends SelectionRegion> Optional<T> getRegion(final Class<T> type) {
		if (!type.equals(SelectionRegion.Polygonal.class)) return Optional.empty();
		if (this.positions.isEmpty()) return Optional.empty();
		
		return Optional.of((T) this.region);
	}

	@Override
	public void describeCUI() {
		this.subject.dispatchCUIEvent(new ShapeCuiMessage(this.getTypeID()));
		
		final List<Vector3i> points = this.region.getPositions();
		if (points.isEmpty()) return;
		
		int volume = this.getVolume();
        for (int id = 0; id < points.size(); id++) {
        	this.subject.dispatchCUIEvent(new Point2DCuiMessage(id, points.get(id), volume));
        }

        this.subject.dispatchCUIEvent(new MinMaxCuiMessage(this.region.getMinimumPoint().getY(), this.region.getMaximumPoint().getY()));
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
		return "polygon2d";
	}

	@Override
	public String getLegacyTypeID() {
		return "polygon2d";
	}
}
