package fr.evercraft.everworldguard.service.index;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everworldguard.regions.EProtectedRegion;

public class ESetProtectedRegion implements SetProtectedRegion {

	private final TreeSet<ProtectedRegion> regions;
	
	public ESetProtectedRegion(Vector3i position, Set<EProtectedRegion> regions) {
		this.regions = new TreeSet<ProtectedRegion>();
		regions.stream()
			.filter(region -> region.containsPosition(position))
			.forEach(region -> this.regions.add(region));
	}
	
	@Override
	public Set<ProtectedRegion> getAll() {
		return ImmutableSet.copyOf(this.regions);
	}

	@Override
	public <V> V getFlag(User user, Set<Context> context, Flag<V> flag) {
		for (ProtectedRegion region : this.regions) {
			FlagValue<V> flag_value = region.getFlagInherit(flag);
			if (!flag_value.isEmpty()) {
				Optional<V> optValue = flag_value.contains(region.getGroup(user, context));
				if (optValue.isPresent()) {
					return optValue.get();
				}
			}
		}
		return flag.getDefault();
	}
}
