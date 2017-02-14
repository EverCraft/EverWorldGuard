package fr.evercraft.everworldguard.service.storage.sql;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.exception.StorageException;
import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.regions.EProtectedRegion;
import fr.evercraft.everworldguard.service.index.EWWorld;
import fr.evercraft.everworldguard.service.storage.RegionStorage;

public class RegionStorageSql implements RegionStorage {
	
	@SuppressWarnings("unused")
	private final EverWorldGuard plugin;
	
	public RegionStorageSql(EverWorldGuard plugin, EWWorld world) {		
		this.plugin = plugin;
	}

	@Override
	public Set<EProtectedRegion> getAll() {
		return ImmutableSet.of();
	}

	@Override
	public void add(EProtectedRegion region) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(EProtectedRegion region) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(Set<EProtectedRegion> regions) throws StorageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveIdentifier(EProtectedRegion region, String identifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePriority(EProtectedRegion region, int priority) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveParent(EProtectedRegion region, ProtectedRegion parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <V> void saveFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAddOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAddOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAddMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveAddMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRemoveOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRemoveOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRemoveMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveRemoveMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}
}
