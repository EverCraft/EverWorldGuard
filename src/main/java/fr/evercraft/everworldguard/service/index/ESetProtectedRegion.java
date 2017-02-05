package fr.evercraft.everworldguard.service.index;

import java.util.Set;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public class ESetProtectedRegion implements SetProtectedRegion {

	private final Set<ProtectedRegion> regions;
	
	public ESetProtectedRegion(Vector3i position, Set<EProtectedRegion> regions) {
		Builder<ProtectedRegion> builder = ImmutableSet.builder();
		regions.stream()
			.filter(region -> region.containsPosition(position))
			.forEach(region -> builder.add(region));
		this.regions = builder.build();
	}
	
	@Override
	public Set<ProtectedRegion> getAll() {
		return this.regions;
	}

	@Override
	public <V> V getFlag(EUser user, Flag<V> flag) {
		
		return flag.getDefault();
	}
}
