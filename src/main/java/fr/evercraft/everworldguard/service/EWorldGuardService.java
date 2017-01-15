package fr.evercraft.everworldguard.service;

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
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import fr.evercraft.everapi.java.Chronometer;
import fr.evercraft.everapi.services.worldguard.SubjectWorldGuard;
import fr.evercraft.everapi.services.worldguard.WorldGuardService;
import fr.evercraft.everworldguard.EverWorldGuard;

public class EWorldGuardService implements WorldGuardService {
	
	private final EverWorldGuard plugin;
	
	private final ConcurrentMap<UUID, EUserSubject> subjects;
	private final LoadingCache<UUID, EUserSubject> cache;

	public EWorldGuardService(final EverWorldGuard plugin) {		
		this.plugin = plugin;
		
		this.subjects = new ConcurrentHashMap<UUID, EUserSubject>();
		this.cache = CacheBuilder.newBuilder()
					    .maximumSize(100)
					    .expireAfterAccess(5, TimeUnit.MINUTES)
					    .removalListener(new RemovalListener<UUID, EUserSubject>() {
					    	/**
					    	 * Supprime un joueur du cache
					    	 */
							@Override
							public void onRemoval(RemovalNotification<UUID, EUserSubject> notification) {
								//EssentialsSubject.this.plugin.getManagerEvent().post(notification.getValue(), PermUserEvent.Action.USER_REMOVED);
							}
					    	
					    })
					    .build(new CacheLoader<UUID, EUserSubject>() {
					    	/**
					    	 * Ajoute un joueur au cache
					    	 */
					        @Override
					        public EUserSubject load(UUID uuid){
					        	Chronometer chronometer = new Chronometer();
					        	
					        	EUserSubject subject = new EUserSubject(EWorldGuardService.this.plugin, uuid);
					        	EWorldGuardService.this.plugin.getLogger().debug("Loading user '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
					            
					            //EssentialsSubject.this.plugin.getManagerEvent().post(subject, PermUserEvent.Action.USER_ADDED);
					            return subject;
					        }
					    });
	}

	@Override
	public Optional<SubjectWorldGuard> get(UUID uuid) {
		return Optional.ofNullable(this.getSubject(uuid).orElse(null));
	}
	
	public Optional<EUserSubject> getSubject(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		try {
			if (!this.subjects.containsKey(uuid)) {
				return Optional.of(this.cache.get(uuid));
	    	}
	    	return Optional.ofNullable(this.subjects.get(uuid));
		} catch (ExecutionException e) {
			this.plugin.getLogger().warn("Error : Loading user (identifier='" + uuid + "';message='" + e.getMessage() + "')");
			return Optional.empty();
		}
	}
	
	public Optional<EUserSubject> getOnline(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		return Optional.ofNullable(this.subjects.get(uuid));
	}
	
	@Override
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
	 */
	public void registerPlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		EUserSubject player = this.cache.getIfPresent(uuid);
		// Si le joueur est dans le cache
		if (player != null) {
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player cache : " + uuid.toString());
		// Si le joueur n'est pas dans le cache
		} else {
			Chronometer chronometer = new Chronometer();
			player = new EUserSubject(this.plugin, uuid);
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
		}
		//this.plugin.getManagerEvent().post(player, PermUserEvent.Action.USER_ADDED);
	}
	
	/**
	 * Supprime un joueur à la liste et l'ajoute au cache
	 * @param identifier L'UUID du joueur
	 */
	public void removePlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		EUserSubject player = this.subjects.remove(uuid);
		// Si le joueur existe
		if (player != null) {
			this.cache.put(uuid, player);
			//this.plugin.getManagerEvent().post(player, PermUserEvent.Action.USER_REMOVED);
			this.plugin.getLogger().debug("Unloading the player : " + uuid.toString());
		}
	}
	
	public Collection<EUserSubject> getAll() {
		Set<EUserSubject> list = new HashSet<EUserSubject>();
		list.addAll(this.subjects.values());
		list.addAll(this.cache.asMap().values());
		return list;
	}
	
	public Collection<EUserSubject> getOnlines() {
		return this.subjects.values();
	}
}
