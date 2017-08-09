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
package fr.evercraft.everworldguard.protection.regions;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.region.Domain;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EDomain implements Domain {
	
	// MultiThreading
	private final ReadWriteLock lock;
	private final Lock write_lock;
	private final Lock read_lock;

	private final Set<UUID> players;
	private final Set<String> groups;

	public EDomain() {
		// MultiThreading
		this.lock = new ReentrantReadWriteLock();
		this.write_lock = this.lock.writeLock();
		this.read_lock = this.lock.readLock();
		
		this.players = new HashSet<UUID>();
		this.groups = new HashSet<String>();
	}

	public EDomain(final EDomain existing) {
		this();
		
		this.players.addAll(existing.getPlayers());
		this.groups.addAll(existing.getGroups());
	}
	
	public void init(final Set<UUID> players, final Set<String> name) {
		Preconditions.checkNotNull(players);
		Preconditions.checkNotNull(name);
		
		this.players.clear();
		this.groups.clear();
		
		this.players.addAll(players);
		this.groups.addAll(name);
	}
	
	/*
	 * Player
	 */
	
	public void addPlayer(final UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		this.write_lock.lock();
		try {
			this.players.add(uniqueId);
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public void removePlayer(final UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		this.write_lock.lock();
		try {
			this.players.remove(uniqueId);
		} finally {
			this.write_lock.unlock();
		}
	}
	
	@Override
	public Set<UUID> getPlayers() {
		this.read_lock.lock();
		try {
			return ImmutableSet.copyOf(this.players);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean containsPlayer(final UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		this.read_lock.lock();
		try {
			return this.players.contains(uniqueId);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	/*
	 * Groups
	 */
	
	public void addGroup(final String name) {
		Preconditions.checkNotNull(name);
		
		this.write_lock.lock();
		try {
			this.groups.add(name);
		} finally {
			this.write_lock.unlock();
		}
	}
	
	public void removeGroup(final String name) {
		Preconditions.checkNotNull(name);
		
		this.write_lock.lock();
		try {
			this.groups.remove(name);
		} finally {
			this.write_lock.unlock();
		}
	}
	
	@Override
	public Set<String> getGroups() {
		this.read_lock.lock();
		try {
			return ImmutableSet.copyOf(this.groups);
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public boolean containsGroup(final String group) {
		Preconditions.checkNotNull(group);
		
		this.read_lock.lock();
		try {
			return this.groups.contains(group);
		} finally {
			this.read_lock.unlock();
		}
	}

	@Override
	public boolean contains(final User player, final Set<Context> contexts) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(contexts);
		
		this.read_lock.lock();
		try {
			if (this.containsPlayer(player.getUniqueId())) {
				return true;
			}
			
			Optional<SubjectReference> group = player.getParents(contexts)
				.stream()
				.filter(subject -> this.groups.contains(subject.getSubjectIdentifier()))
				.findAny();
			
			return group.isPresent();
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public int size() {
		this.read_lock.lock();
		try {
			return this.groups.size() + this.players.size();
		} finally {
			this.read_lock.unlock();
		}
	}

	@Override
	public void clear() {
		this.read_lock.lock();
		try {
			this.groups.clear();
			this.players.clear();
		} finally {
			this.read_lock.unlock();
		}
	}
	
	@Override
	public String toString() {
		return "(players='" + this.players +"';groups='" + this.groups + "')";
	}

}
