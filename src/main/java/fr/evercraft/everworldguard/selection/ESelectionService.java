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
package fr.evercraft.everworldguard.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.evercraft.everapi.services.selection.SelectionService;
import fr.evercraft.everapi.services.selection.SubjectSelection;
import fr.evercraft.everapi.util.Chronometer;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.selection.cui.CUIChannel;

public class ESelectionService implements SelectionService {
	
	private final EverWorldGuard plugin;
	
	private final ConcurrentMap<UUID, ESelectionSubject> subjects;
	private final LoadingCache<UUID, ESelectionSubject> cache;
	
	private final CUIChannel cuiChannel;
	
	public ESelectionService(final EverWorldGuard plugin) {		
		this.plugin = plugin;
		
		this.cuiChannel = new CUIChannel(this.plugin);
		this.subjects = new ConcurrentHashMap<UUID, ESelectionSubject>();
		this.cache = CacheBuilder.newBuilder()
					    .maximumSize(100)
					    .expireAfterAccess(1, TimeUnit.HOURS)
					    .build(new CacheLoader<UUID, ESelectionSubject>() {
					    	/**
					    	 * Ajoute un joueur au cache
					    	 */
					        @Override
					        public ESelectionSubject load(UUID uuid){
					        	Chronometer chronometer = new Chronometer();
					        	
					        	ESelectionSubject subject = new ESelectionSubject(ESelectionService.this.plugin, uuid);
					        	ESelectionService.this.plugin.getLogger().debug("Loading SelectionSubject '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
					        	
					            return subject;
					        }
					    });
	}
	
	public Optional<SubjectSelection> get(UUID uuid) {
		return Optional.ofNullable(this.getSubject(uuid).orElse(null));
	}
	
	public Optional<ESelectionSubject> getSubject(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		try {
			if (!this.subjects.containsKey(uuid)) {
				return Optional.of(this.cache.get(uuid));
	    	}
	    	return Optional.ofNullable(this.subjects.get(uuid));
		} catch (ExecutionException e) {
			this.plugin.getLogger().warn("Error : Loading SelectionSubject (identifier='" + uuid + "';message='" + e.getMessage() + "')");
			return Optional.empty();
		}
	}
	
	public Optional<ESelectionSubject> getOnline(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		return Optional.ofNullable(this.subjects.get(uuid));
	}
	
	public boolean hasRegistered(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		try {
			return this.plugin.getGame().getServer().getPlayer(uuid).isPresent();
		} catch (IllegalArgumentException e) {}
		return false;
	}
	
	/**
	 * Rechargement : Vide le cache et recharge tous les joueurs
	 */
	public void reload() {		
		this.cache.cleanUp();
	}
	
	/**
	 * Ajoute un joueur à la liste
	 * @param identifier L'UUID du joueur
	 * @return 
	 */
	public ESelectionSubject registerPlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESelectionSubject player = this.cache.getIfPresent(uuid);
		// Si le joueur est dans le cache
		if (player != null) {
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player cache : " + uuid.toString());
		// Si le joueur n'est pas dans le cache
		} else {
			Chronometer chronometer = new Chronometer();
			player = new ESelectionSubject(this.plugin, uuid);
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
		}
		return player;
	}
	
	/**
	 * Supprime un joueur à la liste et l'ajoute au cache
	 * @param identifier L'UUID du joueur
	 */
	public void removePlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESelectionSubject player = this.subjects.remove(uuid);
		// Si le joueur existe
		if (player != null) {
			this.cache.put(uuid, player);
			this.plugin.getLogger().debug("Unloading the player : " + uuid.toString());
		}
	}
	
	public Collection<ESelectionSubject> getAll() {
		Set<ESelectionSubject> list = new HashSet<ESelectionSubject>();
		list.addAll(this.subjects.values());
		list.addAll(this.cache.asMap().values());
		return list;
	}

	public CUIChannel getCUIChannel() {
		return this.cuiChannel;
	}
}
