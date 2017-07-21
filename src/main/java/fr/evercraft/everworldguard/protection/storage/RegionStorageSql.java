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
package fr.evercraft.everworldguard.protection.storage;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedGlobalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;

public class RegionStorageSql implements RegionStorage {
	
	private final EverWorldGuard plugin;
	
	private final EWWorld world;
	
	public RegionStorageSql(EverWorldGuard plugin, EWWorld world) {		
		this.plugin = plugin;
		this.world = world;
	}
	
	@Override
	public void reload() {}
	
	public boolean isSql() {
    	return true;
    }

	@Override
	public CompletableFuture<Set<EProtectedRegion>> getAll() {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		Set<EProtectedRegion> regions = this.plugin.getDataBases().getAllRegions(this.world);
	    		if (regions.stream().filter(region -> region.getType().equals(ProtectedRegion.Types.GLOBAL)).findAny().isPresent()) {
	    			return regions;
	    		}
	    		
	    		this.plugin.getELogger().info("Aucune region GLOBAL");
	    		EProtectedRegion global = new EProtectedGlobalRegion(this.world, UUID.randomUUID(), EProtectionService.GLOBAL_REGION);
	    		if(this.plugin.getDataBases().insertRegion(connection, this.world.getUniqueId(), global.getId(), global.getName(), global.getType(), global.getPriority(), null)) {
	    			Builder<EProtectedRegion> build = ImmutableSet.builder();
	    			return build.addAll(regions)
	    					.add(global)
	    					.build();
	    		}
			} catch (ServerDisableException e) {
				e.execute();
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
			return ImmutableSet.of();
		}, this.plugin.getThreadAsync());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> CompletableFuture<Boolean> add(EProtectedRegion region) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		UUID parent = region.getParent().isPresent() ? region.getParent().get().getId() : null;
	    		
	    		if(!this.plugin.getDataBases().insertRegion(connection, this.world.getUniqueId(), region.getId(), region.getName(), region.getType(), region.getPriority(), parent)) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().insertPositions(connection, this.world.getUniqueId(), region.getId(), region.getPoints())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().insertUser(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, region.getOwners().getPlayers())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().insertUser(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, region.getMembers().getPlayers())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().insertGroup(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, region.getOwners().getGroups())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().insertGroup(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, region.getMembers().getGroups())) {
	    			return false;
	    		}
	    		
	    		for (Entry<Flag<?>, FlagValue<?>> flag : region.getFlags().entrySet()) {
	    			for (Entry<Group, ?> value : flag.getValue().getAll().entrySet()) {
		    			if(!this.plugin.getDataBases().insertFlag(connection, this.world.getUniqueId(), region.getId(), (Flag<V>) flag.getKey(), value.getKey(), (V) value.getValue())) {
			    			return false;
			    		}
		    		}
	    		}
	    		
	    		return true;
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> setName(EProtectedRegion region, String name) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().updateName(connection, this.world.getUniqueId(), region.getId(), name);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> setPriority(EProtectedRegion region, int priority) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().updatePriority(connection, this.world.getUniqueId(), region.getId(), priority);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> setParent(EProtectedRegion region, @Nullable ProtectedRegion parent) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().updateParent(connection, this.world.getUniqueId(), region.getId(), (parent != null) ? parent.getId() : null);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public <V> CompletableFuture<Boolean> setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		if (value == null) {
	    			return this.plugin.getDataBases().deleteFlag(connection, this.world.getUniqueId(), region.getId(), flag, group);
	    		} else if (region.getFlag(flag).get(group).isPresent()) {
	    			return this.plugin.getDataBases().updateFlag(connection, this.world.getUniqueId(), region.getId(), flag, group, value);
	    		} else {
	    			return this.plugin.getDataBases().insertFlag(connection, this.world.getUniqueId(), region.getId(), flag, group, value);
	    		}
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().insertUser(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, players);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().insertGroup(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, groups);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().insertUser(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, players);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addMemberGroup(EProtectedRegion region, Set<String> groups) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().insertGroup(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, groups);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().deleteUser(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, players);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().deleteGroup(connection, this.world.getUniqueId(), region.getId(), Groups.OWNER, groups);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().deleteUser(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, players);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().deleteGroup(connection, this.world.getUniqueId(), region.getId(), Groups.MEMBER, groups);
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeClearParent(EProtectedRegion region, Set<EProtectedRegion> regions) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		if(!this.plugin.getDataBases().deleteRegion(connection, this.world.getUniqueId(), region.getId())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().deletePositions(connection, this.world.getUniqueId(), region.getId())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().deleteFlag(connection, this.world.getUniqueId(), region.getId())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().deleteUser(connection, this.world.getUniqueId(), region.getId())) {
	    			return false;
	    		}
	    		
	    		if(!this.plugin.getDataBases().deleteGroup(connection, this.world.getUniqueId(), region.getId())) {
	    			return false;
	    		}
	    		
	    		for (EProtectedRegion parent : regions) {
	    			if(!this.plugin.getDataBases().updateParent(connection, this.world.getUniqueId(), parent.getId(), null)) {
		    			return false;
		    		}
	    		}
	    		
	    		return true;
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeRemoveChildren(Set<EProtectedRegion> regions) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		for (EProtectedRegion region : regions) {
	    			if (region.getType().equals(ProtectedRegion.Types.GLOBAL)) {
	    				if(!this.plugin.getDataBases().updateParent(connection, this.world.getUniqueId(), region.getId(), null)) {
			    			return false;
			    		}
	    			} else {
			    		if(!this.plugin.getDataBases().deleteRegion(connection, this.world.getUniqueId(), region.getId())) {
			    			return false;
			    		}
			    		
			    		if(!this.plugin.getDataBases().deletePositions(connection, this.world.getUniqueId(), region.getId())) {
			    			return false;
			    		}
			    		
			    		if(!this.plugin.getDataBases().deleteFlag(connection, this.world.getUniqueId(), region.getId())) {
			    			return false;
			    		}
			    		
			    		if(!this.plugin.getDataBases().deleteUser(connection, this.world.getUniqueId(), region.getId())) {
			    			return false;
			    		}
			    		
			    		if(!this.plugin.getDataBases().deleteGroup(connection, this.world.getUniqueId(), region.getId())) {
			    			return false;
			    		}
	    			}
	    		}
	    		
	    		return true;
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> redefine(EProtectedRegion region, EProtectedRegion newRegion) {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		if (!region.getType().equals(newRegion.getType())) {
	    			if(!this.plugin.getDataBases().updateType(connection, this.world.getUniqueId(), region.getId(), newRegion.getType())) {
		    			return false;
		    		}
	    		}
	    		
	    		if (!region.getType().equals(ProtectedRegion.Types.TEMPLATE)) {
	    			if(!this.plugin.getDataBases().deletePositions(connection, this.world.getUniqueId(), region.getId())) {
		    			return false;
		    		}
	    		}
	    		
	    		if (!newRegion.getType().equals(ProtectedRegion.Types.TEMPLATE)) {
		    		if(!this.plugin.getDataBases().insertPositions(connection, this.world.getUniqueId(), region.getId(), newRegion.getPoints())) {
		    			return false;
		    		}
	    		}
	    		
	    		return true;
			} catch (ServerDisableException e) {
				e.execute();
				return false;
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
		}, this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> clearAll() {
		return CompletableFuture.supplyAsync(() -> {
			Connection connection = null;
			try {
	    		connection = this.plugin.getDataBases().getConnection();
	    		
	    		return this.plugin.getDataBases().clear(connection);
			} catch (ServerDisableException e) {
				e.execute();
			} finally {
				try {if (connection != null) connection.close();} catch (SQLException e) {}
		    }
			return false;
		}, this.plugin.getThreadAsync());
	}
}
