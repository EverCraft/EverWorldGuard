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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import fr.evercraft.everapi.exception.PluginDisableException;
import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.plugin.EDataBase;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.flag.EFlagValue;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedGlobalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedTemplateRegion;

public class EWDataBases extends EDataBase<EverWorldGuard> {
	
	private static final String REGIONS = "regions";
	private static final String POSITIONS = "positions";
	private static final String USERS = "users";
	private static final String GROUPS = "groups";
	private static final String FLAGS = "flags";

	public EWDataBases(final EverWorldGuard plugin) throws PluginDisableException {
		super(plugin);
	}

	public boolean init() throws ServerDisableException {
		String regions =  "CREATE TABLE IF NOT EXISTS {table} ("
							+ "`uuid` VARCHAR(36) NOT NULL, "
							+ "`world` VARCHAR(36) NOT NULL, "
							+ "`name` VARCHAR(45) NOT NULL, "
							+ "`type` VARCHAR(45) NOT NULL, "
							+ "`priority` INT NOT NULL, "
							+ "`parent` VARCHAR(36), "
							+ "PRIMARY KEY (`uuid`, `world`));";
		initTable(this.getTableRegions(), regions);
		
		String positions =  "CREATE TABLE IF NOT EXISTS {table} ("
							+ "`region` VARCHAR(36) NOT NULL, "
							+ "`world` VARCHAR(36) NOT NULL, "
							+ "`id` INT NOT NULL, "
							+ "`x` INT NOT NULL, "
							+ "`y` INT NOT NULL, "
							+ "`z` INT NOT NULL, "
							+ "PRIMARY KEY (`id`, `region`, `world`), "
							+ "INDEX `fk_position_region_idx` (`region` ASC, `world` ASC), "
							+ "CONSTRAINT `fk_position_region` "
							+ " FOREIGN KEY (`region` , `world`) "
							+ " REFERENCES " + this.getTableRegions() + " (`uuid` , `world`) "
							+ " ON DELETE NO ACTION "
							+ " ON UPDATE NO ACTION);";
		initTable(this.getTablePositions(), positions);
		
		String users =  "CREATE TABLE IF NOT EXISTS {table} ("
							+ "`region` VARCHAR(36) NOT NULL, "
							+ "`world` VARCHAR(36) NOT NULL, "
							+ "`group` VARCHAR(45) NOT NULL , "
							+ "`uuid` VARCHAR(36) NOT NULL, "
							+ "PRIMARY KEY (`uuid`, `group`, `region`, `world`), "
							+ "INDEX `fk_users_region_idx` (`region` ASC, `world` ASC), "
							+ "CONSTRAINT `fk_users_region` "
							+ " FOREIGN KEY (`region` , `world`) "
							+ " REFERENCES " + this.getTableRegions() + " (`uuid` , `world`) "
							+ " ON DELETE NO ACTION "
							+ " ON UPDATE NO ACTION);";
		initTable(this.getTableUsers(), users);
		
		String groups =  "CREATE TABLE IF NOT EXISTS {table} ("
							+ "`region` VARCHAR(36) NOT NULL, "
							+ "`world` VARCHAR(36) NOT NULL, "
							+ "`group` VARCHAR(45) NOT NULL , "
							+ "`name` VARCHAR(50) NOT NULL, "
							+ "PRIMARY KEY (`name`, `group`, `region`, `world`), "
							+ "INDEX `fk_groups_region_idx` (`region` ASC, `world` ASC), "
							+ "CONSTRAINT `fk_groups_region` "
							+ " FOREIGN KEY (`region` , `world`) "
							+ " REFERENCES " + this.getTableRegions() + " (`uuid` , `world`) "
							+ " ON DELETE NO ACTION "
							+ " ON UPDATE NO ACTION);";
		initTable(this.getTableGroups(), groups);
		
		String flags =  "CREATE TABLE IF NOT EXISTS {table} ("
							+ "`region` VARCHAR(36) NOT NULL, "
							+ "`world` VARCHAR(36) NOT NULL, "
							+ "`group` VARCHAR(45) NOT NULL , "
							+ "`flag` VARCHAR(45) NOT NULL, "
							+ "`value` LONGTEXT NULL, "
							+ "PRIMARY KEY (`flag`, `group`, `world`, `region`), "
							+ "INDEX `fk_flags_region_idx` (`region` ASC, `world` ASC), "
							+ "CONSTRAINT `fk_flags_region` "
							+ " FOREIGN KEY (`region` , `world`) "
							+ " REFERENCES " + this.getTableRegions() + " (`uuid` , `world`) "
							+ " ON DELETE NO ACTION "
							+ " ON UPDATE NO ACTION);";
		initTable(this.getTableFlags(), flags);
		
		return true;
	}

