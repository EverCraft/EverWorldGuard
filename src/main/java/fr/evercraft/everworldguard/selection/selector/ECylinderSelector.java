package fr.evercraft.everworldguard.selection.selector;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.RegionOperationException;
import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everapi.services.selection.Selector;
import fr.evercraft.everapi.services.selection.SelectorSecondaryException;
import fr.evercraft.everworldguard.selection.region.ESelectionCylinderRegion;

public class ECylinderSelector extends ESelector implements Selector.Cylinder {
	private Vector3i center;
	private Vector3i radius;
	private final ESelectionCylinderRegion region;
	
	public ECylinderSelector() {
		this(null);
	}
	
	public ECylinderSelector(World world) {
		super();
		this.region = new ESelectionCylinderRegion(world, Vector3i.ZERO, Vector3d.ZERO, 0, 0);
	}
	
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	public void setWorld(@Nullable World world) {
		this.region.setWorld(world);
	}

	@Override
	public boolean selectPrimary(Vector3i position) {
		this.center = position;
		
		this.region.setCenter(position);
		this.region.setMinimumY(position.getY());
		this.region.setMaximumY(position.getY());
		this.region.setRadius(Vector3d.ZERO);
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
		
		this.region.setY(position.getY());
		this.region.setRadius(this.center.sub(this.radius).toDouble());
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
		return true;
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
	public boolean expand(Vector3i... changes) throws RegionOperationException {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.expand(changes)) return false;
		
		this.center = this.region.getPrimaryPosition();
		return true;
	}

	@Override
	public boolean contract(Vector3i... changes) throws RegionOperationException {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.contract(changes)) return false;
		
		this.center = this.region.getPrimaryPosition();
		return true;
	}

	@Override
	public boolean shift(Vector3i change) {
		if (this.center == null || this.radius == null) return false;
		if (!this.region.shift(change)) return false;
		
		this.center = this.region.getPrimaryPosition();
		this.radius = null;
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

	@Override
	public Optional<SelectionRegion.Cuboid> getRegionCuboid() {
		return Optional.empty();
	}

	@Override
	public Optional<SelectionRegion.Polygonal> getRegionPolygonal() {
		return Optional.empty();
	}

	@Override
	public Optional<SelectionRegion.Cylinder> getRegionCylinder() {
		if (this.center == null || this.radius == null) return Optional.empty();
		return Optional.of(this.region);
	}
}
