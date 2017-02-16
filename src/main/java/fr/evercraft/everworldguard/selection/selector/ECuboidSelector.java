package fr.evercraft.everworldguard.selection.selector;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;

import fr.evercraft.everapi.services.selection.SelectionRegion;
import fr.evercraft.everworldguard.selection.region.ESelectionCuboidRegion;

public class ECuboidSelector extends ESelector {
	private Vector3i position1;
	private Vector3i position2;
	private ESelectionCuboidRegion region;
	
	public ECuboidSelector() {
		this(null);
	}
	
	public ECuboidSelector(World world) {
		super();
		this.region = new ESelectionCuboidRegion(world, Vector3i.ZERO, Vector3i.ZERO);
	}
	
	@Override
	public Optional<World> getWorld() {
		return this.region.getWorld();
	}

	@Override
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
	public void clear() {
		this.position1 = null;
		this.position2 = null;
		
		this.recalculate();
	}

	@Override
	public int getVolume() {
		return this.region.getVolume();
	}

	@Override
	public Optional<Vector3i> getPrimaryPosition() {
		return Optional.ofNullable(this.position1);
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
	public SelectionRegion.Type getType() {
		return SelectionRegion.Type.CUBOID;
	}

	@Override
	public Optional<SelectionRegion> getRegion() {
		return Optional.of(this.region);
	}

	@Override
	public boolean expand(Vector3i... changes) {
		return this.region.expand(changes);
	}

	@Override
	public boolean contract(Vector3i... changes) {
		return this.region.expand(changes);
	}

	@Override
	public boolean shift(Vector3i change) {
		return this.region.expand(change);
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
}
