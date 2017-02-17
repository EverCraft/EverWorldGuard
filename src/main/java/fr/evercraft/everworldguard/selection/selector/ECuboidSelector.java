package fr.evercraft.everworldguard.selection.selector;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everworldguard.selection.region.ESelectionCuboidRegion;

public class ECuboidSelector extends ESelector implements Selector.Cuboid {
	private Vector3i position1;
	private Vector3i position2;
	private final ESelectionCuboidRegion region;
	
	public ECuboidSelector() {
		this(null);
	}
	
	public ECuboidSelector(World world) {
		super();
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
		return true;
	}

	@Override
	public boolean selectSecondary(Vector3i position) {
		if (this.position2 != null && position != null && (position.compareTo(this.position2) == 0)) {
            return false;
        }
		
		this.position2 = position;
		this.recalculate();
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
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) {
		if (this.position1 == null || this.position2 == null) return false;
		if (!this.region.contract(changes)) return false;
		
		this.position1 = this.region.getPrimaryPosition();
		this.position2 = this.region.getSecondaryPosition();
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
}