	public String getTableRegions() {
		return this.getPrefix() + REGIONS;
	}
	
	public String getTablePositions() {
		return this.getPrefix() + POSITIONS;
	}
	
	public String getTableUsers() {
		return this.getPrefix() + USERS;
	}
	
	public String getTableGroups() {
		return this.getPrefix() + GROUPS;
	}
	
	public String getTableFlags() {
		return this.getPrefix() + FLAGS;
	}
	
	public Set<EProtectedRegion> getAllRegions(final EWWorld world) {
		Connection connection = null;
    	try {
    		connection = this.plugin.getDataBases().getConnection();
    		return this.getAllRegions(connection, world);
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {if (connection != null) connection.close();} catch (SQLException e) {}
	    }
		return ImmutableSet.of();
	}
	
	public Set<EProtectedRegion> getAllRegions(final Connection connection, final EWWorld world) {
		Map<UUID, EProtectedRegion> regions = new HashMap<UUID, EProtectedRegion>();
		Map<UUID, UUID> parents = new HashMap<UUID, UUID>();
		
		Map<UUID, List<Vector3i>> positions = this.getAllPositions(connection, world.getWorld().getUniqueId());
		Map<UUID, Map<Group, Set<UUID>>> users = this.getAllUsers(connection, world.getWorld().getUniqueId());
		Map<UUID, Map<Group, Set<String>>> groups = this.getAllGroups(connection, world.getWorld().getUniqueId());
		Map<UUID, Map<Flag<?>, EFlagValue<?>>> flags = this.getAllFlags(connection, world.getWorld().getUniqueId());
		
		PreparedStatement preparedStatement = null;
		String query = 	  "SELECT * " 
						+ "FROM `" + this.getTableRegions() + "` "
						+ "WHERE `world` = ? ;";
    	try {	
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, world.getUniqueId().toString());
			ResultSet list = preparedStatement.executeQuery();
			
			while (list.next()) {
				UUID identifier = null;
				try {
					identifier = UUID.fromString(list.getString("uuid"));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + identifier + "';world='" + world.getUniqueId() + "')");
					continue;
				}
				
				Optional<ProtectedRegion.Type> optType = this.plugin.getGame().getRegistry().getType(ProtectedRegion.Type.class, list.getString("type"));
				if (!optType.isPresent()) {
					this.plugin.getELogger().warn("Error : Type unknown (region='" + identifier + "';world='" + world.getUniqueId() + "';type='" + list.getString("type") + "')");
					continue;
				}
				
				EProtectedRegion region = null;
				String name = list.getString("name");
				
				ProtectedRegion.Type type = optType.get();
				if (type.equals(ProtectedRegion.Types.GLOBAL)) {
					region = new EProtectedGlobalRegion(world, identifier, name);
				} else if (type.equals(ProtectedRegion.Types.TEMPLATE)) {
					region = new EProtectedTemplateRegion(world, identifier, name);
				} else if (type.equals(ProtectedRegion.Types.CUBOID)) {
					List<Vector3i> vectors = positions.get(identifier);
					if (vectors != null && vectors.size() == 2) {
						region = new EProtectedCuboidRegion(world, identifier, name, vectors.get(0), vectors.get(1));
					} else {
						this.plugin.getELogger().warn("Error : The position number is incorrect (region='" + identifier + "';world='" + world.getUniqueId() + "';type='" + type.getName() + "')");
						continue;
					}
				} else if (type.equals(ProtectedRegion.Types.POLYGONAL)) {
					List<Vector3i> vectors = positions.get(identifier);
					if (vectors != null && !vectors.isEmpty()) {
						region = new EProtectedPolygonalRegion(world, identifier, name, vectors);
					} else {
						this.plugin.getELogger().warn("Error : The position number is incorrect (region='" + identifier + "';world='" + world.getUniqueId() + "';type='" + type.getName() + "')");
						continue;
					}
				} else {
					this.plugin.getELogger().warn("Error : Unsupported Type (region='" + identifier + "';world='" + world.getUniqueId() + "';type='" + type.getName() + "')");
					continue;
				}
				
				String parent = list.getString("parent"); 
				if (parent != null) {
					parents.put(identifier, UUID.fromString(list.getString("parent")));
				}
				
				int priority = list.getInt("priority");
				Set<UUID> owners = this.get(users, identifier, Groups.OWNER);
				Set<UUID> members = this.get(users, identifier, Groups.MEMBER);
				Set<String> group_owners = this.get(groups, identifier, Groups.OWNER);
				Set<String> group_members = this.get(groups, identifier, Groups.MEMBER);
				Map<Flag<?>, EFlagValue<?>> flag = flags.containsKey(identifier) ? flags.get(identifier) : ImmutableMap.of();
				
				region.init(priority, owners, group_owners, members, group_members, flag);
				regions.put(identifier, region);
			}
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the load of regions (world='" + world.getUniqueId() + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
    	
    	// Parents
		for (Entry<UUID, UUID> entry : parents.entrySet()) {
			EProtectedRegion region = regions.get(entry.getKey());
			EProtectedRegion parent = regions.get(entry.getValue());
			
			if (parent == null) {
				this.plugin.getELogger().warn("Unable to find the parent region '" + entry.getValue() + "' (region='" + region.getId() + "';world='" + world.getUniqueId() + "')");
				continue;
			}
			
			region.init(parent);
		}
    	
		return ImmutableSet.copyOf(regions.values());
	}
	
