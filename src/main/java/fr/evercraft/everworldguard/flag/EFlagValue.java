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
package fr.evercraft.everworldguard.flag;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableMap;

import fr.evercraft.everapi.services.worldguard.flag.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;

public class EFlagValue<T> implements FlagValue<T> {	

	private ConcurrentMap<Group, T> values;
	
	public EFlagValue() {
		this.values = new ConcurrentHashMap<Group, T>();
	}
	
	public void set(Group group, T value) {
		if (value == null) {
			this.values.remove(group);
		} else {
			this.values.put(group, value);
		}
	}

	@Override
	public Optional<T> get(Group group) {
		return Optional.ofNullable(this.values.get(group));
	}
	
	@Override
	public Optional<T> getInherit(Group group) {
		T value = null;
		switch (group) {
			case OWNER :
				value = this.values.get(Group.OWNER);
				if (value != null) {
					return Optional.of(value);
				}
			case MEMBER :
				value = this.values.get(Group.MEMBER);
				if (value != null) {
					return Optional.of(value);
				}
			default :
				return this.get(Group.DEFAULT);
		}
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
