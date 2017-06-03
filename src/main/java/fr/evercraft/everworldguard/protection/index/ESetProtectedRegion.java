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
package fr.evercraft.everworldguard.protection.index;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;

import com.flowpowered.math.vector.Vector3i;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.SetProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public class ESetProtectedRegion implements SetProtectedRegion {

	private final TreeSet<ProtectedRegion> regions;
	
	public ESetProtectedRegion(Vector3i position, Set<EProtectedRegion> regions) {
		this.regions = new TreeSet<ProtectedRegion>();
		regions.stream()
			.filter(region -> region.containsPosition(position))
			.forEach(region -> this.regions.add(region));
	}
	
	public ESetProtectedRegion(Set<ProtectedRegion> setView) {
		this.regions = new TreeSet<ProtectedRegion>(setView);
	}
	
	@Override
	public Set<ProtectedRegion> getAll() {
		return ImmutableSet.copyOf(this.regions);
	}

	@Override
	public <V> V getFlag(User user, Set<Context> context, Flag<V> flag) {
		for (ProtectedRegion region : this.regions) {
			Optional<V> flag_value = region.getFlagInherit(flag, region.getGroup(user, context));
			if (flag_value.isPresent()) {
				return flag_value.get();
			}
		}
		return flag.getDefault();
	}

	@Override
	public <V> V getFlagDefault(Flag<V> flag) {
		for (ProtectedRegion region : this.regions) {
			Optional<V> flag_value = region.getFlagInherit(flag, Groups.DEFAULT);
			if (flag_value.isPresent()) {
				return flag_value.get();
			}
		}
		return flag.getDefault();
	}
	
	@Override
	public <V> Optional<V> getFlagIfPresent(User user, Set<Context> context, Flag<V> flag) {
		for (ProtectedRegion region : this.regions) {
			Optional<V> flag_value = region.getFlagInherit(flag, region.getGroup(user, context));
			if (flag_value.isPresent()) {
				return Optional.ofNullable(flag_value.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public <V> Optional<V> getFlagDefaultIfPresent(Flag<V> flag) {
		for (ProtectedRegion region : this.regions) {
			Optional<V> flag_value = region.getFlagInherit(flag, Groups.DEFAULT);
			if (flag_value.isPresent()) {
				return Optional.ofNullable(flag_value.get());
			}
		}
		return Optional.empty();
	}
}
