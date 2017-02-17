package fr.evercraft.everworldguard.selection.selector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.RegionOperationException;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everworldguard.selection.region.ESelectionPolygonalRegion;

public class EPolygonalSelector extends ESelector implements Selector.Polygonal {
	
	private final List<Vector3i> positions;
	private final ESelectionPolygonalRegion region;
	
	public EPolygonalSelector() {
		this(null);
	}
	
	public EPolygonalSelector(World world) {
		super();
		
		this.positions = new ArrayList<Vector3i>();
		this.region = new ESelectionPolygonalRegion(world, Arrays.asList(Vector3i.ZERO));
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(@Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public boolean selectPrimary(@Nullable Vector3i position) {
		this.positions.clear();
		
		if (position != null) {
			this.positions.add(position);
        }
		
		this.recalculate();
		return true;
	}

	@Override
	public boolean selectSecondary(Vector3i position) {
		if (position == null) {
            if (this.positions.isEmpty())return false;
            this.positions.get(this.positions.size() - 1);
        } else {
        	this.positions.add(position);
        }
		this.recalculate();
		return true;
	}

	@Override
	public boolean clear() {
		this.positions.clear();
		
		this.recalculate();
		return true;
	}

	@Override
	public int getVolume() {
		return this.region.getVolume();
	}

	@Override
	public Optional<Vector3i> getPrimaryPosition() {
		if (this.positions.size() >= 2) return Optional.empty();
		return Optional.of(this.positions.get(0));
	}
	
	public void recalculate() {
		if (this.positions.size() < 2) {
			this.region.setPositions(Arrays.asList(Vector3i.ZERO));
		} else {
			this.region.setPositions(this.positions);
		}
	}
	
	@Override
	public boolean expand(Vector3i... changes) throws RegionOperationException {
		if (this.positions.size() >= 2) return false;
		if (!this.region.expand(changes)) return false;
		
		this.positions.clear();
		this.positions.addAll(this.region.getPositions());
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) throws RegionOperationException {
		if (this.positions.size() >= 2) return false;
		if (!this.region.contract(changes)) return false;
		
		this.positions.clear();
		this.positions.addAll(this.region.getPositions());
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
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

	@Override
	public Optional<SelectionRegion.Cuboid> getRegionCuboid() {
		return Optional.empty();
	}

	@Override
	public Optional<SelectionRegion.Polygonal> getRegionPolygonal() {
		if (this.positions.isEmpty()) return Optional.empty();
		return Optional.of(this.region);
	}

	@Override
	public Optional<SelectionRegion.Cylinder> getRegionCylinder() {
		return Optional.empty();
	}
}
