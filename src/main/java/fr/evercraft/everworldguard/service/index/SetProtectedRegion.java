package fr.evercraft.everworldguard.service.index;

import java.util.Set;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everworldguard.flag.FlagValue;
import fr.evercraft.everworldguard.regions.ProtectedRegion;

public class SetProtectedRegion {

	private final Set<ProtectedRegion> regions;
	
	public SetProtectedRegion(Vector3i positon, Set<ProtectedRegion> regions) {
		Builder<ProtectedRegion> builder = ImmutableSet.builder();
		regions.stream()
			.filter(region -> region.containsPosition(positon))
			.forEach(region -> builder.add(region));
		this.regions = builder.build();
	}
	
	public <T extends Flag<V>, V> FlagValue<V> getFlag(T flag) {
		return null;
	}
	
	public Set<ProtectedRegion> getRegions() {
		return this.regions;
	}
}
