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
package fr.evercraft.everworldguard.protection.flag;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;

public class EFlagValue<T> implements FlagValue<T> {	

	private ConcurrentMap<Group, T> values;
	
	public EFlagValue() {
		this.values = new ConcurrentHashMap<Group, T>();
	}
	
	public void set(final Group group, @Nullable final T value) {
		if (value == null) {
			this.values.remove(group);
		} else {
			this.values.put(group, value);
		}
	}

	@Override
	public Optional<T> get(final Group group) {
		return Optional.ofNullable(this.values.get(group));
	}
	
	@Override
	public Optional<T> getInherit(final Group group) {
		T value = null;
		
		if (group.equals(Groups.OWNER)) {
			value = this.values.get(Groups.OWNER);
			if (value != null) {
				return Optional.of(value);
			}
		}
		
		if (group.equals(Groups.MEMBER)) {
			value = this.values.get(Groups.MEMBER);
			if (value != null) {
				return Optional.of(value);
			}
		}
		
		return this.get(Groups.DEFAULT);
	}
	
	@Override
	public Map<Group, T> getAll() {
		return ImmutableMap.copyOf(this.values);
	}
	
	@Override
	public boolean isEmpty() {
		return this.values.isEmpty();
	}
}
