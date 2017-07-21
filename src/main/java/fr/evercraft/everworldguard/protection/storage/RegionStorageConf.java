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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.worldguard.Flag;
import fr.evercraft.everapi.services.worldguard.FlagValue;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Group;
import fr.evercraft.everapi.services.worldguard.region.ProtectedRegion.Groups;
import fr.evercraft.everworldguard.EverWorldGuard;
import fr.evercraft.everworldguard.protection.EProtectionService;
import fr.evercraft.everworldguard.protection.flag.EFlagValue;
import fr.evercraft.everworldguard.protection.index.EWWorld;
import fr.evercraft.everworldguard.protection.regions.EProtectedCuboidRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedGlobalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedPolygonalRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedRegion;
import fr.evercraft.everworldguard.protection.regions.EProtectedTemplateRegion;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class RegionStorageConf extends EConfig<EverWorldGuard> implements RegionStorage {
	
	public static final String MKDIR = "worlds";
	
	private final EverWorldGuard plugin;
	private final EWWorld world;
	
	public RegionStorageConf(EverWorldGuard plugin, EWWorld world) {
		super(plugin, MKDIR + "/" + world.getWorld().getName(), false);
		Preconditions.checkNotNull(world);

		this.plugin = plugin;
		this.world = world;
	}
	
	@Override
	protected void loadDefault() {}

    private CompletableFuture<Boolean> loadGlobal() {
		for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
			if (config.getValue().getNode("type").getString("").equalsIgnoreCase(ProtectedRegion.Types.GLOBAL.getName())) {
				return CompletableFuture.completedFuture(true);
			}
		}
    	
		return this.add(new EProtectedGlobalRegion(this.world, this.world.nextUUID(), EProtectionService.GLOBAL_REGION));
	}
    
    public boolean isSql() {
    	return false;
    }

	@Override
	public CompletableFuture<Set<EProtectedRegion>> getAll() {
		return this.loadGlobal().thenApply(result -> {
		
			Map<String, EProtectedRegion> regions = new HashMap<String, EProtectedRegion>();
			for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
				try {
					this.get(UUID.fromString(config.getKey().toString()), config.getValue())
						.ifPresent(region -> regions.put(region.getId().toString(), region));
				} catch (IllegalArgumentException e) {
					this.plugin.getELogger().warn("Error the uuid of the region is incorrect : (region='" + config.getKey().toString() + "';world='" + this.world.getUniqueId() + "')");
				}
			}
			
			// Parents
			for (Entry<Object, ? extends ConfigurationNode> config : this.getNode().getChildrenMap().entrySet()) {
				if (config.getKey() instanceof String) {
					String key = (String) config.getKey();
					String value = config.getValue().getNode("parent").getString("");
					EProtectedRegion region = regions.get(key.toLowerCase());
					if (region != null && !value.isEmpty()) {
						EProtectedRegion parent = regions.get(value.toLowerCase());
						if (parent != null) {
							region.init(parent);
						} else {
							this.plugin.getELogger().warn("Unable to find the parent region '" + value + "' (region='" + region.getId() + "';world='" + world.getUniqueId() + "')");
						}
					}
				}
			}
			
			return ImmutableSet.copyOf(regions.values());
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T> Optional<EProtectedRegion> get(UUID identifier, ConfigurationNode config) {
		// Name
		String name = config.getNode("name").getString(identifier.toString());
		
		// Type
		String type_string = config.getNode("type").getString("");
		Optional<ProtectedRegion.Type> optType = this.plugin.getGame().getRegistry().getType(ProtectedRegion.Type.class, type_string);
		if (!optType.isPresent()) {
			this.plugin.getELogger().warn("Error : Type unknown (region='" + identifier + "';world='" + world.getUniqueId() + "';type='" + type_string + "')");
			return Optional.empty();
		}
		ProtectedRegion.Type type = optType.get();
		
		// Priority
		int priority = config.getNode("priority").getInt(0);
		
		// Owners
		Set<UUID> owners = null;
		try {
			owners = ImmutableSet.copyOf(config.getNode("owners").getList(TypeToken.of(UUID.class)));
		} catch (ObjectMappingException e) {
			owners = ImmutableSet.of();
			this.plugin.getELogger().warn("Error the uuid of the user is incorrect : (region='" + identifier + "';world='" + world + "',region_group='OWNER'");
		}
		
		Set<String> group_owners = null;
		try {
			group_owners = ImmutableSet.copyOf(config.getNode("group-owners").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_owners = ImmutableSet.of();
			this.plugin.getELogger().warn("Error the group is incorrect : (region='" + identifier + "';world='" + world + "',region_group='OWNER'");
		}
		
		// Members
		Set<UUID> members = null;
		try {
			members = ImmutableSet.copyOf(config.getNode("members").getList(TypeToken.of(UUID.class)));
		} catch (ObjectMappingException e) {
			members = ImmutableSet.of();
			this.plugin.getELogger().warn("Error the uuid of the user is incorrect : (region='" + identifier + "';world='" + world + "',region_group='MEMBER'");
		}
		
		Set<String> group_members = null;
		try {
			group_members = ImmutableSet.copyOf(config.getNode("group-members").getList(TypeToken.of(String.class)));
		} catch (ObjectMappingException e) {
			group_members = ImmutableSet.of();
			this.plugin.getELogger().warn("Error the group is incorrect : (region='" + identifier + "';world='" + world + "',region_group='MEMBER'");
		}
		
		// Flags
		Map<Flag<?>, EFlagValue<?>> flags = new HashMap<Flag<?>, EFlagValue<?>>();
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-default").getChildrenMap().entrySet()) {
			Optional<Flag<?>> optFlag = this.plugin.getProtectionService().getFlag(config_flags.getKey().toString());
			if (optFlag.isPresent()) {
				Flag<T> flag = (Flag<T>) optFlag.get();
				try {
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Groups.DEFAULT, value));
				} catch(Exception e) {
					this.plugin.getELogger().warn("The value of the flag is incorrect (region='" + identifier + "';world='" + world + "',region_group='DEFAULT';flag='" + config_flags.getKey() + "')");
				}
			} else {
				this.plugin.getELogger().warn("Error : Unsupported Flag (region='" + identifier + "';world='" + world + "',region_group='DEFAULT';flag='" + config_flags.getKey() + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-member").getChildrenMap().entrySet()) {
			Optional<Flag<?>> optFlag = this.plugin.getProtectionService().getFlag(config_flags.getKey().toString());
			if (optFlag.isPresent()) {
				Flag<T> flag = (Flag<T>) optFlag.get();
				try {
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Groups.MEMBER, value));
				} catch(Exception e) {
					this.plugin.getELogger().warn("The value of the flag is incorrect (region='" + identifier + "';world='" + world + "',region_group='MEMBER';flag='" + config_flags.getKey() + "')");
				}
			} else {
				this.plugin.getELogger().warn("Error : Unsupported Flag (region='" + identifier + "';world='" + world + "',region_group='MEMBER';flag='" + config_flags.getKey() + "')");
			}
		}
		
		for (Entry<Object, ? extends ConfigurationNode> config_flags : config.getNode("flags-owner").getChildrenMap().entrySet()) {
			Optional<Flag<?>> optFlag = this.plugin.getProtectionService().getFlag(config_flags.getKey().toString());
			if (optFlag.isPresent()) {
				Flag<T> flag = (Flag<T>) optFlag.get();
				try {
					T value = flag.deserialize(config_flags.getValue().getString(""));
					flags.put(flag, this.putFlags((EFlagValue<T>) flags.get(flag), Groups.OWNER, value));
				} catch(Exception e) {
					this.plugin.getELogger().warn("The value of the flag is incorrect (region='" + identifier + "';world='" + world + "',region_group='MEMBER';flag='" + config_flags.getKey() + "')");
				}
			} else {
				this.plugin.getELogger().warn("Error : Unsupported Flag (region='" + identifier + "';world='" + world + "',region_group='MEMBER';flag='" + config_flags.getKey() + "')");
			}
		}
		
		EProtectedRegion region = null;
		if (type.equals(ProtectedRegion.Types.GLOBAL)) {
			region = new EProtectedGlobalRegion(this.world, identifier, name);
		} else if (type.equals(ProtectedRegion.Types.TEMPLATE)) {
			region = new EProtectedTemplateRegion(this.world, identifier, name);
		} else if (type.equals(ProtectedRegion.Types.CUBOID)) {
			Vector3i min = null;
			Vector3i max = null;
			
			try {
				min = config.getNode("min").getValue(TypeToken.of(Vector3i.class));
			} catch (ObjectMappingException e1) {}
			if (min == null) {
				this.plugin.getELogger().warn("The maximum position is incorrect (region='" + identifier + "';world='" + world + "')");
				return Optional.empty();
			}
			
			
			try {
				max = config.getNode("max").getValue(TypeToken.of(Vector3i.class));
			} catch (ObjectMappingException e) {}
			if (max == null) {
				this.plugin.getELogger().warn("The maximum position is incorrect (region='" + identifier + "';world='" + world + "')");
				return Optional.empty();
			}

			region = new EProtectedCuboidRegion(this.world, identifier, name, min, max);
		} else if (type.equals(ProtectedRegion.Types.POLYGONAL)) {
			List<Vector3i> vectors = null;
			try {
				vectors = config.getNode("positions").getList(TypeToken.of(Vector3i.class));
			} catch (ObjectMappingException e) {}
            if (vectors == null) {
            	this.plugin.getELogger().warn("The list of positions is incorrect (region='" + identifier + "';world='" + world + "')");
                return Optional.empty();
            }

			region = new EProtectedPolygonalRegion(this.world, identifier, name, vectors);
		} else {
			return Optional.empty();
		}
	
		region.init(priority, owners, group_owners, members, group_members, flags);
		return Optional.of(region);
	}
	
	public <T> EFlagValue<T> putFlags(EFlagValue<T> values, Group association, T value) {
		if (values == null) {
			values = new EFlagValue<T>();
		}
		values.set(association, value);
		return values;
	}
	
	public boolean addAll(Set<EProtectedRegion> regions) {
		regions.forEach(region -> this.add(region, this.getNode().getNode(region.getId().toString())));
		return this.save(true);
	}
	
	@Override
	public CompletableFuture<Boolean> add(EProtectedRegion region) {
		this.add(region, this.getNode().getNode(region.getId().toString()));
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@SuppressWarnings("unchecked")
	public <T> void add(EProtectedRegion region, ConfigurationNode config) {
		// Type
		config.getNode("name").setValue(region.getName());
		
		// Type
		config.getNode("type").setValue(region.getType().getName());
		config.getNode("priority").setValue(region.getPriority());
		
		// Owners
		config.getNode("owners").setValue(region.getOwners().getPlayers()
				.stream().map(uuid -> uuid.toString()).collect(Collectors.toSet()));
		if (!region.getOwners().getGroups().isEmpty()) {
			config.getNode("group-owners").setValue(region.getOwners().getGroups());
		}
		
		// Members
		config.getNode("members").setValue(region.getMembers().getPlayers()
				.stream().map(uuid -> uuid.toString()).collect(Collectors.toSet()));
		if (!region.getMembers().getGroups().isEmpty()) {
			config.getNode("group-members").setValue(region.getMembers().getGroups());
		}
		
		// Parent
		region.getParent().ifPresent(parent -> 
			config.getNode("parent").setValue(parent.getId().toString()));
		
		// Flags
		Map<String, String> flags_owner = new HashMap<String, String>();
		Map<String, String> flags_member = new HashMap<String, String>();
		Map<String, String> flags_default = new HashMap<String, String>();
		
		for (Entry<Flag<?>, FlagValue<?>> flag : region.getFlags().entrySet()) {
			Flag<T> key = (Flag<T>) flag.getKey();
			for (Entry<Group, ?> value : flag.getValue().getAll().entrySet()) {
				T val = (T) value.getValue();
				if (value.getKey().equals(Groups.DEFAULT)) {
					flags_default.put(key.getId(), key.serialize(val));
				} else if (value.getKey().equals(Groups.MEMBER)) {
					flags_member.put(key.getId(), key.serialize(val));
				} else if (value.getKey().equals(Groups.OWNER)) {
					flags_default.put(key.getId(), key.serialize(val));
				}
			}
		}
		
		if (!flags_owner.isEmpty()) {
			config.getNode("flags-owner").setValue(flags_owner);
		}
		if (!flags_member.isEmpty()) {
			config.getNode("flags-member").setValue(flags_member);
		}
		if (!flags_default.isEmpty()) {
			config.getNode("flags-default").setValue(flags_default);
		}
		
		// Points
		if (region.getType().equals(ProtectedRegion.Types.CUBOID)) {
			try {
				config.getNode("min").setValue(TypeToken.of(Vector3i.class), region.getMinimumPoint());
				config.getNode("max").setValue(TypeToken.of(Vector3i.class), region.getMaximumPoint());
			} catch (ObjectMappingException e) {}
		} else if (region.getType().equals(ProtectedRegion.Types.POLYGONAL)) {
			region.getPoints().forEach(point -> {
				try {
					config.getNode("positions").getAppendedNode().setValue(TypeToken.of(Vector3i.class), point);
				} catch (ObjectMappingException e) {}
			});
		}
	}
	
	@Override
	public CompletableFuture<Boolean> setName(EProtectedRegion region, String name) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		config.getNode("name").setValue(name);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> setPriority(EProtectedRegion region, int priority) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		config.getNode("priority").setValue(priority);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> setParent(EProtectedRegion region, ProtectedRegion parent) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
        if (parent  == null) {
            config.removeChild("parent");
        } else {
            config.getNode("parent").setValue(parent.getId().toString());
        }
        return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public <V> CompletableFuture<Boolean> setFlag(EProtectedRegion region, Flag<V> flag, Group group, V value) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		if (group.equals(Groups.OWNER)) {
			config = config.getNode("flags-owner");
		} else if (group.equals(Groups.MEMBER)) {
			config = config.getNode("flags-member");
		} else if (group.equals(Groups.DEFAULT)) {
			config = config.getNode("flags-default");
		}
		
		if (value == null) {
			config.removeChild(flag.getId());
		} else {
			config.getNode(flag.getId()).setValue(flag.serialize(value));
		}
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.addAll(players);
		
		config.getNode("owners").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addOwnerGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.addAll(groups);
		
		config.getNode("group-owners").setValue(all);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.addAll(players);
		
		config.getNode("members").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> addMemberGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.addAll(groups);
		
		config.getNode("group-members").setValue(all);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.removeAll(players);
		
		config.getNode("owners").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeOwnerGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.removeAll(groups);
		
		config.getNode("group-owners").setValue(all);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeMemberPlayer(EProtectedRegion region, Set<UUID> players) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<UUID> all = new HashSet<UUID>();
		all.addAll(region.getMembers().getPlayers());
		all.removeAll(players);
		
		config.getNode("members").setValue(all.stream()
				.map(uuid -> uuid.toString())
				.collect(Collectors.toSet()));
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeMemberGroup(EProtectedRegion region, Set<String> groups) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		Set<String> all = new HashSet<String>();
		all.addAll(region.getMembers().getGroups());
		all.removeAll(groups);
		
		config.getNode("group-members").setValue(all);
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}
	
	@Override
	public CompletableFuture<Boolean> removeRemoveChildren(Set<EProtectedRegion> regions) {
		regions.forEach(children -> {
			if (children.getType().equals(ProtectedRegion.Types.GLOBAL)) {
				this.getNode().getNode(children.getId().toString()).removeChild("parent");
			} else {
				this.getNode().removeChild(children.getId().toString());
			}
		});
		
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> removeClearParent(EProtectedRegion region, Set<EProtectedRegion> regions) {
		this.getNode().removeChild(region.getId().toString());
		
		regions.forEach(children -> {
			this.getNode().getNode(children.getId().toString()).removeChild("parent");
		});
		
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> redefine(EProtectedRegion region, EProtectedRegion newRegion) {
		ConfigurationNode config = this.getNode().getNode(region.getId().toString());
		
		//Type
		config.getNode("type").setValue(newRegion.getType().getName());
		
		config.removeChild("min");
		config.removeChild("max");
		config.removeChild("positions");
		
		// Points
		if (newRegion.getType().equals(ProtectedRegion.Types.CUBOID)) {
			try {
				config.getNode("min").setValue(TypeToken.of(Vector3i.class), newRegion.getMinimumPoint());
				config.getNode("max").setValue(TypeToken.of(Vector3i.class), newRegion.getMaximumPoint());
			} catch (ObjectMappingException e) {}
		} else if (region.getType().equals(ProtectedRegion.Types.POLYGONAL)) {
			newRegion.getPoints().forEach(point -> {
				try {
					config.getNode("positions").getAppendedNode().setValue(TypeToken.of(Vector3i.class), point);
				} catch (ObjectMappingException e) {}
			});
		}
		
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}

	@Override
	public CompletableFuture<Boolean> clearAll() {
		this.getNode().setValue(ImmutableMap.of());
		return CompletableFuture.supplyAsync(() -> this.save(true), this.plugin.getThreadAsync());
	}
}