	public <T> Set<T> get(final Map<UUID, Map<Group, Set<T>>> users, final UUID identifier, final Group group) {
		Map<Group, Set<T>> map = users.get(identifier);
		if (map == null) return ImmutableSet.of();
		
		Set<T> set = map.get(group);
		if (set == null) return ImmutableSet.of();
		
		return set;
	}
	
	public Map<UUID, List<Vector3i>> getAllPositions(final Connection connection, final UUID world) {
		Map<UUID, List<Vector3i>> positions = new HashMap<UUID, List<Vector3i>>();
		PreparedStatement preparedStatement = null;
		
		String query = 	  "SELECT * " 
						+ "FROM `" + this.getTablePositions() + "` "
						+ "WHERE `world` = ? "
						+ "ORDER BY `id` ASC;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, world.toString());
			ResultSet list = preparedStatement.executeQuery();
			
			while (list.next()) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(list.getString("region"));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + list.getString("region") + "';world='" + world + "',x='" + list.getString("x") + "';y='" + list.getString("y") + "';z='" + list.getString("z") + "')");
					continue;
				}
				
				List<Vector3i> value = positions.get(uuid);
				if (value == null) {
					value = new ArrayList<Vector3i>();
					positions.put(uuid, value);
				}
				value.add(Vector3i.from(list.getInt("x"), list.getInt("y"), list.getInt("z")));
			}
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the load of the positions : (world='" + world + "') " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return positions;
	}
	
	public Map<UUID, Map<Group, Set<UUID>>> getAllUsers(final Connection connection, final UUID world) {
		Map<UUID, Map<Group, Set<UUID>>> users = new HashMap<UUID, Map<Group, Set<UUID>>>();
		PreparedStatement preparedStatement = null;
		
		String query = 	  "SELECT * " 
						+ "FROM `" + this.getTableUsers() + "` "
						+ "WHERE `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, world.toString());
			ResultSet list = preparedStatement.executeQuery();
			
			while (list.next()) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(list.getString("region"));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + list.getString("region") + "';world='" + world + "',region_group='" + list.getString("group") + "';user='" + list.getString("uuid") + "')");
					continue;
				}
				
				Optional<Group> group = this.plugin.getGame().getRegistry().getType(Group.class, list.getString("group"));
				if (!group.isPresent()) {
					this.plugin.getELogger().warn("Error : Unsupported Group (region='" + uuid + "';world='" + world + "',region_group='" + list.getString("group") + "';user='" + list.getString("uuid") + "')");
					continue;
				}
				
				Map<Group, Set<UUID>> map = users.get(uuid);
				if (map == null) {
					map = new HashMap<Group, Set<UUID>>();
					users.put(uuid, map);
				}
				Set<UUID> value = map.get(group.get());
				if (value == null) {
					value = new HashSet<UUID>();
					map.put(group.get(), value);
				}
					
				try {
					value.add(UUID.fromString(list.getString("uuid")));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the user is incorrect : (region='" + uuid + "';world='" + world + "',region_group='" + group.get().getId() + "';user='" + list.getString("uuid") + "')");
					continue;
				}
			}
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the load of users : (world='" + world + "') " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return users;
	}
	
	public Map<UUID, Map<Group, Set<String>>> getAllGroups(final Connection connection, final UUID world) {
		Map<UUID, Map<Group, Set<String>>> groups = new HashMap<UUID, Map<Group, Set<String>>>();
		PreparedStatement preparedStatement = null;
		
		String query = 	  "SELECT * " 
						+ "FROM `" + this.getTableGroups() + "` "
						+ "WHERE `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, world.toString());
			ResultSet list = preparedStatement.executeQuery();
			
			while (list.next()) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(list.getString("region"));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + list.getString("uuid") + "';world='" + world + "',region_group='" + list.getString("group") + "';group='" + list.getString("name") + "')");
					continue;
				}
				
				Optional<Group> group = this.plugin.getGame().getRegistry().getType(Group.class, list.getString("group"));
				if (!group.isPresent()) {
					this.plugin.getELogger().warn("Error : Unsupported Group (region='" + uuid + "';world='" + world + "',region_group='" + list.getString("group") + "';group='" + list.getString("name") + "')");
					continue;
				}
				
				Map<Group, Set<String>> map = groups.get(uuid);
				if (map == null) {
					map = new HashMap<Group, Set<String>>();
					groups.put(uuid, map);
				}
				Set<String> value = map.get(group.get());
				if (value == null) {
					value = new HashSet<String>();
					map.put(group.get(), value);
				}
				
				value.add(list.getString("name"));
			}
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the load of groups : (world='" + world + "') " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return groups;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Map<UUID, Map<Flag<?>, EFlagValue<?>>> getAllFlags(final Connection connection, final UUID world) {
		Map<UUID, Map<Flag<?>, EFlagValue<?>>> flags = new HashMap<UUID, Map<Flag<?>, EFlagValue<?>>>();
		PreparedStatement preparedStatement = null;
		
		String query = 	  "SELECT * " 
						+ "FROM `" + this.getTableFlags() + "` "
						+ "WHERE `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, world.toString());
			ResultSet list = preparedStatement.executeQuery();
			
			while (list.next()) {
				UUID uuid = null;
				try {
					uuid = UUID.fromString(list.getString("region"));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + list.getString("uuid") + "';world='" + world + "',region_group='" + list.getString("group") + "';flag='" + list.getString("flag") + "')");
					continue;
				}
				
				Map<Flag<?>, EFlagValue<?>> map = flags.get(uuid);
				if (map == null) {
					map = new HashMap<Flag<?>, EFlagValue<?>>();
					flags.put(uuid, map);
				}
				
				Optional<Group> group = this.plugin.getGame().getRegistry().getType(Group.class, list.getString("group"));
				if (!group.isPresent()) {
					this.plugin.getELogger().warn("Error : Unsupported Group (region='" + uuid + "';world='" + world + "',region_group='" + list.getString("group") + "';flag='" + list.getString("flag") + "')");
					continue;
				}
				
				Optional<Flag<?>> optFlag = this.plugin.getProtectionService().getFlag(list.getString("flag"));
				if (!optFlag.isPresent()) {
					this.plugin.getELogger().warn("Error : Unsupported Flag (region='" + uuid + "';world='" + world + "',region_group='" + list.getString("group") + "';flag='" + list.getString("flag") + "')");
					continue;
				}
				
				Flag<T> flag = (Flag<T>) optFlag.get();
				
				EFlagValue<T> flagValue = (EFlagValue<T>) map.get(flag);
				if (flagValue == null) {
					flagValue = new EFlagValue<T>();
					map.put(flag, flagValue);
				}
				
				try {
					T value = flag.deserialize(list.getString("value"));
					flagValue.set(group.get(), value);
				} catch(Exception e) {
					this.plugin.getELogger().warn("The value of the flag is incorrect (region='" + uuid + "';world='" + world + "',region_group='" + list.getString("group") + "';flag='" + list.getString("flag") + "')");
				}
			}
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the load of flags : (world='" + world + "') " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return flags;
	}
	
	public <V> boolean insertRegion(final Connection connection, final UUID world, final UUID identifier, final String name, 
			final ProtectedRegion.Type type, int priority, final @Nullable UUID parent) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "INSERT INTO `" + this.getTableRegions() + "` (`uuid`, `world`, `name`, `type`, `priority`, `parent`) " 
						+ "VALUES (?, ?, ?, ?, ?, ?);";
    	try {
    		preparedStatement = connection.prepareStatement(query);
    		preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			preparedStatement.setString(3, name);
			preparedStatement.setString(4, type.getId());
			preparedStatement.setInt(5, priority);
			preparedStatement.setString(6, (parent != null) ? parent.toString() : null);
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the addition of a region (name='" + name + "';world='" + world + "') : " + e.getMessage());
		}
		return false;
	}
	
	public <V> boolean deleteRegion(final Connection connection, final UUID world, final UUID identifier) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableRegions() + "` "
						+ "WHERE `region` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of a region (uuid='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean insertPositions(final Connection connection, final UUID world, final UUID identifier, final List<Vector3i> positions) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "INSERT INTO `" + this.getTablePositions() + "` (`region`, `world`, `id`, `x`, `y`, `z`) " 
						+ "VALUES (?, ?, ?, ?, ?, ?);";
    	try {
    		int id = 1;
    		for (Vector3i position : positions) {
	    		preparedStatement = connection.prepareStatement(query);
	    		preparedStatement.setString(1, identifier.toString());
				preparedStatement.setString(2, world.toString());
				preparedStatement.setInt(3, id);
				preparedStatement.setInt(4, position.getX());
				preparedStatement.setInt(5, position.getY());
				preparedStatement.setInt(6, position.getZ());
				
				preparedStatement.execute();
				preparedStatement.close();
				id++;
    		}
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the addition of a position (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		}
		return false;
	}
	
	public <V> boolean deletePositions(final Connection connection, final UUID world, final UUID identifier) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTablePositions() + "` "
						+ "WHERE `region` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of a position (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public boolean updateName(final Connection connection, final UUID world, final UUID identifier, final String name) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "UPDATE `" + this.getTableRegions() + "` " 
						+ "SET `name` = ? "
						+ "WHERE `uuid` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, name);
			preparedStatement.setString(2, identifier.toString());
			preparedStatement.setString(3, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the update of the name of a region (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public boolean updatePriority(final Connection connection, final UUID world, final UUID identifier, int priority) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "UPDATE `" + this.getTableRegions() + "` " 
						+ "SET `priority` = ? "
						+ "WHERE `uuid` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, priority);
			preparedStatement.setString(2, identifier.toString());
			preparedStatement.setString(3, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the update of the priority of a region (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public boolean updateParent(final Connection connection, final UUID world, final UUID identifier, final @Nullable UUID parent) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "UPDATE `" + this.getTableRegions() + "` " 
						+ "SET `parent` = ? "
						+ "WHERE `uuid` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
    		if (parent == null) {
    			preparedStatement.setString(1, null);
    		} else {
    			preparedStatement.setString(1, parent.toString());
    		}
			preparedStatement.setString(2, identifier.toString());
			preparedStatement.setString(3, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the update of the parent of a region (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public boolean updateType(final Connection connection, final UUID world, final UUID identifier, final ProtectedRegion.Type type) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "UPDATE `" + this.getTableRegions() + "` " 
						+ "SET `type` = ? "
						+ "WHERE `uuid` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
    		preparedStatement.setString(1, type.getId());
			preparedStatement.setString(2, identifier.toString());
			preparedStatement.setString(3, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the update of the type of a region (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean insertFlag(final Connection connection, final UUID world, final UUID identifier, final Flag<V> flag, final Group group, final V value) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "INSERT INTO `" + this.getTableFlags() + "` (`region`, `world`, `group`, `flag`, `value`) " 
						+ "VALUES (?, ?, ?, ?, ?);";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
    		preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			preparedStatement.setString(3, group.getId());
			preparedStatement.setString(4, flag.getName());
			preparedStatement.setString(5, flag.serialize(value));
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the addition of a flag (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean updateFlag(final Connection connection, final UUID world, final UUID identifier, final Flag<V> flag, final Group group, final V value) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "UPDATE `" + this.getTableFlags() + "` " 
						+ "SET `value` = ? "
						+ "WHERE `region` = ? AND `world` = ? AND `flag` = ? AND `group` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
    		preparedStatement.setString(1, flag.serialize(value));
			preparedStatement.setString(2, identifier.toString());
			preparedStatement.setString(3, world.toString());
			preparedStatement.setString(4, flag.getId());
			preparedStatement.setString(5, group.getId());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the update of a flag (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean deleteFlag(final Connection connection, final UUID world, final UUID identifier, final Flag<V> flag, final Group group) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableFlags() + "` "
						+ "WHERE `region` = ? AND `world` = ? AND `flag` = ? AND `group` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			preparedStatement.setString(3, flag.getId());
			preparedStatement.setString(4, group.getId());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of a flag (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean deleteFlag(final Connection connection, final UUID world, final UUID identifier) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableFlags() + "` "
						+ "WHERE `region` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of flags (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean insertUser(final Connection connection, final UUID world, final UUID identifier, final Group group, final Set<UUID> users) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "INSERT INTO `" + this.getTableUsers() + "` (`region`, `world`, `group`, `uuid`) " 
						+ "VALUES (?, ?, ?, ?);";
    	try {
    		for (UUID user : users) {
	    		preparedStatement = connection.prepareStatement(query);
	    		preparedStatement.setString(1, identifier.toString());
				preparedStatement.setString(2, world.toString());
				preparedStatement.setString(3, group.getId());
				preparedStatement.setString(4, user.toString());
				
				preparedStatement.execute();
				preparedStatement.close();
    		}
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the addition of a user (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		}
		return false;
	}
	
	public <V> boolean deleteUser(final Connection connection, final UUID world, final UUID identifier, final Group group, final Set<UUID> users) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableUsers() + "` "
						+ "WHERE `region` = ? AND `world` = ? AND `group` = ? AND `uuid` IN ('" + users.stream().map(user -> user.toString()).reduce((u1, u2) -> u1 + "', '" + u2).orElse("") + "') ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			preparedStatement.setString(3, group.getId());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of a user (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean deleteUser(final Connection connection, final UUID world, final UUID identifier) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableUsers() + "` "
						+ "WHERE `region` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of users (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean insertGroup(final Connection connection, final UUID world, final UUID identifier, final Group group, final Set<String> groups) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "INSERT INTO `" + this.getTableGroups() + "` (`region`, `world`, `group`, `name`) " 
						+ "VALUES (?, ?, ?, ?);";
    	try {
    		for (String name : groups) {
	    		preparedStatement = connection.prepareStatement(query);
	    		preparedStatement.setString(1, identifier.toString());
				preparedStatement.setString(2, world.toString());
				preparedStatement.setString(3, group.getId());
				preparedStatement.setString(4, name);
				
				preparedStatement.execute();
				preparedStatement.close();
    		}
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the addition of a group (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		}
		return false;
	}
	
	public <V> boolean deleteGroup(final Connection connection, final UUID world, final UUID identifier, final Group group, final Set<String> groups) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableGroups() + "` "
						+ "WHERE `region` = ? AND `world` = ? AND `group` = ? AND `name` IN ('" + String.join("', '", groups) + "') ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			preparedStatement.setString(3, group.getId());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of a group (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	public <V> boolean deleteGroup(final Connection connection, final UUID world, final UUID identifier) {
		PreparedStatement preparedStatement = null;
		
		String query = 	  "DELETE FROM `" + this.getTableGroups() + "` "
						+ "WHERE `region` = ? AND `world` = ? ;";
    	try {    		
    		preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, identifier.toString());
			preparedStatement.setString(2, world.toString());
			
			preparedStatement.execute();
			return true;
		} catch (SQLException e) {
			this.plugin.getELogger().warn("Error during the deletion of group (region='" + identifier + "';world='" + world + "') : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return false;
	}
	
	/**
	 * Supprimé les données de la base de données
	 * @param connection La connection SQL
	 * @return Retourne True s'il n'y a pas eu d'erreur
	 */
	public boolean clear(final Connection connection) {
		boolean resultat = false;
		PreparedStatement preparedStatement = null;
		try {
			// Positions
			preparedStatement = connection.prepareStatement("DELETE FROM  `" + this.getTablePositions() + "` ;");
			preparedStatement.execute();
			preparedStatement.close();
		    	
			// Users
			preparedStatement = connection.prepareStatement("DELETE FROM  `" + this.getTableUsers() + "` ;");
			preparedStatement.execute();
			preparedStatement.close();
			
			// Groups
			preparedStatement = connection.prepareStatement("DELETE FROM  `" + this.getTableGroups() + "` ;");
			preparedStatement.execute();
			preparedStatement.close();
			
			// Flags
			preparedStatement = connection.prepareStatement("DELETE FROM  `" + this.getTableFlags() + "` ;");
			preparedStatement.execute();
			preparedStatement.close();
			
			// Regions
			preparedStatement = connection.prepareStatement("DELETE FROM  `" + this.getTableRegions() + "` ;");
			preparedStatement.execute();
			preparedStatement.close();

			resultat = true;
    	} catch (SQLException e) {
			this.plugin.getELogger().warn("Error while deleting the database : " + e.getMessage());
		} finally {
			try {if (preparedStatement != null) preparedStatement.close();} catch (SQLException e) {}
	    }
		return resultat;
	}
}
