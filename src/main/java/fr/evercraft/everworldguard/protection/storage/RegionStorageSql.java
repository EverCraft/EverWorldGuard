package fr.evercraft.everworldguard.protection.storage;

import java.util.Set;
import java.util.UUID;

import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.services.worldguard.flag.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

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
	public void add(EProtectedRegion region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(EProtectedRegion region) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(Set<EProtectedRegion> regions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIdentifier(EProtectedRegion region, String identifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPriority(EProtectedRegion region, int priority) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setParent(EProtectedRegion region, ProtectedRegion parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <V> void setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		// TODO Auto-generated method stub
		
	}
}
