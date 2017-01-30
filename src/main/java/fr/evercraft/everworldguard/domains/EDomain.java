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
package fr.evercraft.everworldguard.domains;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.server.user.EUser;
import fr.evercraft.everapi.services.worldguard.regions.Domain;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

public class EDomain implements Domain {

	private final Set<UUID> players;
	private final Set<String> groups;

	public EDomain() {
		this.players = new CopyOnWriteArraySet<UUID>();
		this.groups = new CopyOnWriteArraySet<String>();
	}

	public EDomain(EDomain existing) {
		this();
		
		this.players.addAll(existing.getPlayers());
		this.groups.addAll(existing.getGroups());
	}
	
	public void init(Set<UUID> players, Set<String> name) {
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
	
	@Override
	public void addPlayer(UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		this.players.add(uniqueId);
	}
	
	@Override
	public void removePlayer(UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		this.players.remove(uniqueId);
	}
	
	@Override
	public void addPlayer(EUser player) {
		Preconditions.checkNotNull(player);
		
		this.players.add(player.getUniqueId());
	}
	
	@Override
	public void removePlayer(EUser player) {
		Preconditions.checkNotNull(player);
		
		this.players.remove(player.getUniqueId());
	}
	
	@Override
	public Set<UUID> getPlayers() {
		return ImmutableSet.copyOf(this.players);
	}
	
	@Override
	public boolean containsPlayers(UUID uniqueId) {
		Preconditions.checkNotNull(uniqueId);
		
		return this.players.contains(uniqueId);
	}
	
	/*
	 * Groups
	 */
	
	@Override
	public void addGroup(String name) {
		Preconditions.checkNotNull(name);
		
		this.groups.add(name);
	}
	
	@Override
	public void removeGroup(String name) {
		Preconditions.checkNotNull(name);
		
		this.groups.remove(name);
	}
	
	@Override
	public void addGroup(Subject subject) {
		Preconditions.checkNotNull(subject);
		
		this.groups.add(subject.getIdentifier());
	}
	
	@Override
	public void removeGroup(Subject subject) {
		Preconditions.checkNotNull(subject);
		
		this.groups.remove(subject.getIdentifier());
	}
	
	@Override
	public Set<String> getGroups() {
		return ImmutableSet.copyOf(this.groups);
	}
	
	@Override
	public boolean containsGroups(String group) {
		Preconditions.checkNotNull(group);
		
		return this.groups.contains(group);
	}
	
	/*
	 * 
	 */
	
	@Override
	public void addAll(Domain other) {
		Preconditions.checkNotNull(other);
		
		for (UUID uuid : other.getPlayers()) {
			this.addPlayer(uuid);
		}
		for (String group : other.getGroups()) {
			this.addGroup(group);
		}
	}
	
	@Override
	public void removeAll(Domain other) {
		Preconditions.checkNotNull(other);
		
		for (UUID uuid : other.getPlayers()) {
			this.removePlayer(uuid);
		}
		for (String group : other.getGroups()) {
			this.removeGroup(group);
		}
	}

	@Override
	public boolean contains(EUser player, Set<Context> contexts) {
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(contexts);
		
		if (this.containsPlayers(player.getUniqueId())) {
			return true;
		}
		
		Optional<Subject> group = player.getParents()
			.stream()
			.filter(subject -> this.groups.contains(subject.getIdentifier()))
			.findAny();
		
		return group.isPresent();
	}
	
	@Override
	public int size() {
		return this.groups.size() + this.players.size();
	}

	@Override
	public void clear() {
		this.groups.clear();
		this.players.clear();
	}

	public void removeAll() {
		clear();
	}
	
	@Override
	public String toString() {
		return "(players=" + this.players +", groups=" + this.groups + ')';
	}

}
