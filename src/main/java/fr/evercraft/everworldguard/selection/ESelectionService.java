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

import org.spongepowered.api.item.ItemType;

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
	private ItemType item;
	
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
		        	ESelectionService.this.plugin.getELogger().debug("Loading SelectionSubject '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
		        	
		            return subject;
		        }
		    });
		this.plugin.getGame().getEventManager().registerListeners(this.plugin, new ESelectionListener(this.plugin));
		this.plugin.getManagerCommands().loadSelect();
		this.reload();
	}
	
	public void reload() {
		this.item = this.plugin.getConfigs().getSelectItem();
		this.cache.cleanUp();
		this.subjects.forEach((uuid, subject) -> subject.reload());
	}
	
	public Optional<SubjectSelection> get(final UUID uuid) {
		return Optional.ofNullable(this.getSubject(uuid).orElse(null));
	}
	
	public Optional<ESelectionSubject> getSubject(final UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		try {
			if (!this.subjects.containsKey(uuid)) {
				return Optional.of(this.cache.get(uuid));
	    	}
	    	return Optional.ofNullable(this.subjects.get(uuid));
		} catch (ExecutionException e) {
			this.plugin.getELogger().warn("Error : Loading SelectionSubject (identifier='" + uuid + "';message='" + e.getMessage() + "')");
			return Optional.empty();
		}
	}
	
	public Optional<ESelectionSubject> getOnline(final UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		return Optional.ofNullable(this.subjects.get(uuid));
	}
	
	public boolean hasRegistered(final UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		try {
			return this.plugin.getGame().getServer().getPlayer(uuid).isPresent();
		} catch (IllegalArgumentException e) {}
		return false;
	}
	
	/**
	 * Ajoute un joueur à la liste
	 * @param identifier L'UUID du joueur
	 * @return 
	 */
	public ESelectionSubject registerPlayer(final UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESelectionSubject player = this.cache.getIfPresent(uuid);
		// Si le joueur est dans le cache
		if (player != null) {
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getELogger().debug("Loading player cache : " + uuid.toString());
		// Si le joueur n'est pas dans le cache
		} else {
			Chronometer chronometer = new Chronometer();
			player = new ESelectionSubject(this.plugin, uuid);
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getELogger().debug("Loading player '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
		}
		return player;
	}
	
	/**
	 * Supprime un joueur à la liste et l'ajoute au cache
	 * @param identifier L'UUID du joueur
	 */
	public void removePlayer(final UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESelectionSubject player = this.subjects.remove(uuid);
		// Si le joueur existe
		if (player != null) {
			this.cache.put(uuid, player);
			this.plugin.getELogger().debug("Unloading the player : " + uuid.toString());
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
	
	public ItemType getItem() {
		return this.item;
	}
}
